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

package org.jboss.test.capedwarf.testsuite.callbacks.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public abstract class QueryCallbacksTestBase extends CallbacksTestBase {
    public static final String POST_LOAD = "PostLoad";
    protected final int N = 5;
    protected final String[] states = getPostLoadStates(N);

    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment().addClass(QueryCallbacksTestBase.class);
    }

    @Before
    public void setUp() {
        super.setUp();

        DatastoreService service = createDatastoreService();
        int n = N;
        while (n > 0) {
            Entity e = new Entity(KIND);
            e.setProperty("x", n);
            service.put(e);
            n--;
        }
    }

    protected void assertCallbackInvokedFully() {
        assertCallbackInvoked(states);
    }

    protected void assertPostLoadCallbackInvokedTimes(int num) {
        assertCallbackInvoked(getPostLoadStates(num));
    }

    protected static String[] getPostLoadStates(int num) {
        List<String> states = new ArrayList<String>();
        while(num > 0) {
            states.add(POST_LOAD);
            num--;
        }
        return states.toArray(new String[states.size()]);
    }

    protected List<Entity> asList(FetchOptions options) {
        DatastoreService service = createDatastoreService();

        PreparedQuery pq = service.prepare(new Query(KIND));
        reset();
        List<Entity> list = pq.asList(options);
        assertNoCallbackInvoked();
        return list;
    }

    protected Iterator<Entity> asIterator(FetchOptions options) {
        DatastoreService service = createDatastoreService();

        PreparedQuery pq = service.prepare(new Query(KIND));
        reset();
        Iterator<Entity> iterator = pq.asIterator(options);
        assertNoCallbackInvoked();
        return iterator;
    }
}
