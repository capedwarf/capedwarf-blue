/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.test.capedwarf.datastore.test;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class TransactionsTestCase extends AbstractTest {

    @Test
    public void testBasicTxOps() throws Exception {
        Entity entity = createTestEntity();
        Transaction tx = service.beginTransaction();
        try {
            service.put(tx, entity);
            assertStoreContains(entity);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
    }

    @Test
    public void testRollback() throws Exception {
        Entity entity = createTestEntity();
        Transaction tx = service.beginTransaction();
        Key key = service.put(tx, entity);
        assertStoreContains(entity);
        tx.rollback();
        Object result = service.get(key);
        Assert.assertNotNull(result); // should not be there due to rollback
    }
}
