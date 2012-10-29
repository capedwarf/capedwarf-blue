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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.prospectivesearch.Subscription;
import org.infinispan.distexec.mapreduce.Collator;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
* @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
*/
class MatchCollator implements Collator<String, List<SerializableSubscription>, List<Subscription>> {

    public List<Subscription> collate(Map<String, List<SerializableSubscription>> reducedResults) {
        List<SerializableSubscription> subscriptions = reducedResults.get(MatchMapper.KEY);
        if (subscriptions == null || subscriptions.isEmpty()) {
            return Collections.emptyList();
        } else {
            final List<Subscription> results = new ArrayList<Subscription>();
            for (SerializableSubscription ss : subscriptions) {
                results.add(ss.getSubscription());
            }
            return results;
        }
    }

}
