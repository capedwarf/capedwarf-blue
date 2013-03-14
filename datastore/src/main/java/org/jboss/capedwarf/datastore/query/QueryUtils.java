package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.datastore.Query;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class QueryUtils {

    public static String getNamespace(Query query) {
        Object appIdNamespace = ReflectionUtils.invokeInstanceMethod(query, "getAppIdNamespace");
        return (String) ReflectionUtils.invokeInstanceMethod(appIdNamespace, "getNamespace");
    }
}
