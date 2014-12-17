/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.tasks;

import com.google.appengine.api.taskqueue.TaskOptions;
import org.jboss.capedwarf.common.jms.MessageCreator;
import org.jboss.capedwarf.common.jms.ServletExecutorProducer;
import org.jboss.capedwarf.shared.components.AppIdFactory;
import org.jboss.capedwarf.shared.components.ClassloaderAppIdFactory;
import org.jboss.capedwarf.shared.config.QueueXml;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ServletInvoker {
    /**
     * Invoke servlet async.
     *
     * @param path the servlet path
     * @throws Exception for any error
     */
    public static void invoke(String path) throws Exception {
        invoke(null, path);
    }

    /**
     * Invoke servlet async.
     *
     * @param module the module name
     * @param path   the servlet path
     * @throws Exception for any error
     */
    public static void invoke(final String module, String path) throws Exception {
        AppIdFactory previous = AppIdFactory.getCurrentFactory();
        AppIdFactory.setCurrentFactory(ClassloaderAppIdFactory.INSTANCE);
        try {
            TaskOptions taskOptions = TaskOptions.Builder.withUrl(path);
            MessageCreator creator = new TasksMessageCreator(QueueXml.INTERNAL, taskOptions);
            ServletExecutorProducer producer = new CustomServletExecutorProducer(module);
            try {
                producer.sendMessage(creator);
            } finally {
                producer.dispose();
            }
        } finally {
            AppIdFactory.setCurrentFactory(previous);
        }
    }

    private static class CustomServletExecutorProducer extends ServletExecutorProducer {
        private final String module;

        public CustomServletExecutorProducer(String module) {
            this.module = module;
        }

        @Override
        protected String getModuleName() {
            return (module != null) ? module : super.getModuleName();
        }
    }
}
