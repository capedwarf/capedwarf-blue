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

package org.jboss.capedwarf.admin;

import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import org.jboss.capedwarf.log.CapedwarfLogQuery;
import org.jboss.capedwarf.log.CapedwarfLogQueryResult;
import org.jboss.capedwarf.log.ExposedLogService;
import org.jboss.capedwarf.log.LogQueryOptions;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@Named("logViewer")
@RequestScoped
public class LogViewer {

    private static final int DEFAULT_ROWS_PER_PAGE = 20;

    @Inject @HttpParam
    private String show;

    @Inject @HttpParam
    private String severity;

    @Inject @HttpParam
    private String page;

    private long resultCount;

    public boolean isShowAll() {
        return show == null || show.equals("all");
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public List<String> getSeverities() {
        List<String> list = new ArrayList<String>();
        for (LogService.LogLevel level : LogService.LogLevel.values()) {
            list.add(level.name());
        }
        return list;
    }

    public Iterable<RequestLogs> getRequestLogs() {
        LogQuery logQuery = new LogQuery();
        if (!isShowAll()) {
            logQuery = logQuery.minLogLevel(LogService.LogLevel.valueOf(severity));
        }
        // TODO: add other fields to filter
        LogQueryOptions options = new LogQueryOptions(getOffset(), getLimit());
        CapedwarfLogQuery query = new CapedwarfLogQuery(logQuery, options);
        CapedwarfLogQueryResult result = getLogService().fetch(query);
        resultCount = result.getResultCount();
        return result;
    }

    private ExposedLogService getLogService() {
        return (ExposedLogService)LogServiceFactory.getLogService();
    }

    public int getOffset() {
        return (getCurrentPage()-1) * DEFAULT_ROWS_PER_PAGE;
    }

    public int getLimit() {
        return DEFAULT_ROWS_PER_PAGE;
    }

    public long getNumberOfPages() {
        return ((resultCount - 1) / DEFAULT_ROWS_PER_PAGE) + 1;
    }

    public int getCurrentPage() {
        return page == null ? 1 : Integer.parseInt(page);
    }

}
