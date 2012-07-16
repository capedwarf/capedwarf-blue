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

package org.jboss.capedwarf.datastore.datancleus;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.datanucleus.metadata.MetaDataScanner;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileVisitor;
import org.jboss.vfs.VisitorAttributes;

/**
 * Default JBoss VFS based metadata scanner.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossMetaDataScanner implements MetaDataScanner {
    public Set<String> scanForPersistableClasses(PersistenceUnitMetaData pumd) {
        try {
            final Set<String> classes = new HashSet<String>();
            final VirtualFile root = VFS.getChild(pumd.getRootURI());
            root.visit(new VirtualFileVisitor() {
                public VisitorAttributes getAttributes() {
                    return VisitorAttributes.RECURSE_LEAVES_ONLY;
                }

                public void visit(VirtualFile file) {
                    if (accept(file)) {
                        String relative = file.getPathNameRelativeTo(root);
                        int length = relative.length();
                        relative = relative.substring(0, length - ".class".length());
                        classes.add(relative.replace("/", "."));
                    }
                }
            });
            return classes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean accept(VirtualFile file) {
        return file.getName().endsWith(".class");
    }
}
