/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.capedwarf.blobstore;

import com.google.appengine.api.blobstore.UploadOptions;
import org.jboss.capedwarf.shared.reflection.MethodInvocation;
import org.jboss.capedwarf.shared.reflection.ReflectionUtils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class UploadOptionsAdapter {
    private static final MethodInvocation<Boolean> hasMaxUploadSizeBytesPerBlob = ReflectionUtils.cacheMethod(UploadOptions.class, "hasMaxUploadSizeBytesPerBlob");
    private static final MethodInvocation<Long> getMaxUploadSizeBytesPerBlob = ReflectionUtils.cacheMethod(UploadOptions.class, "getMaxUploadSizeBytesPerBlob");

    private static final MethodInvocation<Boolean> hasMaxUploadSizeBytes = ReflectionUtils.cacheMethod(UploadOptions.class, "hasMaxUploadSizeBytes");
    private static final MethodInvocation<Long> getMaxUploadSizeBytes = ReflectionUtils.cacheMethod(UploadOptions.class, "getMaxUploadSizeBytes");

    private static final MethodInvocation<Boolean> hasGoogleStorageBucketName = ReflectionUtils.cacheMethod(UploadOptions.class, "hasGoogleStorageBucketName");
    private static final MethodInvocation<String> getGoogleStorageBucketName = ReflectionUtils.cacheMethod(UploadOptions.class, "getGoogleStorageBucketName");

    private final UploadOptions options;

    UploadOptionsAdapter(UploadOptions options) {
        this.options = options;
    }

    boolean hasMaxUploadSizeBytesPerBlob() {
        return hasMaxUploadSizeBytesPerBlob.invokeWithTarget(options);
    }

    long getMaxUploadSizeBytesPerBlob() {
        return getMaxUploadSizeBytesPerBlob.invokeWithTarget(options);
    }

    boolean hasMaxUploadSizeBytes() {
        return hasMaxUploadSizeBytes.invokeWithTarget(options);
    }

    long getMaxUploadSizeBytes() {
        return getMaxUploadSizeBytes.invokeWithTarget(options);
    }

    boolean hasGoogleStorageBucketName() {
        return hasGoogleStorageBucketName.invokeWithTarget(options);
    }

    String getGoogleStorageBucketName() {
        return getGoogleStorageBucketName.invokeWithTarget(options);
    }
}
