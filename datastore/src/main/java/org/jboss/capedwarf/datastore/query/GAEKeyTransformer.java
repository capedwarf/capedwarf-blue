/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.capedwarf.datastore.query;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.infinispan.query.Transformer;
import org.jboss.capedwarf.shared.datastore.DatastoreConstants;


/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GAEKeyTransformer implements Transformer {
    public Object fromString(String s) {
        return from(s);
    }

    public String toString(Object key) {
        return to(key);
    }

    static String to(Object key) {
        return key.toString();
    }

    static Key from(String string) {
        try {
            return fromInternal(string);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot parse key: " + string, t);
        }
    }

    private static Key fromInternal(String string) {
        string = removeIndexName(string);
        String[] split = split(string);
        if (split.length == 0) {
            throw new IllegalArgumentException("Bad key: " + string);
        }
        String namespace = parseNamespace(split[0]);
        String previousNs = NamespaceManager.get();
        NamespaceManager.set(namespace);
        KeyFactory.Builder builder;
        try {
            Object[] args = parseKindAndNameOrId(namespace, split[0]);
            if (args[1] instanceof Long) {
                builder = new KeyFactory.Builder(args[0].toString(), (Long) args[1]);
            } else {
                builder = new KeyFactory.Builder(args[0].toString(), args[1].toString());
            }
        } finally {
            NamespaceManager.set(previousNs);
        }
        return build(builder, split);
    }

    private static String removeIndexName(String string) {
        int index = string.lastIndexOf(DatastoreConstants.SEPARATOR);
        if (index == -1) {
            return string;
        }

        index = string.lastIndexOf(DatastoreConstants.SEPARATOR, index - 1);
        return string.substring(0, index);
    }

    private static String[] split(String string) {
        List<String> strings = new ArrayList<String>();
        int p = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '/' && string.charAt(i - 1) == ')') {
                strings.add(string.substring(p, i));
                p = i + 1;
            }
        }
        strings.add(string.substring(p)); // last token
        return strings.toArray(new String[strings.size()]);
    }

    private static Key build(KeyFactory.Builder builder, String[] tokens) {
        for (int i = 1; i < tokens.length; i++) {
            String namespace = parseNamespace(tokens[i]);
            String previousNs = NamespaceManager.get();
            NamespaceManager.set(namespace);
            try {
                Object[] args = parseKindAndNameOrId(namespace, tokens[i]);
                if (args[1] instanceof Long) {
                    builder.addChild(args[0].toString(), (Long) args[1]);
                } else {
                    builder.addChild(args[0].toString(), args[1].toString());
                }
            } finally {
                NamespaceManager.set(previousNs);
            }
        }
        return builder.getKey();
    }

    private static Object[] parseKindAndNameOrId(String namespace, String token) {
        if (namespace != null) {
            token = token.substring(namespace.length() + 2); // !namespace:kind(id|name)
        }
        int p = token.indexOf('(');
        String kind = token.substring(0, p);
        String ids = token.substring(p + 1, token.length() - 1);
        if (ids.charAt(0) == '"') {
            return new Object[]{kind, ids.substring(1, ids.length() - 1)};
        } else {
            Long id;
            if (ids.equals("no-id-yet")) id = 0L;
            else id = Long.parseLong(ids);
            return new Object[]{kind, id};
        }
    }

    private static String parseNamespace(String token) {
        if (token.startsWith("!")) {
            int p = token.indexOf(':');
            return token.substring(1, p);
        } else {
            return null;
        }
    }
}
