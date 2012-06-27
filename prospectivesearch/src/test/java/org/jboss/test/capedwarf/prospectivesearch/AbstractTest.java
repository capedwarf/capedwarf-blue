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

package org.jboss.test.capedwarf.prospectivesearch;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.prospectivesearch.FieldType;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchService;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchServiceFactory;
import com.google.appengine.api.prospectivesearch.Subscription;
import org.jboss.capedwarf.prospectivesearch.CapedwarfProspectiveSearchService;
import org.junit.After;
import org.junit.Before;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public abstract class AbstractTest {

    protected ProspectiveSearchService service;

    @Before
    public void setUp() {
        service = ProspectiveSearchServiceFactory.getProspectiveSearchService();
    }

    @After
    public void tearDown() throws Exception {
        ((CapedwarfProspectiveSearchService)service).clear();
    }

    protected void sortBySubId(List<Subscription> subscriptions) {
        Collections.sort(subscriptions, new Comparator<Subscription>() {
            public int compare(Subscription o1, Subscription o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
    }

    protected Map<String, FieldType> createSchema(String field, FieldType type) {
        Map<String, FieldType> schema = new HashMap<String, FieldType>();
        schema.put(field, type);
        return schema;
    }

    protected Map<String, FieldType> createSchema(String field1, FieldType type1, String field2, FieldType type2) {
        Map<String, FieldType> schema = new HashMap<String, FieldType>();
        schema.put(field1, type1);
        schema.put(field2, type2);
        return schema;
    }
}
