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

package org.jboss.test.capedwarf.datastore.test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class AllocateIdsTestCase extends AbstractTest {

    @Test
    public void testAllocateId() throws Exception {
        KeyRange keys = service.allocateIds("SomeKind", 10L);
        Assert.assertNotNull(keys);
        Key start = keys.getStart();
        Assert.assertNotNull(start);
        Assert.assertEquals(1, start.getId());
        Key end = keys.getStart();
        Assert.assertNotNull(end);
        Assert.assertEquals(10, end.getId());
    }

    @Test
    public void testCheckKeyRange() throws Exception {
        KeyRange kr1 = new KeyRange(null, "OtherKind", 1, 5);
        DatastoreService.KeyRangeState state1 = service.allocateIdRange(kr1);
        Assert.assertNotNull(state1);
        Assert.assertSame(DatastoreService.KeyRangeState.CONTENTION, state1);

        KeyRange kr2 = service.allocateIds("OtherKind", 6);
        Assert.assertNotNull(kr2);

        KeyRange kr3 = new KeyRange(null, "OtherKind", 2, 5);
        DatastoreService.KeyRangeState state2 = service.allocateIdRange(kr3);
        Assert.assertNotNull(state2);
        Assert.assertSame(DatastoreService.KeyRangeState.COLLISION, state1);
    }

}
