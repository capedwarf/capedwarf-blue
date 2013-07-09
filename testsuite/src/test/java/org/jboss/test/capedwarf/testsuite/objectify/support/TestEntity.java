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

package org.jboss.test.capedwarf.testsuite.objectify.support;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * @author Chris Ritter
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Entity(name = "TestEntity")
@Cache
public class TestEntity {
    @Id
    public Long id;
    public String name;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TestEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String toString() {
        return id != null ? id.toString() : "new";
    }
}