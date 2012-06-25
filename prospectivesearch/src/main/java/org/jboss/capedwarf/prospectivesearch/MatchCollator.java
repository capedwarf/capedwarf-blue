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

import com.google.appengine.api.prospectivesearch.Subscription;
import org.infinispan.distexec.mapreduce.Collator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
class MatchCollator implements Collator<String, List<Subscription>, List<Subscription>> {
    public List<Subscription> collate(Map<String, List<Subscription>> reducedResults) {
        List<Subscription> subscriptions = reducedResults.get(MatchMapper.KEY);
        return subscriptions == null ? Collections.<Subscription>emptyList() : subscriptions;
    }

}
