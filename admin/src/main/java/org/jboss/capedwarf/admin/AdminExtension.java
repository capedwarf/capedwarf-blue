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
        final AnnotatedType<DatastoreEntityViewer> dev = bm.createAnnotatedType(DatastoreEntityViewer.class);
        bbd.addAnnotatedType(dev);

        final AnnotatedType<DatastoreViewer> dv = bm.createAnnotatedType(DatastoreViewer.class);
        bbd.addAnnotatedType(dv);
    }

}
