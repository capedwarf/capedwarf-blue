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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
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
            assertStoreDoesNotContain(entity);
            tx.commit();
            assertStoreContains(entity);
        } catch (Exception e) {
            tx.rollback();
            throw e;
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
        assertNoActiveTransactions();

        Entity e1;
        Entity e2;

        Transaction t1 = service.beginTransaction();
        try {
            e1 = createTestEntity("DUMMY", 1);
            service.put(t1, e1);
            assertStoreDoesNotContain(e1);

            assertActiveTransactions(t1);

            Transaction t2 = service.beginTransaction();
            try {
                e2 = createTestEntity("DUMMY", 2);
                service.put(e2);

                assertActiveTransactions(t1, t2);
                assertStoreDoesNotContain(e2);
            } finally {
                t2.rollback();
            }

            assertActiveTransactions(t1);
//            assertStoreDoesNotContain(e2);  // should not be there due to rollback
        } finally {
            t1.commit();
        }

        assertStoreContains(e1);
        assertStoreDoesNotContain(e2);  // should not be there due to rollback
        assertNoActiveTransactions();
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

    @Test
    public void testAncestorIsMandatoryInQueriesInsideTransaction() {
        Transaction tx = service.beginTransaction();
        try {

            service.prepare(new Query("test"));         // no tx, ancestor not necessary
            service.prepare(null, new Query("test"));   // no tx, ancestor not necessary
            service.prepare(tx, new Query("test").setAncestor(KeyFactory.createKey("some_kind", "some_id"))); // tx + ancestor

            try {
                service.prepare(tx, new Query("test")); // tx, but no ancestor
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // pass
            }
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void testGetWithDifferentAncestorsInsideSameTransactionAreNotAllowed() {
        service.put(new Entity("foo", "1"));
        service.put(new Entity("foo", "2"));

        Transaction tx = service.beginTransaction();
        try {
            service.get(Arrays.asList(KeyFactory.createKey("foo", "1")));

            try {
                service.get(Arrays.asList(KeyFactory.createKey("foo", "2")));
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // pass
            }
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void testMultipleQueriesWithSameAncestorInsideSameTransactionAreAllowed() {
        Transaction tx = service.beginTransaction();
        try {
            Key ancestor = KeyFactory.createKey("ancestor", "1");
            prepareQueryWithAncestor(tx, ancestor).asIterator().hasNext();
            prepareQueryWithAncestor(tx, ancestor).asIterator().hasNext();
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void testQueriesWithDifferentAncestorsInsideSameTransactionThrowIllegalArgumentException() {
        Transaction tx = service.beginTransaction();
        try {
            Key someAncestor = KeyFactory.createKey("ancestor", "1");
            prepareQueryWithAncestor(tx, someAncestor).asIterator();

            Key otherAncestor = KeyFactory.createKey("ancestor", "2");
            assertIAEWhenAccessingResult(prepareQueryWithAncestor(tx, otherAncestor));
        } finally {
            tx.rollback();
        }
    }

    private PreparedQuery prepareQueryWithAncestor(Transaction tx, Key someAncestor) {
        return service.prepare(tx, new Query("foo").setAncestor(someAncestor));
    }

    private void assertNoActiveTransactions() {
        assertActiveTransactions();
    }

    protected void assertActiveTransactions(Transaction... txs) {
        Collection<Transaction> transactions = service.getActiveTransactions();
        assertNotNull(txs);
        Set<Transaction> expected = new HashSet<Transaction>(transactions);
        Set<Transaction> existing = new HashSet<Transaction>(Arrays.asList(txs));
        assertEquals(expected, existing);

        for (Transaction tx : txs) {
            assertTrue(tx.isActive());
        }
    }
}
