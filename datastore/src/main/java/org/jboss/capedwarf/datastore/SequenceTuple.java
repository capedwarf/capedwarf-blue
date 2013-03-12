/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.datastore;

import java.util.Map;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class SequenceTuple {
    private static final String SEQUENCE_POSTFIX = "_SEQUENCE__"; // GAE's SequenceGenerator impl detail

    private String sequenceName;
    private int allocationSize;

    SequenceTuple(String sequenceName, int allocationSize) {
        this.sequenceName = sequenceName;
        this.allocationSize = allocationSize;
    }

    String getSequenceName() {
        return sequenceName;
    }

    int getAllocationSize() {
        return allocationSize;
    }

    static SequenceTuple getSequenceTuple(Map<String, Integer> allocationsMap, String kind) {
        final String key;
        final int p = kind.lastIndexOf(SEQUENCE_POSTFIX);
        if (p > 0) {
            key = kind.substring(0 , p);
        } else {
            key = kind;
        }
        // search w/o _SEQUENCE__, to find explicit ones
        Integer allocationSize = allocationsMap.get(key);
        final String sequenceName;
        if (allocationSize != null) {
            // impl detail, on how to diff default vs. explicit seq names
            if (allocationSize > 0) {
                sequenceName = key + SEQUENCE_POSTFIX; // by default add _SEQUENCE__
            } else {
                allocationSize = (-1) * allocationSize;
                sequenceName = key; // use explicit sequence name
            }
        } else {
            allocationSize = 1;
            sequenceName = key + SEQUENCE_POSTFIX; // by default add _SEQUENCE__
        }
        return new SequenceTuple(sequenceName, allocationSize);
    }
}
