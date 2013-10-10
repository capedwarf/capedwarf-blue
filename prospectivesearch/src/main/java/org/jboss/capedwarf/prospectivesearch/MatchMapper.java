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

import com.google.appengine.api.datastore.Entity;
import org.apache.lucene.index.memory.MemoryIndex;
import org.infinispan.distexec.mapreduce.Collector;
import org.infinispan.distexec.mapreduce.Mapper;
import org.jboss.capedwarf.search.CacheValue;
import org.jboss.capedwarf.search.DocumentFieldAnalyzer;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
* @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
*/
class MatchMapper implements Mapper<TopicAndSubId, SubscriptionHolder, String, List<SerializableSubscription>> {
    private final String topic;

    public static final String KEY = "result";
    private Map<String, Object> fields = new HashMap<>();
    private transient MemoryIndex memoryIndex;

    public MatchMapper(String topic, Entity entity) {
        this.topic = topic;

        for (Map.Entry<String, Object> entry : entity.getProperties().entrySet()) {
            fields.put(entry.getKey(), entry.getValue());
        }
    }

    private synchronized MemoryIndex getMemoryIndex() {
        if (memoryIndex == null) {
            memoryIndex = new MemoryIndex();
            StringBuilder allFieldValue = new StringBuilder();   // since MemoryIndex does not support adding multiple fields with same name, we have to concatenate values and store them under a single field
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Number) {
                    memoryIndex.addField(entry.getKey(), DoubleBridge.INSTANCE.objectToString(value), DocumentFieldAnalyzer.PASS_THROUGH_ANALYZER);
                } else {
                    String string = String.valueOf(value);
                    memoryIndex.addField(entry.getKey(), string, DocumentFieldAnalyzer.STANDARD_ANALYZER);
                    allFieldValue.append(string);
                }
            }
            memoryIndex.addField(CacheValue.ALL_FIELD_NAME, allFieldValue.toString(), DocumentFieldAnalyzer.STANDARD_ANALYZER);
            memoryIndex.addField(CacheValue.MATCH_ALL_DOCS_FIELD_NAME, CacheValue.MATCH_ALL_DOCS_FIELD_VALUE, DocumentFieldAnalyzer.PASS_THROUGH_ANALYZER);
        }
        return memoryIndex;
    }

    public void map(TopicAndSubId key, SubscriptionHolder value, Collector<String, List<SerializableSubscription>> collector) {
        if (key.getTopic().equals(topic)) {
            float score = getMemoryIndex().search(value.getLuceneQuery());
            if (score > 0.0f) {
                collector.emit(KEY, Collections.singletonList(new SerializableSubscription(value.toSubscription())));
            }
        }
    }
}
