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

package org.jboss.test.capedwarf.testsuite.callbacks.support;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DeleteContext;
import com.google.appengine.api.datastore.PostDelete;
import com.google.appengine.api.datastore.PostLoad;
import com.google.appengine.api.datastore.PostLoadContext;
import com.google.appengine.api.datastore.PostPut;
import com.google.appengine.api.datastore.PreDelete;
import com.google.appengine.api.datastore.PreGet;
import com.google.appengine.api.datastore.PreGetContext;
import com.google.appengine.api.datastore.PrePut;
import com.google.appengine.api.datastore.PreQuery;
import com.google.appengine.api.datastore.PreQueryContext;
import com.google.appengine.api.datastore.PutContext;
import org.jboss.test.capedwarf.testsuite.callbacks.test.CallbacksTestBase;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class BarKindCallbackHandler {

    public static List<String> states = new ArrayList<String>();

    @PrePut(kinds = {CallbacksTestBase.KIND2})
    public void prePut(PutContext context) {
        states.add("PrePut");
        System.out.println("context = " + context);
    }

    @PostPut(kinds = {CallbacksTestBase.KIND2})
    public void postPut(PutContext context) {
        states.add("PostPut");
        System.out.println("context = " + context);
    }

    @PreGet(kinds = {CallbacksTestBase.KIND2})
    public void preGet(PreGetContext context) {
        states.add("PreGet");
        System.out.println("context = " + context);
    }

    @PostLoad(kinds = {CallbacksTestBase.KIND2})
    public void postLoad(PostLoadContext context) {
        states.add("PostLoad");
        System.out.println("context = " + context);
    }

    @PreQuery(kinds = {CallbacksTestBase.KIND2})
    public void preQuery(PreQueryContext context) {
        states.add("PreQuery");
        System.out.println("context = " + context);
    }

    @PreDelete(kinds = {CallbacksTestBase.KIND2})
    public void preDelete(DeleteContext context) {
        states.add("PreDelete");
        System.out.println("context = " + context);
    }

    @PostDelete(kinds = {CallbacksTestBase.KIND2})
    public void postDelete(DeleteContext context) {
        states.add("PostDelete");
        System.out.println("context = " + context);
    }
}
