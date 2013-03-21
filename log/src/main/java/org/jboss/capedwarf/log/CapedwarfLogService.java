/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.capedwarf.log;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.RequestLogs;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.AdvancedCache;
import org.infinispan.context.Flag;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.FetchOptions;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.jboss.capedwarf.common.apiproxy.CapedwarfDelegate;
import org.jboss.capedwarf.common.compatibility.CompatibilityUtils;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.shared.compatibility.Compatibility;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class CapedwarfLogService implements ExposedLogService {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

    private static final String REQUEST_LOGS_REQUEST_ATTRIBUTE = "__org.jboss.capedwarf.LogRequest__";
    private static final String REQUEST_LOGS_ENV_ATTRIBUTE = "com.google.appengine.runtime.request_logs";
    private static final String REQUEST_LOG_ID = "com.google.appengine.runtime.request_log_id";

    private final AdvancedCache<String, CapedwarfLogElement> store;
    private final SearchManager searchManager;

    private final boolean ignoreLogging;
    private final LogWriter logWriter;

    public CapedwarfLogService() {

        String appId = CapedwarfEnvironment.getThreadLocalInstance().getAppId();
        store = InfinispanUtils.<String, CapedwarfLogElement>getCache(appId, CacheName.LOGS)
            .getAdvancedCache()
            .withFlags(Flag.IGNORE_RETURN_VALUES);
        this.searchManager = Search.getSearchManager(store);

        Compatibility instance = CompatibilityUtils.getInstance();
        ignoreLogging = instance.isEnabled(Compatibility.Feature.IGNORE_LOGGING);
        if (instance.isEnabled(Compatibility.Feature.ASYNC_LOGGING)) {
            logWriter = new AsyncLogWriter();
        } else {
            logWriter = new SyncLogWriter();
        }
    }

    private String getLogLineKey(CapedwarfAppLogLine logLine) {
        return generateId();
    }

    public Iterable<RequestLogs> fetch(LogQuery logQuery) {
        List<RequestLogs> list = new ArrayList<RequestLogs>();
        for (CapedwarfRequestLogs capedwarfRequestLogs : fetchCapedwarfRequestLogs(logQuery)) {
            RequestLogs requestLogs = capedwarfRequestLogs.getRequestLogs();
            if (logQuery.getIncludeAppLogs()) {
                fetchAppLogLines(requestLogs, logQuery);
            }
            list.add(requestLogs);
        }
        return list;
    }

    private List<CapedwarfRequestLogs> fetchCapedwarfRequestLogs(LogQuery logQuery) {
        if (logQuery.getRequestIds().isEmpty()) {
            CacheQuery cacheQuery = createRequestLogsQuery(logQuery);
            return (List<CapedwarfRequestLogs>) (List) cacheQuery.list();
        } else {
            List<CapedwarfRequestLogs> list = new ArrayList<CapedwarfRequestLogs>(logQuery.getRequestIds().size());
            for (String requestId : logQuery.getRequestIds()) {
                CapedwarfRequestLogs requestLogs = (CapedwarfRequestLogs) store.get(requestId);
                if (requestLogs != null) {
                    list.add(requestLogs);
                }
            }
            return list;
        }
    }

    private CacheQuery createRequestLogsQuery(LogQuery logQuery) {
        QueryBuilder queryBuilder = searchManager.buildQueryBuilderForClass(CapedwarfRequestLogs.class).get();
        List<Query> queries = new ArrayList<Query>();
        if (logQuery.getStartTimeUsec() != null) {
            queries.add(queryBuilder.range().onField(CapedwarfRequestLogs.END_TIME_USEC).above(logQuery.getStartTimeUsec()).createQuery());
        }
        if (logQuery.getEndTimeUsec() != null) {
            queries.add(queryBuilder.range().onField(CapedwarfRequestLogs.END_TIME_USEC).below(logQuery.getEndTimeUsec()).createQuery());
        }
        if (logQuery.getMinLogLevel() != null) {
            queries.add(queryBuilder.range().onField(CapedwarfRequestLogs.MAX_LOG_LEVEL).above(logQuery.getMinLogLevel().ordinal()).createQuery());
        }

        boolean onlyCompleteRequests = !Boolean.TRUE.equals(logQuery.getIncludeIncomplete());
        if (onlyCompleteRequests) {
            queries.add(queryBuilder.keyword().onField(CapedwarfRequestLogs.FINISHED).matching(Boolean.TRUE).createQuery());
        }

        Query query = getQuery(queryBuilder, queries);
        CacheQuery cacheQuery = searchManager.getQuery(query, CapedwarfRequestLogs.class);
        cacheQuery.sort(new Sort(new SortField(CapedwarfRequestLogs.END_TIME_USEC, SortField.LONG)));
        return cacheQuery;
    }

    private Query getQuery(QueryBuilder queryBuilder, List<Query> queries) {
        if (queries.isEmpty()) {
            return queryBuilder.all().createQuery();
        } else {
            BooleanJunction<BooleanJunction> bool = queryBuilder.bool();
            for (Query query : queries) {
                bool.must(query);
            }
            return bool.createQuery();
        }
    }

    private void fetchAppLogLines(RequestLogs requestLogs, LogQuery logQuery) {
        CacheQuery query = createAppLogLinesQuery(requestLogs);
//        FetchOptions fetchOptions = createAppLogFetchOptions(logQuery);

        List<CapedwarfAppLogLine> capedwarfAppLogLines = (List<CapedwarfAppLogLine>) (List) query.list();
        for (CapedwarfAppLogLine capedwarfAppLogLine : capedwarfAppLogLines) {
            requestLogs.getAppLogLines().add(capedwarfAppLogLine.getAppLogLine());
        }
    }

    private CacheQuery createAppLogLinesQuery(RequestLogs requestLogs) {
        QueryBuilder queryBuilder = searchManager.buildQueryBuilderForClass(CapedwarfAppLogLine.class).get();
        Query query = queryBuilder.keyword().onField(CapedwarfAppLogLine.REQUEST_ID).matching(requestLogs.getRequestId()).createQuery();
        CacheQuery cacheQuery = searchManager.getQuery(query, CapedwarfAppLogLine.class);
        cacheQuery.sort(new Sort(new SortField(CapedwarfAppLogLine.SEQUENCE_NUMBER, SortField.LONG)));
        return cacheQuery;
    }

    private FetchOptions createAppLogFetchOptions(LogQuery logQuery) {
        FetchOptions fetchOptions = new FetchOptions();
        fetchOptions.fetchMode(FetchOptions.FetchMode.LAZY);
        if (logQuery.getBatchSize() != null) {
            fetchOptions.fetchSize(logQuery.getBatchSize());
        }
        return fetchOptions;
    }

    public void log(LogRecord record) {
        // did we disable logging
        if (ignoreLogging)
            return;

        CapedwarfAppLogLine capedwarfAppLogLine = new CapedwarfAppLogLine(getCurrentRequestId(), record.getSequenceNumber());
        AppLogLine appLogLine = capedwarfAppLogLine.getAppLogLine();
        appLogLine.setLogLevel(getLogLevel(record));
        appLogLine.setLogMessage(getFormattedMessage(record));
        appLogLine.setTimeUsec(record.getMillis() * 1000);
        logWriter.put(capedwarfAppLogLine);

        CapedwarfRequestLogs requestLogs = getCurrentRequestLogs();
        requestLogs.logLineAdded(appLogLine);
        logWriter.put(requestLogs);
    }

    private String getFormattedMessage(LogRecord record) {
        return new MessageFormat(record.getMessage()).format(record.getParameters());
    }

    private String getCurrentRequestId() {
        return getRequestId(getCurrentRequest());
    }

    private ServletRequest getCurrentRequest() {
        CapedwarfDelegate capedwarfDelegate = CapedwarfDelegate.INSTANCE;
        return capedwarfDelegate.getServletRequest();
    }

    private CapedwarfRequestLogs getCurrentRequestLogs() {
        CapedwarfEnvironment environment = CapedwarfEnvironment.getThreadLocalInstance();
        return (CapedwarfRequestLogs) environment.getAttributes().get(REQUEST_LOGS_ENV_ATTRIBUTE);
    }

    private LogLevel getLogLevel(LogRecord record) {
        int level = record.getLevel().intValue();
        if (level <= Level.CONFIG.intValue()) {
            return LogLevel.DEBUG;
        } else if (level <= Level.INFO.intValue()) {
            return LogLevel.INFO;
        } else if (level <= Level.WARNING.intValue()) {
            return LogLevel.WARN;
        } else if (level <= Level.SEVERE.intValue()) {
            return LogLevel.ERROR;
        } else {
            return LogLevel.FATAL;
        }
    }

    public void requestStarted(ServletRequest servletRequest, long startTimeMillis) {
        if (ignoreLogging) {
            return;
        }

        CapedwarfEnvironment environment = CapedwarfEnvironment.getThreadLocalInstance();
        CapedwarfRequestLogs capedwarfRequestLogs = createCapedwarfRequestLogs(servletRequest, startTimeMillis, environment);

        logWriter.put(capedwarfRequestLogs);
        servletRequest.setAttribute(REQUEST_LOGS_REQUEST_ATTRIBUTE, capedwarfRequestLogs);
        environment.getAttributes().put(REQUEST_LOGS_ENV_ATTRIBUTE, capedwarfRequestLogs);
        environment.getAttributes().put(REQUEST_LOG_ID, capedwarfRequestLogs.getRequestLogs().getRequestId());
    }

    private CapedwarfRequestLogs createCapedwarfRequestLogs(ServletRequest servletRequest, long startTimeMillis, CapedwarfEnvironment environment) {
        long startTimeUsec = startTimeMillis * 1000;

        CapedwarfRequestLogs capedwarfRequestLogs = new CapedwarfRequestLogs();
        RequestLogs requestLogs = capedwarfRequestLogs.getRequestLogs();
        requestLogs.setRequestId(generateId());
        requestLogs.setStartTimeUsec(startTimeUsec);
        requestLogs.setEndTimeUsec(startTimeUsec);
        requestLogs.setFinished(false);


        requestLogs.setAppId(environment.getAppId());
        requestLogs.setVersionId(environment.getVersionId());

        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String queryString = request.getQueryString();
            requestLogs.setResource(request.getRequestURI() + (queryString == null ? "" : ("?" + queryString)));
            requestLogs.setUserAgent(request.getHeader("User-Agent"));
            requestLogs.setMethod(request.getMethod());
            requestLogs.setReferrer(request.getHeader("referer"));
            requestLogs.setHost(request.getServerName() + (request.getServerPort() == 80 ? "" : (":" + request.getServerPort())));
        }

        requestLogs.setHttpVersion(servletRequest.getProtocol());

        requestLogs.setInstanceKey("");     // TODO
        requestLogs.setReplicaIndex(-1);    // TODO

        requestLogs.setPendingTime(0);      // TODO
        requestLogs.setLatency(0);          // TODO

//        requestLogs.setUrlMapEntry();

//        requestLogs.setTaskName();
//        requestLogs.setTaskQueueName();
//        requestLogs.setWasLoadingRequest();

        requestLogs.setNickname("");
        requestLogs.setIp(servletRequest.getRemoteAddr());


        requestLogs.setCost(0);
        requestLogs.setMcycles(0);
        requestLogs.setApiMcycles(0);


        // combined='93.103.26.101 - - [17/Jan/2013:08:07:11 -0800] "GET /favicon.ico HTTP/1.1" 404 0 - "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.52 Safari/537.17"',
        requestLogs.setCombined(
            requestLogs.getIp() + " - " + requestLogs.getNickname() + (requestLogs.getNickname().isEmpty() ? "" : " ")
                + "- [" + DATE_FORMAT.format(requestLogs.getStartTimeUsec() / 1000L) + "] \""
                + requestLogs.getMethod() + " " + requestLogs.getResource() + " " + requestLogs.getHttpVersion() + "\" "
                + requestLogs.getStatus() + " " + requestLogs.getResponseSize() + " - \"" + requestLogs.getUserAgent() + "\""
        );

//        requestLogs.setOffset();  TODO
        return capedwarfRequestLogs;
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void requestFinished(ServletRequest servletRequest, int status, int contentLength) {
        CapedwarfRequestLogs capedwarfRequestLogs = getCapedwarfRequestLogs(servletRequest);
        // check if all went well
        if (capedwarfRequestLogs != null) {
            RequestLogs requestLogs = capedwarfRequestLogs.getRequestLogs();
            requestLogs.setEndTimeUsec(System.currentTimeMillis() * 1000);
            requestLogs.setStatus(status);
            requestLogs.setResponseSize(contentLength);
            requestLogs.setFinished(true);
            logWriter.put(capedwarfRequestLogs);
        }
    }

    private CapedwarfRequestLogs getCapedwarfRequestLogs(ServletRequest request) {
        return (CapedwarfRequestLogs) request.getAttribute(REQUEST_LOGS_REQUEST_ATTRIBUTE);
    }

    private String getRequestId(ServletRequest request) {
        return getCapedwarfRequestLogs(request).getRequestLogs().getRequestId();
    }

    public void clearLog() {
        store.clear();
    }

    private static interface LogWriter {
        void put(CapedwarfAppLogLine logLine);

        void put(CapedwarfRequestLogs requestLogs);
    }

    private class AsyncLogWriter implements LogWriter {
        @Override
        public void put(CapedwarfAppLogLine logLine) {
            store.putAsync(getLogLineKey(logLine), logLine);
        }

        @Override
        public void put(CapedwarfRequestLogs requestLogs) {
            store.putAsync(requestLogs.getRequestLogs().getRequestId(), requestLogs);
        }
    }

    private class SyncLogWriter implements LogWriter {
        @Override
        public void put(CapedwarfAppLogLine logLine) {
            store.put(getLogLineKey(logLine), logLine);
        }

        @Override
        public void put(CapedwarfRequestLogs requestLogs) {
            store.put(requestLogs.getRequestLogs().getRequestId(), requestLogs);
        }
    }
}
