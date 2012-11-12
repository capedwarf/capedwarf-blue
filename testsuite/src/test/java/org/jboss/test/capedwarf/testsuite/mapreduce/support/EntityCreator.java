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

package org.jboss.test.capedwarf.testsuite.mapreduce.support;

import java.util.Random;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.Mapper;

/**
 * From Google AppEngine MapReduce Examples.
 *
 * @author ohler@google.com (Christian Ohler)
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EntityCreator extends Mapper<Long, Void, Void> {
    private static final long serialVersionUID = 1L;

    private final String kind;
    private final int payloadBytesPerEntity;
    private final Random random = new Random();
    private transient DatastoreMutationPool pool;

    public EntityCreator(String kind, int payloadBytesPerEntity) {
        if (kind == null)
            throw new IllegalArgumentException("Null kind");

        this.kind = kind;
        this.payloadBytesPerEntity = payloadBytesPerEntity;
    }

    private String randomString(int length) {
        StringBuilder out = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            out.append((char) ('a' + random.nextInt(26)));
        }
        return out.toString();
    }

    @Override
    public void beginShard() {
        pool = DatastoreMutationPool.forWorker(this);
    }

    public void map(Long ignored) {
        String name = "" + (random.nextLong() & Long.MAX_VALUE);
        Entity e = new Entity(kind, name);
        e.setProperty("payload", new Text(randomString(payloadBytesPerEntity)));
        pool.put(e);
    }
}