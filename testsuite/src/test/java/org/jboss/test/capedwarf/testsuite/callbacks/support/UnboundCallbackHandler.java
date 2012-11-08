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

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class UnboundCallbackHandler {

    public static List<String> states = new ArrayList<String>();

    @PrePut
    public void prePut(PutContext context) {
        states.add("PrePut");
    }

    @PostPut
    public void postPut(PutContext context) {
        states.add("PostPut");
    }

    @PreGet
    public void preGet(PreGetContext context) {
        states.add("PreGet");
    }

    @PostLoad
    public void postLoad(PostLoadContext context) {
        states.add("PostLoad");
    }

    @PreQuery
    public void preQuery(PreQueryContext context) {
        states.add("PreQuery");
    }

    @PreDelete
    public void preDelete(DeleteContext context) {
        states.add("PreDelete");
    }

    @PostDelete
    public void postDelete(DeleteContext context) {
        states.add("PostDelete");
    }
}
