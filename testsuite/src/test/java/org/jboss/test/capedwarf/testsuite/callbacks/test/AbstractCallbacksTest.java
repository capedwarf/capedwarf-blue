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

import com.google.appengine.api.datastore.AsyncDatastoreService;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.testsuite.AbstractTest;
import org.jboss.test.capedwarf.testsuite.LibUtils;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public abstract class AbstractCallbacksTest extends AbstractTest {
    protected AsyncDatastoreService service;

    protected static WebArchive getDefaultDeployment() {
        final WebArchive war = getCapedwarfDeployment();
        war.addClass(AbstractCallbacksTest.class);
        war.addClass(CallbackHandler.class);
        war.addAsWebInfResource("META-INF/datastorecallbacks.xml", "classes/META-INF/datastorecallbacks.xml");
        LibUtils.addGaeAsLibrary(war);
        return war;
    }

    protected abstract AsyncDatastoreService createAsyncDatastoreService();

    @Before
    public void startUp() {
        service = createAsyncDatastoreService();
    }

    protected void reset() {
        CallbackHandler.state = null;
    }
}
