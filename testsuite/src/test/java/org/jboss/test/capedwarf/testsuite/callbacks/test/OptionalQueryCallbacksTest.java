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

import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 * TODO -- should be removed once GAE fixes callback List::subList bug
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Category(JBoss.class)
public class OptionalQueryCallbacksTest extends QueryCallbacksTestBase {

    @Test
    public void testSubList() throws Exception {
        List<Entity> list = asList(FetchOptions.Builder.withChunkSize(1));

        List<Entity> subList = list.subList(1, 3);
        assertPostLoadCallbackInvokedTimes(3);
        assertEquals(2, subList.size());

        list.get(0);
        assertNoCallbackInvoked();
        subList.get(0);
        assertNoCallbackInvoked();

        list.get(2);
        assertNoCallbackInvoked();
        subList.get(1);
        assertNoCallbackInvoked();

        list.get(3);
        assertPostLoadCallbackInvokedTimes(1);
    }

}
