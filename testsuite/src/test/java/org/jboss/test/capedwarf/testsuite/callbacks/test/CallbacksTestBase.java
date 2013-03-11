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

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.jboss.test.capedwarf.testsuite.LibUtils;
import org.jboss.test.capedwarf.testsuite.TestsuiteTestBase;
import org.jboss.test.capedwarf.testsuite.callbacks.support.BarKindCallbackHandler;
import org.jboss.test.capedwarf.testsuite.callbacks.support.FooBarKindCallbackHandler;
import org.jboss.test.capedwarf.testsuite.callbacks.support.FooKindCallbackHandler;
import org.jboss.test.capedwarf.testsuite.callbacks.support.UnboundCallbackHandler;
import org.junit.Before;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public abstract class CallbacksTestBase extends TestsuiteTestBase {

    public static final String KIND = "Foo";
    public static final String KIND2 = "Bar";

    protected static WebArchive getDefaultDeployment() {
        TestContext context = TestContext.asDefault().setCallbacks(true).setIgnoreLogging(true);
        WebArchive war = getCapedwarfDeployment(context);
        war.addClass(TestsuiteTestBase.class);
        war.addClass(CallbacksTestBase.class);
        war.addPackage(BarKindCallbackHandler.class.getPackage());
        LibUtils.addGaeAsLibrary(war);
        return war;
    }

    @Before
    public void setUp() {
        reset();
    }

    protected void reset() {
        UnboundCallbackHandler.states.clear();
        FooKindCallbackHandler.states.clear();
        FooBarKindCallbackHandler.states.clear();
        BarKindCallbackHandler.states.clear();
    }

    protected void assertNoCallbackInvoked() {
        assertCallbackInvoked();    // note: states parameter is empty
    }

    protected void assertCallbackInvoked(String... states) {
        assertEquals(asList(states), UnboundCallbackHandler.states);
        assertEquals(asList(states), FooKindCallbackHandler.states);
        assertEquals(asList(states), FooBarKindCallbackHandler.states);
        assertEquals(emptyList(), BarKindCallbackHandler.states);

        reset();
    }

    protected void assertCallbackInvokedAtLeastOnce(String callBack) {
        assertTrue(callBack + " was not invoked", UnboundCallbackHandler.states.contains(callBack));
        assertTrue(callBack + " was not invoked", FooKindCallbackHandler.states.contains(callBack));
        assertTrue(callBack + " was not invoked", FooBarKindCallbackHandler.states.contains(callBack));
        assertEquals(emptyList(), BarKindCallbackHandler.states);

        reset();
    }
}
