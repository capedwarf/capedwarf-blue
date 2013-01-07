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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.RequestLogs;
import org.jboss.capedwarf.common.apiproxy.CapedwarfDelegate;
import org.jboss.capedwarf.common.compatibility.Compatibility;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfLogService implements LogService, Logable {

    private static final String LOG_REQUEST_ENTITY_KIND = "__org.jboss.capedwarf.LogRequest__";
    private static final String LOG_REQUEST_START_TIME_MILLIS = "startTimeMillis";
    private static final String LOG_REQUEST_END_TIME_MILLIS = "endTimeMillis";
    private static final String LOG_REQUEST_URI = "uri";
    private static final String LOG_REQUEST_USER_AGENT = "userAgent";

    private static final String LOG_LINE_ENTITY_KIND = "__org.jboss.capedwarf.LogLine__";
    private static final String LOG_LINE_REQUEST_KEY = "requestKey";
    private static final String LOG_LINE_MILLIS = "millis";
    private static final String LOG_LINE_LEVEL = "level";
    private static final String LOG_LINE_MESSAGE = "message";
    private static final String LOG_LINE_LOGGER = "logger";
    private static final String LOG_LINE_THROWN = "thrown";

    private static final String LOG_REQUEST_ENTITY_REQUEST_ATTRIBUTE = "__org.jboss.capedwarf.LogRequest__";

    private boolean ignoreLogging = Compatibility.getInstance().isEnabled(Compatibility.Feature.IGNORE_LOGGING);

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
                if (logQuery.getMinLogLevel() != null) {
                    removeRequestsWithNoLogLines(list);
                }
                return list;
            } finally {
                Compatibility.disable(Compatibility.Feature.DISABLE_QUERY_INEQUALITY_FILTER_CHECK);
            }
        } finally {
            NamespaceManager.set(ns);
        }
    }

    private void removeRequestsWithNoLogLines(List<RequestLogs> list) {
        for (Iterator<RequestLogs> iterator = list.iterator(); iterator.hasNext(); ) {
            RequestLogs requestLogs = iterator.next();
            if (requestLogs.getAppLogLines().isEmpty()) {
                iterator.remove();
            }
        }
    }

    private Map<Key, RequestLogs> fetchRequestLogs(LogQuery logQuery, List<RequestLogs> list) {
        Map<Key, RequestLogs> map = new HashMap<Key, RequestLogs>();

        Query query = createRequestLogsQuery(logQuery);
        FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

        List<Entity> entities = DatastoreServiceFactory.getDatastoreService().prepare(query).asList(fetchOptions);
        for (Entity entity : entities) {
            RequestLogs requestLogs = new RequestLogs();
            Long startTimeUsec = (Long) entity.getProperty(LOG_REQUEST_START_TIME_MILLIS);
            if (startTimeUsec != null) {
                requestLogs.setStartTimeUsec(startTimeUsec);
            }
            Long endTimeUsec = (Long) entity.getProperty(LOG_REQUEST_END_TIME_MILLIS);
            if (endTimeUsec == null) {
                requestLogs.setFinished(false);
            } else {
                requestLogs.setEndTimeUsec(endTimeUsec);
                requestLogs.setPendingTime(requestLogs.getEndTimeUsec() - requestLogs.getStartTimeUsec());
            }
            requestLogs.setResource((String) entity.getProperty(LOG_REQUEST_URI));
            requestLogs.setUserAgent((String) entity.getProperty(LOG_REQUEST_USER_AGENT));
            // TODO: set all other properties

            map.put(entity.getKey(), requestLogs);
            list.add(requestLogs);
        }
        return map;
    }

    private Query createRequestLogsQuery(LogQuery logQuery) {
        List<Query.Filter> filters = new LinkedList<Query.Filter>();
        if (logQuery.getStartTimeUsec() != null) {
            filters.add(new Query.FilterPredicate(LOG_REQUEST_END_TIME_MILLIS, Query.FilterOperator.GREATER_THAN_OR_EQUAL, logQuery.getStartTimeUsec()));
        }
        if (logQuery.getEndTimeUsec() != null) {
            filters.add(new Query.FilterPredicate(LOG_REQUEST_END_TIME_MILLIS, Query.FilterOperator.LESS_THAN_OR_EQUAL, logQuery.getEndTimeUsec()));
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

        List<Entity> entities = DatastoreServiceFactory.getDatastoreService().prepare(query).asList(fetchOptions);
        for (Entity entity : entities) {
            AppLogLine logLine = new AppLogLine();
            logLine.setLogLevel(LogLevel.values()[((Number) entity.getProperty(LOG_LINE_LEVEL)).intValue()]);
            logLine.setLogMessage((String) entity.getProperty(LOG_LINE_MESSAGE));
            logLine.setTimeUsec((Long) entity.getProperty(LOG_LINE_MILLIS));

            RequestLogs requestLogs = map.get((Key) entity.getProperty(LOG_LINE_REQUEST_KEY));
            requestLogs.getAppLogLines().add(logLine);
        }
    }

    private Query createAppLogLinesQuery(LogQuery logQuery) {
        List<Query.Filter> filters = new ArrayList<Query.Filter>();

        Query query = new Query(LOG_LINE_ENTITY_KIND);
        if (logQuery.getMinLogLevel() != null) {
            filters.add(new Query.FilterPredicate(LOG_LINE_LEVEL, Query.FilterOperator.GREATER_THAN_OR_EQUAL, logQuery.getMinLogLevel().ordinal()));
        }
        if (logQuery.getStartTimeUsec() != null) {
            filters.add(new Query.FilterPredicate(LOG_LINE_MILLIS, Query.FilterOperator.GREATER_THAN_OR_EQUAL, logQuery.getStartTimeUsec()));
        }
        if (logQuery.getEndTimeUsec() != null) {
            filters.add(new Query.FilterPredicate(LOG_LINE_MILLIS, Query.FilterOperator.LESS_THAN_OR_EQUAL, logQuery.getEndTimeUsec()));
        }

        addFilters(query, filters);
        query.addSort(LOG_LINE_MILLIS);
        return query;
    }

    private FetchOptions createAppLogFetchOptions(LogQuery logQuery) {
        FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
        if (logQuery.getBatchSize() != null) {
            fetchOptions = fetchOptions.limit(logQuery.getBatchSize());
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

            Entity entity = new Entity(LOG_LINE_ENTITY_KIND);
            entity.setProperty(LOG_LINE_LOGGER, record.getLoggerName());
            entity.setProperty(LOG_LINE_LEVEL, getLogLevel(record).ordinal());
            entity.setProperty(LOG_LINE_MILLIS, record.getMillis());
            entity.setProperty(LOG_LINE_THROWN, record.getThrown());
            entity.setProperty(LOG_LINE_MESSAGE, record.getMessage()); // TODO: format message
            entity.setProperty(LOG_LINE_REQUEST_KEY, getRequestEntityKey(request));

            DatastoreServiceFactory.getDatastoreService().put(entity); // TODO -- async
        } finally {
            NamespaceManager.set(ns);
        }
    }

    private LogLevel getLogLevel(LogRecord record) {
        // TODO
        if (record.getLevel().equals(Level.INFO)) {
            return LogLevel.INFO;
        } else if (record.getLevel().equals(Level.WARNING)) {
            return LogLevel.WARN;
        } else if (record.getLevel().equals(Level.SEVERE)) {
            return LogLevel.ERROR;
        } else if (record.getLevel().equals(Level.FINE)) {
            return LogLevel.DEBUG;
        } else {
            return LogLevel.INFO;
        }
    }

    public void requestStarted(ServletRequest servletRequest, long startTimeMillis) {
        if (ignoreLogging) {
            return;
        }

        Entity entity = new Entity(LOG_REQUEST_ENTITY_KIND);
        entity.setProperty(LOG_REQUEST_START_TIME_MILLIS, startTimeMillis);

        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            entity.setProperty(LOG_REQUEST_URI, request.getRequestURI());
            entity.setProperty(LOG_REQUEST_USER_AGENT, request.getHeader("User-Agent"));
        }

        DatastoreServiceFactory.getDatastoreService().put(entity);
        servletRequest.setAttribute(LOG_REQUEST_ENTITY_REQUEST_ATTRIBUTE, entity);
    }

    public void requestFinished(ServletRequest servletRequest) {
        Entity entity = getRequestEntity(servletRequest);
        // check if all went well
        if (entity != null) {
            entity.setProperty(LOG_REQUEST_END_TIME_MILLIS, System.currentTimeMillis());

    //            HttpServletResponse response;
            // TODO entity.setProperty("responseStatusCode", response.getStatus());
            // TODO entity.setProperty("responseLength", );
        }
    }

    private Entity getRequestEntity(ServletRequest request) {
        return (Entity) request.getAttribute(LOG_REQUEST_ENTITY_REQUEST_ATTRIBUTE);
    }

    private Key getRequestEntityKey(ServletRequest request) {
        return getRequestEntity(request).getKey();
    }

    public void clearLog() {
        final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query(LOG_LINE_ENTITY_KIND).setKeysOnly();
        Iterable<Entity> entities = ds.prepare(query).asIterable();
        for (Entity entity : entities) {
            ds.delete(entity.getKey());
        }
    }
}
