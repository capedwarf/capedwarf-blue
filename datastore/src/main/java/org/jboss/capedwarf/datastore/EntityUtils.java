package org.jboss.capedwarf.datastore;

import com.google.appengine.api.datastore.Entity;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class EntityUtils {

    public static Entity cloneEntity(Entity entity) {
        if (entity == null) {
            return null;
        }
        Entity clone = entity.clone();
        DatastoreServiceImpl.applyKeyChecked(entity, clone);
        return clone;
    }
}
