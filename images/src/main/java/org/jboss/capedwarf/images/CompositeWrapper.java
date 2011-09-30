/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.Image;

/**
 * Wraps GAE Composite and exposes its private fields.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CompositeWrapper extends ReflectionWrapper<Composite> {

    public CompositeWrapper(Composite composite) {
        super(composite);
    }

    public Image getImage() {
        return getFieldValue("image");
    }

    public Composite.Anchor getAnchor() {
        return getFieldValue("anchor");
    }

    public int getXOffset() {
        return (Integer) getFieldValue("xOffset");
    }

    public int getYOffset() {
        return (Integer) getFieldValue("yOffset");
    }

}
