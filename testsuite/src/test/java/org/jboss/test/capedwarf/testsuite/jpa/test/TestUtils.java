package org.jboss.test.capedwarf.testsuite.jpa.test;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.testsuite.LibUtils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TestUtils {

    public static void addLibraries(WebArchive war) {
        // default JPA libs
        LibUtils.addGaeAsLibrary(war);
        LibUtils.addLibrary(war, "org.datanucleus:datanucleus-core");
        LibUtils.addLibrary(war, "org.datanucleus:datanucleus-api-jpa");
        LibUtils.addLibrary(war, "com.google.appengine.orm:datanucleus-appengine");
        LibUtils.addLibrary(war, "javax.jdo:jdo-api");
        LibUtils.addLibrary(war, "org.apache.geronimo.specs:geronimo-jta_1.1_spec");
        LibUtils.addLibrary(war, "org.hibernate.javax.persistence:hibernate-jpa-2.0-api");
    }
    
    public static void addPersistenceXml(WebArchive war, String resource) {
        war.addAsWebInfResource(resource, "classes/META-INF/persistence.xml");
    }
}
