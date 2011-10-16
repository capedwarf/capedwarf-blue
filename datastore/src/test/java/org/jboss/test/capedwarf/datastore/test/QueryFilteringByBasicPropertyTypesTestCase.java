/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.test.capedwarf.datastore.test;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Datastore querying tests.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class QueryFilteringByBasicPropertyTypesTestCase extends QueryTestCase {

    @Test
    public void testBooleanProperty() throws Exception {
        testEqualityQueries(Boolean.TRUE, Boolean.FALSE);
    }

    @Test
    public void testIntegerProperty() {
        testEqualityQueries(1, 2);
        testInequalityQueries(1, 2, 3);
    }

    @Test
    public void testByteProperty() {
        testEqualityQueries((byte) 1, (byte) 2);
        testInequalityQueries((byte) 1, (byte) 2, (byte) 3);
    }

    @Test
    public void testShortProperty() {
        testEqualityQueries((short) 1, (short) 2);
        testInequalityQueries((short) 1, (short) 2, (short) 3);
    }

    @Test
    public void testLongProperty() {
        testEqualityQueries(1L, 2L);
        testInequalityQueries(1L, 2L, 3L);
    }

    @Test
    public void testFloatProperty() {
        testEqualityQueries(1f, 2f);
        testInequalityQueries(1f, 2f, 3f);
    }

    @Test
    public void testDoubleProperty() {
        testEqualityQueries(1.0, 2.0);
        testInequalityQueries(1.0, 2.0, 3.0);
    }

}
