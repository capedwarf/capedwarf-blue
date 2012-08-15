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

package org.jboss.capedwarf.search;

import com.google.appengine.api.search.Consistency;

import java.io.Serializable;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class FullIndexSpec implements Serializable, Comparable<FullIndexSpec> {
    private String namespace;
    private String name;
    private Consistency consistency;

    FullIndexSpec(String namespace, String name, Consistency consistency) {
        this.namespace = namespace;
        this.name = name;
        this.consistency = consistency;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public Consistency getConsistency() {
        return consistency;
    }

    public int compareTo(FullIndexSpec o) {
        int compare = name.compareTo(o.name);
        if (compare != 0) {
            return compare;
        }

        compare = namespace.compareTo(o.namespace);
        if (compare != 0) {
            return compare;
        }

        return consistency.compareTo(o.consistency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FullIndexSpec that = (FullIndexSpec) o;

        if (consistency != that.consistency) return false;
        if (!name.equals(that.name)) return false;
        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = namespace != null ? namespace.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + consistency.hashCode();
        return result;
    }
}
