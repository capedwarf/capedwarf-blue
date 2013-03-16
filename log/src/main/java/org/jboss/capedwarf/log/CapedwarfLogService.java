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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.RequestLogs;
import org.jboss.capedwarf.common.apiproxy.CapedwarfDelegate;
import org.jboss.capedwarf.common.compatibility.CompatibilityUtils;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.shared.compatibility.Compatibility;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;
import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN_OR_EQUAL;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class CapedwarfLogService implements ExposedLogService {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

    private static final String LOG_REQUEST_ENTITY_KIND = "__org.jboss.capedwarf.LogRequest__";
    private static final String LOG_REQUEST_START_TIME_MILLIS = "startTimeMillis";
    private static final String LOG_REQUEST_END_TIME_MILLIS = "endTimeMillis";
    private static final String LOG_REQUEST_URI = "uri";
    private static final String LOG_REQUEST_USER_AGENT = "userAgent";
    private static final String LOG_REQUEST_MAX_LOG_LEVEL = "maxLogLevel";

    private static final String LOG_REQUEST_APP_ID = "appId";
    private static final String LOG_REQUEST_VERSION_ID = "versionId";
    private static final String LOG_REQUEST_OFFSET = "offset";
    private static final String LOG_REQUEST_IP = "ip";
    private static final String LOG_REQUEST_NICKNAME = "nickname";
    private static final String LOG_REQUEST_LATENCY = "latency";
    private static final String LOG_REQUEST_MCYCLES = "mcycles";
    private static final String LOG_REQUEST_METHOD = "method";
    private static final String LOG_REQUEST_RESOURCE = "resource";
    private static final String LOG_REQUEST_HTTP_VERSION = "httpVersion";
    private static final String LOG_REQUEST_STATUS = "status";
    private static final String LOG_REQUEST_RESPONSE_SIZE = "responseSize";
    private static final String LOG_REQUEST_REFERRER = "referrer";
    private static final String LOG_REQUEST_URL_MAP_ENTRY = "urlMapEntry";
    private static final String LOG_REQUEST_COMBINED = "combined";
    private static final String LOG_REQUEST_API_MCYCLES = "apiMcycles";
    private static final String LOG_REQUEST_HOST = "host";
    private static final String LOG_REQUEST_COST = "cost";
    private static final String LOG_REQUEST_TASK_QUEUE_NAME = "taskQueueName";
    private static final String LOG_REQUEST_TASK_NAME = "taskName";
    private static final String LOG_REQUEST_LOADING_REQUEST = "wasLoadingRequest";
    private static final String LOG_REQUEST_PENDING_TIME = "pendingTime";
    private static final String LOG_REQUEST_REPLICA_INDEX = "replicaIndex";
    private static final String LOG_REQUEST_FINISHED = "finished";
    private static final String LOG_REQUEST_INSTANCE_KEY = "instanceKey";

    private static final String LOG_LINE_ENTITY_KIND = "__org.jboss.capedwarf.LogLine__";
    private static final String LOG_LINE_REQUEST_KEY = "requestKey";
    private static final String LOG_LINE_MILLIS = "millis";
    private static final String LOG_LINE_LEVEL = "level";
    private static final String LOG_LINE_MESSAGE = "message";
    private static final String LOG_LINE_LOGGER = "logger";
    private static final String LOG_LINE_THROWN = "thrown";

    private static final String LOG_REQUEST_ENTITY_REQUEST_ATTRIBUTE = "__org.jboss.capedwarf.LogRequest__";

    private static final String REQUEST_LOG_ENTITY = "com.google.appengine.runtime.request_log_entity";
    private static final String REQUEST_LOG_ID = "com.google.appengine.runtime.request_log_id";

    private final DatastoreService datastoreService;

    private final boolean ignoreLogging;
    private final LogWriter logWriter;

    public CapedwarfLogService() {
        datastoreService = DatastoreServiceFactory.getDatastoreService();
        Compatibility instance = CompatibilityUtils.getInstance();
        ignoreLogging = instance.isEnabled(Compatibility.Feature.IGNORE_LOGGING);
        // do we use async ds
        if (instance.isEnabled(Compatibility.Feature.ASYNC_LOGGING)) {
            logWriter = new LogWriter() {
                private final AsyncDatastoreService ads = DatastoreServiceFactory.getAsyncDatastoreService();

                public void put(Entity entity) {
                    ads.put(entity);
                }
            };
        } else {
            logWriter = new LogWriter() {
                public void put(Entity entity) {
                    datastoreService.put(entity);
                }
            };
        }
    }

    public Iterable<RequestLogs> fetch(LogQuery logQuery) {
        String ns = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            Compatibility.enable(Compatibility.Feature.DISABLE_QUERY_INEQUALITY_FILTER_CHECK);
            try {
                List<RequestLogs> list = new ArrayList<RequestLogs>();

                Map<Key, RequestLogs> map = fetchRequestLogs(logQuery, list);
                if (logQuery.getIncludeAppLogs()) {
                    fetchAppLogLines(logQuery, map);
                }
                return list;
            } finally {
                Compatibility.disable(Compatibility.Feature.DISABLE_QUERY_INEQUALITY_FILTER_CHECK);
            }
        } finally {
            NamespaceManager.set(ns);
        }
    }

    private Map<Key, RequestLogs> fetchRequestLogs(LogQuery logQuery, List<RequestLogs> list) {
        Map<Key, RequestLogs> map = new HashMap<Key, RequestLogs>();
        for (Entity entity : fetchRequestLogEntities(logQuery)) {
            RequestLogs requestLogs = convertEntityToRequestLogs(entity);
            map.put(entity.getKey(), requestLogs);
            list.add(requestLogs);
        }
        return map;
    }

    private List<Entity> fetchRequestLogEntities(LogQuery logQuery) {
        if (logQuery.getRequestIds().isEmpty()) {
            return datastoreService.prepare(createRequestLogsQuery(logQuery)).asList(withDefaults());
        } else {
            List<Entity> entities = new ArrayList<Entity>(logQuery.getRequestIds().size());
            for (String requestId : logQuery.getRequestIds()) {
                try {
                    entities.add(datastoreService.get(KeyFactory.createKey(LOG_REQUEST_ENTITY_KIND, Long.parseLong(requestId))));
                } catch (EntityNotFoundException ignored) {
                }
            }
            return entities;
        }
    }

    private RequestLogs convertEntityToRequestLogs(Entity entity) {
        RequestLogs requestLogs = new RequestLogs();
        Long startTimeMillis = (Long) entity.getProperty(LOG_REQUEST_START_TIME_MILLIS);
        if (startTimeMillis != null) {
            requestLogs.setStartTimeUsec(startTimeMillis * 1000);
        }
        Long endTimeMillis = (Long) entity.getProperty(LOG_REQUEST_END_TIME_MILLIS);
        if (endTimeMillis != null) {
            requestLogs.setEndTimeUsec(endTimeMillis * 1000);
        }

        requestLogs.setFinished(Boolean.TRUE.equals(entity.getProperty(LOG_REQUEST_FINISHED)));
        requestLogs.setMethod((String) entity.getProperty(LOG_REQUEST_METHOD));
        requestLogs.setHttpVersion((String) entity.getProperty(LOG_REQUEST_HTTP_VERSION));
        requestLogs.setHost((String) entity.getProperty(LOG_REQUEST_HOST));
        requestLogs.setResource((String) entity.getProperty(LOG_REQUEST_URI));
        requestLogs.setUserAgent((String) entity.getProperty(LOG_REQUEST_USER_AGENT));
        requestLogs.setReferrer((String)entity.getProperty(LOG_REQUEST_REFERRER));
        requestLogs.setRequestId(String.valueOf(entity.getKey().getId()));

        requestLogs.setAppId((String) entity.getProperty(LOG_REQUEST_APP_ID));
        requestLogs.setVersionId((String) entity.getProperty(LOG_REQUEST_VERSION_ID));
        requestLogs.setInstanceKey("");     // TODO
        requestLogs.setReplicaIndex(-1);    // TODO

        requestLogs.setPendingTime(0);      // TODO
        requestLogs.setLatency(0);          // TODO

//        requestLogs.setUrlMapEntry();

//        requestLogs.setTaskName();
//        requestLogs.setTaskQueueName();
//        requestLogs.setWasLoadingRequest();

        requestLogs.setNickname(emptyIfNull((String) entity.getProperty(LOG_REQUEST_NICKNAME)));
        requestLogs.setIp((String) entity.getProperty(LOG_REQUEST_IP));


        requestLogs.setCost(0);
        requestLogs.setMcycles(0);
        requestLogs.setApiMcycles(0);

        Long status = (Long) entity.getProperty(LOG_REQUEST_STATUS);
        requestLogs.setStatus(status == null ? 0 : (int)(long)status);

        Long responseSize = (Long) entity.getProperty(LOG_REQUEST_RESPONSE_SIZE);
        requestLogs.setResponseSize(responseSize == null ? 0 : (int)(long)responseSize);

        // combined='93.103.26.101 - - [17/Jan/2013:08:07:11 -0800] "GET /favicon.ico HTTP/1.1" 404 0 - "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.52 Safari/537.17"',
        requestLogs.setCombined(
            requestLogs.getIp() + " - " + requestLogs.getNickname() + (requestLogs.getNickname().isEmpty() ? "" : " ")
                + "- [" + DATE_FORMAT.format(requestLogs.getStartTimeUsec() / 1000L) + "] \""
                + requestLogs.getMethod() + " " + requestLogs.getResource() + " " + requestLogs.getHttpVersion() + "\" "
                + requestLogs.getStatus() + " " + requestLogs.getResponseSize() + " - \"" + requestLogs.getUserAgent() + "\""
        );

//        requestLogs.setOffset();  TODO
        return requestLogs;
    }

    private String emptyIfNull(String str) {
        return str == null ? "" : str;
    }

    private Query createRequestLogsQuery(LogQuery logQuery) {
        List<Query.Filter> filters = new LinkedList<Query.Filter>();
        if (logQuery.getStartTimeMillis() != null) {
            filters.add(new Query.FilterPredicate(LOG_REQUEST_END_TIME_MILLIS, GREATER_THAN_OR_EQUAL, logQuery.getStartTimeMillis()));
        }
        if (logQuery.getEndTimeMillis() != null) {
            filters.add(new Query.FilterPredicate(LOG_REQUEST_END_TIME_MILLIS, LESS_THAN_OR_EQUAL, logQuery.getEndTimeMillis()));
        }
        if (logQuery.getMinLogLevel() != null) {
            filters.add(new Query.FilterPredicate(LOG_REQUEST_MAX_LOG_LEVEL, GREATER_THAN_OR_EQUAL, logQuery.getMinLogLevel().ordinal()));
        }

        boolean onlyCompleteRequests = !Boolean.TRUE.equals(logQuery.getIncludeIncomplete());
        if (onlyCompleteRequests) {
            filters.add(new Query.FilterPredicate(LOG_REQUEST_FINISHED, EQUAL, Boolean.TRUE));
        }

        Query query = new Query(LOG_REQUEST_ENTITY_KIND);
        addFilters(query, filters);
        query.addSort(LOG_REQUEST_END_TIME_MILLIS);
        return query;
    }

    private void addFilters(Query query, List<Query.Filter> filters) {
        if (filters.size() == 1) {
            query.setFilter(filters.get(0));
        } else if (filters.size() > 1) {
            query.setFilter(Query.CompositeFilterOperator.and(filters));
        }
    }

    private void fetchAppLogLines(LogQuery logQuery, Map<Key, RequestLogs> map) {
        Query query = createAppLogLinesQuery(logQuery);
        FetchOptions fetchOptions = createAppLogFetchOptions(logQuery);

        List<Entity> entities = datastoreService.prepare(query).asList(fetchOptions);
        for (Entity entity : entities) {
            AppLogLine logLine = new AppLogLine();
            logLine.setLogLevel(LogLevel.values()[((Number) entity.getProperty(LOG_LINE_LEVEL)).intValue()]);
            logLine.setLogMessage((String) entity.getProperty(LOG_LINE_MESSAGE));
            logLine.setTimeUsec((Long) entity.getProperty(LOG_LINE_MILLIS));

            RequestLogs requestLogs = map.get((Key) entity.getProperty(LOG_LINE_REQUEST_KEY));
            if (requestLogs != null) {
                requestLogs.getAppLogLines().add(logLine);
            }
        }
    }

    private Query createAppLogLinesQuery(LogQuery logQuery) {
        List<Query.Filter> filters = new ArrayList<Query.Filter>();

        Query query = new Query(LOG_LINE_ENTITY_KIND);
        if (logQuery.getStartTimeUsec() != null) {
            filters.add(new Query.FilterPredicate(LOG_LINE_MILLIS, GREATER_THAN_OR_EQUAL, logQuery.getStartTimeMillis()));
        }
        if (logQuery.getEndTimeUsec() != null) {
            filters.add(new Query.FilterPredicate(LOG_LINE_MILLIS, LESS_THAN_OR_EQUAL, logQuery.getEndTimeMillis()));
        }

        addFilters(query, filters);
        query.addSort(LOG_LINE_MILLIS);
        return query;
    }

    private FetchOptions createAppLogFetchOptions(LogQuery logQuery) {
        FetchOptions fetchOptions = withDefaults();
        if (logQuery.getBatchSize() != null) {
            fetchOptions = fetchOptions.chunkSize(logQuery.getBatchSize());
        }
        return fetchOptions;
    }

    public void log(LogRecord record) {
        // did we disable logging
        if (ignoreLogging)
            return;

        String ns = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            CapedwarfDelegate capedwarfDelegate = CapedwarfDelegate.INSTANCE;
            ServletRequest request = capedwarfDelegate.getServletRequest();
            LogLevel logLevel = getLogLevel(record);
            String formattedMessage = new MessageFormat(record.getMessage()).format(record.getParameters());

            Entity entity = new Entity(LOG_LINE_ENTITY_KIND);
            entity.setProperty(LOG_LINE_LOGGER, record.getLoggerName());
            entity.setProperty(LOG_LINE_LEVEL, logLevel.ordinal());
            entity.setProperty(LOG_LINE_MILLIS, record.getMillis());
            entity.setProperty(LOG_LINE_THROWN, record.getThrown());
            entity.setProperty(LOG_LINE_MESSAGE, formattedMessage);
            entity.setProperty(LOG_LINE_REQUEST_KEY, getRequestEntityKey(request));

            logWriter.put(entity);

            CapedwarfEnvironment environment = CapedwarfEnvironment.getThreadLocalInstance();
            Entity requestEntity = (Entity) environment.getAttributes().get(REQUEST_LOG_ENTITY);

            Long maxLogLevelOrdinal = (Long) requestEntity.getProperty(LOG_REQUEST_MAX_LOG_LEVEL);
            if (maxLogLevelOrdinal == null || logLevel.ordinal() > maxLogLevelOrdinal) {
                requestEntity.setProperty(LOG_REQUEST_MAX_LOG_LEVEL, (long) logLevel.ordinal());
            }
            requestEntity.setProperty(LOG_REQUEST_END_TIME_MILLIS, System.currentTimeMillis());
            datastoreService.put(requestEntity);

        } finally {
            NamespaceManager.set(ns);
        }
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

        Entity entity = new Entity(LOG_REQUEST_ENTITY_KIND);
        entity.setProperty(LOG_REQUEST_START_TIME_MILLIS, startTimeMillis);
        entity.setProperty(LOG_REQUEST_END_TIME_MILLIS, startTimeMillis);
        entity.setProperty(LOG_REQUEST_FINISHED, false);

        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String queryString = request.getQueryString();
            entity.setProperty(LOG_REQUEST_URI, request.getRequestURI() + (queryString == null ? "" : ("?" + queryString)));
            entity.setProperty(LOG_REQUEST_USER_AGENT, request.getHeader("User-Agent"));
            entity.setProperty(LOG_REQUEST_METHOD, request.getMethod());
            entity.setProperty(LOG_REQUEST_REFERRER, request.getHeader("referer"));
            entity.setProperty(LOG_REQUEST_HOST, request.getServerName() + (request.getServerPort() == 80 ? "" : (":" + request.getServerPort())));
        }
        entity.setProperty(LOG_REQUEST_HTTP_VERSION, servletRequest.getProtocol());
        entity.setProperty(LOG_REQUEST_IP, servletRequest.getRemoteAddr());
        entity.setProperty(LOG_REQUEST_APP_ID, environment.getAppId());
        entity.setProperty(LOG_REQUEST_VERSION_ID, environment.getVersionId());

        Key key = datastoreService.put(entity);
        servletRequest.setAttribute(LOG_REQUEST_ENTITY_REQUEST_ATTRIBUTE, entity);

        environment.getAttributes().put(REQUEST_LOG_ENTITY, entity);
        environment.getAttributes().put(REQUEST_LOG_ID, String.valueOf(key.getId()));
    }

    public void requestFinished(ServletRequest servletRequest, int status, int contentLength) {
        Entity entity = getRequestEntity(servletRequest);
        // check if all went well
        if (entity != null) {
            entity.setProperty(LOG_REQUEST_END_TIME_MILLIS, System.currentTimeMillis());
            entity.setProperty(LOG_REQUEST_STATUS, status);
            entity.setProperty(LOG_REQUEST_RESPONSE_SIZE, contentLength);
            entity.setProperty(LOG_REQUEST_FINISHED, true);
            datastoreService.put(entity);
        }
    }

    private Entity getRequestEntity(ServletRequest request) {
        return (Entity) request.getAttribute(LOG_REQUEST_ENTITY_REQUEST_ATTRIBUTE);
    }

    private Key getRequestEntityKey(ServletRequest request) {
        return getRequestEntity(request).getKey();
    }

    public void clearLog() {
        Query query = new Query(LOG_LINE_ENTITY_KIND).setKeysOnly();
        Iterable<Entity> entities = datastoreService.prepare(query).asIterable();
        for (Entity entity : entities) {
            datastoreService.delete(entity.getKey());
        }
    }

    private static interface LogWriter {
        void put(Entity entity);
    }
}
