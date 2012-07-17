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
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

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
            assertStoreContains(entity);
        } catch (Exception e) {
            tx.rollback();
        }
    }

    @Test
    public void testRollbackWhenPuttingEntity() throws Exception {
        Entity entity = createTestEntity("ROLLBACK", 1);
        Transaction tx = service.beginTransaction();
        service.put(tx, entity);
        tx.rollback();
        // should not be there due to rollback
        assertStoreDoesNotContain(entity);
    }

    @Test
    public void testRollbackWhenModifyingEntity() throws Exception {
        Entity entity = new Entity("test");
        entity.setProperty("name", "original");
        Key key = service.put(entity);

        Transaction tx = service.beginTransaction();
        Entity entity2 = service.get(key);
        entity2.setProperty("name", "modified");
        tx.rollback();

        Entity entity3 = service.get(key);
        assertEquals("original", entity3.getProperty("name"));
    }

    @Test
    public void testNoIdKey() throws Exception {
        Entity entity = new Entity("NO_ID");
        Key key = service.put(entity);
        assertTrue(key.isComplete());
    }

    @Test
    public void testNested() throws Exception {
        assertTxs();

        Entity e1 = createTestEntity("DUMMY", 1);
        Transaction t1 = service.beginTransaction();
        service.put(t1, e1);

        assertStoreContains(e1);

        assertTxs(t1);
        assertTrue(t1.isActive());

        Transaction t2 = service.beginTransaction();
        Entity e2 = createTestEntity("DUMMY", 2);
        service.put(e2);

        assertTxs(t1, t2);
        assertTrue(t1.isActive());
        assertTrue(t2.isActive());

        assertStoreContains(e2);
        t2.rollback();

        assertTxs(t1);
        assertTrue(t1.isActive());

        // should not be there due to rollback
        assertStoreDoesNotContain(e2);
        t1.commit();
        assertStoreContains(e1);

        assertTxs();
    }

    @Test
    public void testMultipleEntityGroupsInSingleTransactionAreNotAllowed() {
        Transaction tx = service.beginTransaction();
        try {
            Entity person = new Entity("Person", "tom");
            service.put(person);

            try {
                Entity photoNotAChild = new Entity("Photo");
                photoNotAChild.setProperty("photoUrl", "http://domain.com/path/to/photo.jpg");
                service.put(photoNotAChild);
                fail("put should have thrown IllegalArgumentException");
            } catch (IllegalArgumentException ex) {
                // pass
            }
        } finally {
            tx.rollback();
        }
    }

    protected void assertTxs(Transaction... txs) {
        Collection<Transaction> transactions = service.getActiveTransactions();
        assertNotNull(txs);
        Set<Transaction> expected = new HashSet<Transaction>(transactions);
        Set<Transaction> existing = new HashSet<Transaction>(Arrays.asList(txs));
        assertEquals(expected, existing);
    }
}
