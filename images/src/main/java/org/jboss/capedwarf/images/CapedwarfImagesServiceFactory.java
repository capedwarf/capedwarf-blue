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
import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.CompositeTransform;
import com.google.appengine.api.images.IImagesServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.Transform;
import org.jboss.capedwarf.aspects.proxy.AspectFactory;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
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

    public Image makeImage(byte[] bytes) {
        return getDelegate().makeImage(bytes);
    }

    public Image makeImageFromBlob(BlobKey blobKey) {
        return getDelegate().makeImageFromBlob(blobKey);
    }

    public Image makeImageFromFilename(String s) {
        return getDelegate().makeImageFromFilename(s);
    }

    public Transform makeResize(int i, int i2) {
        return getDelegate().makeResize(i, i2);
    }

    public Transform makeResize(int i, int i2, boolean b) {
        return getDelegate().makeResize(i, i2, b);
    }

    public Transform makeResize(int i, int i2, float v, float v2) {
        return getDelegate().makeResize(i, i2, v, v2);
    }

    public Transform makeResize(int i, int i2, double v, double v2) {
        return getDelegate().makeResize(i, i2, v, v2);
    }

    public Transform makeCrop(float v, float v2, float v3, float v4) {
        return getDelegate().makeCrop(v, v2, v3, v4);
    }

    public Transform makeCrop(double v, double v2, double v3, double v4) {
        return getDelegate().makeCrop(v, v2, v3, v4);
    }

    public Transform makeVerticalFlip() {
        return getDelegate().makeVerticalFlip();
    }

    public Transform makeHorizontalFlip() {
        return getDelegate().makeHorizontalFlip();
    }

    public Transform makeRotate(int i) {
        return getDelegate().makeRotate(i);
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

    public Composite makeComposite(Image image, int i, int i2, float v, Composite.Anchor anchor) {
        return getDelegate().makeComposite(image, i, i2, v, anchor);
    }
}
