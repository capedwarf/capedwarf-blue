package org.jboss.capedwarf.images;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.ServingUrlOptions;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class ServingUrlOptionsHelper {
    private ServingUrlOptions options;

    public ServingUrlOptionsHelper(ServingUrlOptions options) {
        this.options = options;
    }

    public boolean hasBlobKey() {
        return (Boolean) invokeMethod("hasBlobKey");
    }

    public BlobKey getBlobKey() {
        return invokeMethod("getBlobKey");
    }

    public boolean hasImageSize() {
        return (Boolean) invokeMethod("hasImageSize");
    }

    public int getImageSize() {
        return (Integer) invokeMethod("getImageSize");
    }

    public boolean hasCrop() {
        return (Boolean) invokeMethod("hasCrop");
    }

    public boolean getCrop() {
        return (Boolean) invokeMethod("getCrop");
    }

    public boolean hasSecureUrl() {
        return (Boolean) invokeMethod("hasSecureUrl");
    }

    public boolean getSecureUrl() {
        return (Boolean) invokeMethod("getSecureUrl");
    }

    @SuppressWarnings("unchecked")
    private <T> T invokeMethod(String methodName) {
        return (T) ReflectionUtils.invokeInstanceMethod(options, methodName);
    }
}
