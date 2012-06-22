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
import org.infinispan.distexec.mapreduce.Reducer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
class MatchReducer implements Reducer {

    public Object reduce(Object reducedKey, Iterator iter) {
        return reduce((String) reducedKey, (Iterator<List<Subscription>>) iter);
    }
    public List<Subscription> reduce(String reducedKey, Iterator<List<Subscription>> iter) {
        List<Subscription> list = new ArrayList<Subscription>();
        while (iter.hasNext()) {
            List<Subscription> topics = iter.next();
            list.addAll(topics);
        }
        return list;
    }

}
