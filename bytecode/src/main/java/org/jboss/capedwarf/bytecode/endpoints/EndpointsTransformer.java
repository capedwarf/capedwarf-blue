package org.jboss.capedwarf.bytecode.endpoints;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import javassist.CtClass;
import org.jboss.capedwarf.bytecode.JavassistTransformer;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class EndpointsTransformer extends JavassistTransformer {
    private static final Set<String> IGNORED_PACKAGES = new HashSet<String>();

    static {
        IGNORED_PACKAGES.add("org/jboss/capedwarf/");
        IGNORED_PACKAGES.add("org.jboss.capedwarf.");
        IGNORED_PACKAGES.add("com/google/api/server/");
        IGNORED_PACKAGES.add("com.google.api.server.");
        IGNORED_PACKAGES.add("com/google/appengine/spi/");
        IGNORED_PACKAGES.add("com.google.appengine.spi.");
        IGNORED_PACKAGES.add("com/google/appengine/api/");
        IGNORED_PACKAGES.add("com.google.appengine.api.");
        IGNORED_PACKAGES.add("com/google/appengine/tools/");
        IGNORED_PACKAGES.add("com.google.appengine.tools.");
        IGNORED_PACKAGES.add("com/google/apphosting/api/");
        IGNORED_PACKAGES.add("com.google.apphosting.api.");
        IGNORED_PACKAGES.add("com/google/apphosting/base/");
        IGNORED_PACKAGES.add("com.google.apphosting.base.");
        IGNORED_PACKAGES.add("org/junit/");
        IGNORED_PACKAGES.add("org.junit.");
        IGNORED_PACKAGES.add("org/jboss/arquillian/");
        IGNORED_PACKAGES.add("org.jboss.arquillian.");
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (isAllowedPackage(className)) {
            return classfileBuffer;
        }
        return super.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }

    protected boolean isAllowedPackage(String className) {
        for (String pckg : IGNORED_PACKAGES) {
            if (className.startsWith(pckg)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void transform(CtClass clazz) throws Exception {
        new EndPointAnnotator(clazz).addAnnotations();
    }
}
