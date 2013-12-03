/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.images;

import java.util.Collection;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.CompositeTransform;
import com.google.appengine.api.images.IImagesServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.Transform;
import org.jboss.capedwarf.aspects.proxy.AspectFactory;
import org.jboss.capedwarf.shared.reflection.ReflectionUtils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
class CapedwarfImagesServiceFactory implements IImagesServiceFactory {
    private volatile IImagesServiceFactory delegate;

    protected IImagesServiceFactory getDelegate() {
        if (delegate == null) {
            delegate = ReflectionUtils.newInstance("com.google.appengine.api.images.ImagesServiceFactoryImpl");
        }
        return delegate;
    }

    public ImagesService getImagesService() {
        return AspectFactory.createProxy(ImagesService.class, new CapedwarfImagesService());
    }

    public Image makeImage(byte[] imageData) {
        return getDelegate().makeImage(imageData);
    }

    public Image makeImageFromBlob(BlobKey blobKey) {
        byte[] bytes = BlobstoreServiceFactory.getBlobstoreService().fetchData(blobKey, 0, BlobstoreService.MAX_BLOB_FETCH_SIZE - 1);
        return makeImage(bytes);
    }

    public Image makeImageFromFilename(String filename) {
        return getDelegate().makeImageFromFilename(filename);
    }

    public Transform makeResize(int width, int height) {
        return getDelegate().makeResize(width, height);
    }

    public Transform makeResize(int width, int height, boolean allowStretch) {
        return getDelegate().makeResize(width, height, allowStretch);
    }

    public Transform makeResize(int width, int height, float cropOffsetX, float cropOffsetY) {
        return getDelegate().makeResize(width, height, cropOffsetX, cropOffsetY);
    }

    public Transform makeResize(int width, int height, double cropOffsetX, double cropOffsetY) {
        return getDelegate().makeResize(width, height, cropOffsetX, cropOffsetY);
    }

    public Transform makeCrop(float leftX, float topY, float rightX, float bottomY) {
        return getDelegate().makeCrop(leftX, topY, rightX, bottomY);
    }

    public Transform makeCrop(double leftX, double topY, double rightX, double bottomY) {
        return getDelegate().makeCrop(leftX, topY, rightX, bottomY);
    }

    public Transform makeVerticalFlip() {
        return getDelegate().makeVerticalFlip();
    }

    public Transform makeHorizontalFlip() {
        return getDelegate().makeHorizontalFlip();
    }

    public Transform makeRotate(int degrees) {
        return getDelegate().makeRotate(degrees);
    }

    public Transform makeImFeelingLucky() {
        return getDelegate().makeImFeelingLucky();
    }

    public CompositeTransform makeCompositeTransform(Collection<Transform> transforms) {
        return getDelegate().makeCompositeTransform(transforms);
    }

    public CompositeTransform makeCompositeTransform() {
        return getDelegate().makeCompositeTransform();
    }

    public Composite makeComposite(Image image, int xOffset, int yOffset, float opacity, Composite.Anchor anchor) {
        return getDelegate().makeComposite(image, xOffset, yOffset, opacity, anchor);
    }
}
