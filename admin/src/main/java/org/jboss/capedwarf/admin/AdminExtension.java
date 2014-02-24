package org.jboss.capedwarf.admin;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

/**
 * Register admin console beans.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class AdminExtension implements Extension {

    /**
     * Register all admin CDI beans.
     *
     * @param bbd the bbd event
     * @param bm  the bean manager
     */
    public void register(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {
        addAnnotatedType(bbd, bm, Capabilities.class);
        addAnnotatedType(bbd, bm, DatastoreEditViewer.class);
        addAnnotatedType(bbd, bm, DatastoreEntityViewer.class);
        addAnnotatedType(bbd, bm, DatastoreViewer.class);
        addAnnotatedType(bbd, bm, DatastoreStatistics.class);
        addAnnotatedType(bbd, bm, Modules.class);
        addAnnotatedType(bbd, bm, Search.class);
        addAnnotatedType(bbd, bm, SearchDocument.class);
        addAnnotatedType(bbd, bm, SearchIndex.class);
        addAnnotatedType(bbd, bm, TaskQueues.class);
        addAnnotatedType(bbd, bm, TaskQueue.class);
        addAnnotatedType(bbd, bm, HttpParamProducer.class);
        addAnnotatedType(bbd, bm, LogViewer.class);
        addAnnotatedType(bbd, bm, TimeFormatter.class);
        addAnnotatedType(bbd, bm, SizeFormatter.class);
        addAnnotatedType(bbd, bm, ContextPathProducer.class);
    }

    private <E> void addAnnotatedType(BeforeBeanDiscovery bbd, BeanManager bm, Class<E> clazz) {
        final AnnotatedType<E> dev = bm.createAnnotatedType(clazz);
        bbd.addAnnotatedType(dev);
    }

}
