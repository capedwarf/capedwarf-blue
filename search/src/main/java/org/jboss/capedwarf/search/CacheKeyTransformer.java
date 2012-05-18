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

import org.infinispan.query.Transformer;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class CacheKeyTransformer implements Transformer {

    private static final String TOKEN_DELIMITER = ":_$capedwarf$_:";

    public Object fromString(String s) {
        String[] tokens = s.split(Pattern.quote(TOKEN_DELIMITER));
        if (tokens.length != 3) {
            throw new IllegalArgumentException("Not a valid CacheKey string: " + s);
        }

        return new CacheKey(tokens[0], tokens[1], tokens[2]);
    }

    public String toString(Object customType) {
        CacheKey cacheKey = (CacheKey) customType;
        return cacheKey.getIndexName() + TOKEN_DELIMITER + cacheKey.getNamespace() + TOKEN_DELIMITER + cacheKey.getDocumentId();
    }
}
