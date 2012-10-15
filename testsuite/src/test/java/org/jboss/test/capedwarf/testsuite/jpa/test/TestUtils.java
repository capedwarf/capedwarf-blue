package org.jboss.test.capedwarf.testsuite.jpa.test;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

import java.io.File;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TestUtils {

    private static PomEquippedResolveStage resolver;

    public static PomEquippedResolveStage getResolver() {
        if (resolver == null)
            resolver = Maven.resolver().loadPomFromFile(getPomPath());
        return resolver;
    }

    protected static boolean isBlue(String absolutePath) {
        // Is this test run in CapeDwarf Blue -- impl detail!
        // Found "blue" or force Blue via "capedwarf.blue" system property
        return absolutePath.contains("blue") || Boolean.getBoolean("capedwarf.blue");
    }

    // we need testsuite/pom.xml file
    protected static String getPomPath() {
        final File root = new File(".");
        String path = "pom.xml";
        final String absolutePath = root.getAbsolutePath();
        if (isBlue(absolutePath)) {
            if (absolutePath.contains("testsuite") == false)
                path = "testsuite/" + path;
        } else {
            // Or are we in CapeDwarf Testsuite
            if (absolutePath.contains("tests") == false)
                path = "tests/" + path;
        }
        return path;
    }

    public static void addLibraries(WebArchive war) {
        // default JPA libs
        war.addAsLibraries(getDependency("com.google.appengine:appengine-api-1.0-sdk"));
        war.addAsLibraries(getDependency("org.datanucleus:datanucleus-core"));
        war.addAsLibraries(getDependency("org.datanucleus:datanucleus-api-jpa"));
        war.addAsLibraries(getDependency("com.google.appengine.orm:datanucleus-appengine"));
        war.addAsLibraries(getDependency("javax.jdo:jdo-api"));
        war.addAsLibraries(getDependency("org.apache.geronimo.specs:geronimo-jta_1.1_spec"));
        war.addAsLibraries(getDependency("org.hibernate.javax.persistence:hibernate-jpa-2.0-api"));
    }
    
    private static File getDependency(final String coordinates){
        return getResolver().resolve(coordinates).withoutTransitivity().asSingle(File.class);
    }

    public static void addPersistenceXml(WebArchive war, String resource) {
        war.addAsWebInfResource(resource, "classes/META-INF/persistence.xml");
    }
}
