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
 */
public class AdminExtension implements Extension {

    /**
     * Register all admin CDI beans.
     *
     * @param bbd the bbd event
     * @param bm  the bean manager
     */
    public void register(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {
        addAnnotatedType(bbd, bm, DatastoreEntityViewer.class);
        addAnnotatedType(bbd, bm, DatastoreViewer.class);
        addAnnotatedType(bbd, bm, DatastoreStatistics.class);
        addAnnotatedType(bbd, bm, HttpParamProducer.class);
        addAnnotatedType(bbd, bm, LogViewer.class);
        addAnnotatedType(bbd, bm, TimeFormatter.class);
        addAnnotatedType(bbd, bm, SizeFormatter.class);
    }

    private <E> void addAnnotatedType(BeforeBeanDiscovery bbd, BeanManager bm, Class<E> clazz) {
        final AnnotatedType<E> dev = bm.createAnnotatedType(clazz);
        bbd.addAnnotatedType(dev);
    }

}
