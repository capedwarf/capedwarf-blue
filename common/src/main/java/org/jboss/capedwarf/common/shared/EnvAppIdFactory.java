package org.jboss.capedwarf.common.shared;

import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.shared.components.AppIdFactory;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Deprecated // use SimpleAppIdFactory
public class EnvAppIdFactory extends AppIdFactory {
    public static final AppIdFactory INSTANCE = new EnvAppIdFactory();

    private EnvAppIdFactory() {
    }

    public String appId() {
        return Application.getAppId();
    }
}
