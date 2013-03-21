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

import org.jboss.capedwarf.datastore.metadata.MetadataQueryTypeFactory;
import org.jboss.capedwarf.datastore.stats.StatsQueryTypeFactory;

/**
 * Handle entity kind.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class KindUtils {
    private KindUtils() {
    }

    public static enum Type {
        STATS(new StatsMatcher()),
        METADATA(new MetadataMatcher());

        private Matcher matcher;

        private Type(Matcher matcher) {
            this.matcher = matcher;
        }

        boolean match(String kind) {
            return matcher.match(kind);
        }

        public boolean inProgress() {
            return matcher.inProgress();
        }
    }

    private static interface Matcher {
        boolean match(String kind);
        boolean inProgress();
    }

    /**
     * Match kind with any type.
     *
     * @param kind the kind
     * @param types the types
     * @return true if matches any type, false otherwise
     */
    public static boolean match(String kind, Type... types) {
        for (Type type : types) {
            if (type.match(kind))
                return true;
        }
        return false;
    }

    /**
     * In progress
     *
     * @param types the types
     * @return true if matches any type, false otherwise
     */
    public static boolean inProgress(Type... types) {
        for (Type type : types) {
            if (type.inProgress())
                return true;
        }
        return false;
    }

    /**
     * Is special kind, matches any of the default types.
     *
     * @param kind the kind
     * @return true if matches any of the default types, false otherwise
     */
    public static boolean isSpecial(String kind) {
        return match(kind, Type.values());
    }

    private static class StatsMatcher implements Matcher {
        public boolean match(String kind) {
            return StatsQueryTypeFactory.isStatsKind(kind);
        }

        public boolean inProgress() {
            return false;
        }
    }

    private static class MetadataMatcher implements Matcher {
        public boolean match(String kind) {
            return MetadataQueryTypeFactory.isMetadataKind(kind);
        }

        public boolean inProgress() {
            return MetadataQueryTypeFactory.inProgress();
        }
    }
}
