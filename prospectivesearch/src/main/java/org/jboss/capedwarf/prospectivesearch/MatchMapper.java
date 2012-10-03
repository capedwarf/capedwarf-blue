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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.miscellaneous.PatternAnalyzer;
import org.apache.lucene.index.memory.MemoryIndex;
import org.infinispan.distexec.mapreduce.Collector;
import org.infinispan.distexec.mapreduce.Mapper;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.prospectivesearch.Subscription;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
class MatchMapper implements Mapper<TopicAndSubId, SubscriptionHolder, String, List<Subscription>> {
    private final String topic;

    public static final String KEY = "result";
    private Map<String, String> map = new HashMap<String, String>();
    private transient MemoryIndex memoryIndex;

    public MatchMapper(String topic, Entity entity) {
        this.topic = topic;

        for (Map.Entry<String, Object> entry : entity.getProperties().entrySet()) {
            map.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
    }

    private MemoryIndex getMemoryIndex() {
        if (memoryIndex == null) {
            memoryIndex = new MemoryIndex();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                memoryIndex.addField(entry.getKey(), entry.getValue(), PatternAnalyzer.DEFAULT_ANALYZER);
            }
        }
        return memoryIndex;
    }

    public void map(TopicAndSubId key, SubscriptionHolder value, Collector<String, List<Subscription>> collector) {
        if (key.getTopic().equals(topic)) {
            float score = getMemoryIndex().search(value.getLuceneQuery());
            if (score > 0.0f) {
                collector.emit(KEY, Collections.singletonList(value.toSubscription()));
            }
        }
    }
}
