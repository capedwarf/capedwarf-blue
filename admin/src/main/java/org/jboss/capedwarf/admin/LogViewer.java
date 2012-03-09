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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@Named("logViewer")
@RequestScoped
public class LogViewer {

    @Inject @HttpParam
    private String show;

    @Inject @HttpParam
    private String severity;

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
        return LogServiceFactory.getLogService().fetch(logQuery);
    }

}
