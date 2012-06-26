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

package org.jboss.capedwarf.prospectivesearch;

import java.util.Collections;
import java.util.Set;

import org.infinispan.distexec.mapreduce.Collector;
import org.infinispan.distexec.mapreduce.Mapper;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
class TopicsMapper implements Mapper {

    public static final String KEY = "topics";

    public void map(Object key, Object value, Collector collector) {
        if (key instanceof TopicAndSubId && value instanceof SubscriptionHolder) {
            //noinspection unchecked
            map((TopicAndSubId) key, (SubscriptionHolder) value, (Collector<String, Set<String>>) collector);
        }
    }

    public void map(TopicAndSubId key, SubscriptionHolder value, Collector<String, Set<String>> collector) {
        collector.emit(KEY, Collections.singleton(value.getTopic()));
    }
}
