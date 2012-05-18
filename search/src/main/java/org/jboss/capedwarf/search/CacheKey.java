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

import org.infinispan.query.Transformable;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Transformable(transformer = CacheKeyTransformer.class)
public class CacheKey {

    private String indexName;
    private String namespace;
    private String documentId;

    public CacheKey(String indexName, String namespace, String documentId) {
        this.indexName = indexName;
        this.namespace = namespace;
        this.documentId = documentId;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getDocumentId() {
        return documentId;
    }

    @Override
    public String toString() {
        return "CacheKey{" +
            "indexName='" + indexName + '\'' +
            ", namespace='" + namespace + '\'' +
            ", documentId='" + documentId + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheKey cacheKey = (CacheKey) o;

        if (!documentId.equals(cacheKey.documentId)) return false;
        if (!indexName.equals(cacheKey.indexName)) return false;
        if (!namespace.equals(cacheKey.namespace)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = indexName.hashCode();
        result = 31 * result + namespace.hashCode();
        result = 31 * result + documentId.hashCode();
        return result;
    }
}
