/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.log.RequestLogs;
import com.google.appengine.repackaged.com.google.common.base.Function;
import com.google.appengine.repackaged.com.google.common.collect.Iterators;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class CapedwarfLogQueryResult implements Iterable<RequestLogs> {

    private long resultCount;
    private final List<CapedwarfRequestLogs> requestLogs;

    public CapedwarfLogQueryResult(List<CapedwarfRequestLogs> requestLogs, long resultCount) {
        this.requestLogs = requestLogs;
        this.resultCount = resultCount;
    }

    public long getResultCount() {
        return resultCount;
    }

    @Override
    public Iterator<RequestLogs> iterator() {
        return Iterators.transform(requestLogs.iterator(), new ExtractRequestLogs());
    }

    public Iterable<? extends CapedwarfRequestLogs> getCapedwarfRequestLogs() {
        return requestLogs;
    }

    private static class ExtractRequestLogs implements Function<CapedwarfRequestLogs, RequestLogs> {
        @Override
        public RequestLogs apply(CapedwarfRequestLogs capedwarfRequestLogs) {
            return capedwarfRequestLogs.getRequestLogs();
        }
    }
}
