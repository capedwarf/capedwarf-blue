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

package org.jboss.capedwarf.users;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;

import java.util.Set;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossUserService implements UserService {
    public String createLoginURL(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String createLoginURL(String s, String s1) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String createLoginURL(String s, String s1, String s2, Set<String> strings) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String createLogoutURL(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String createLogoutURL(String s, String s1) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isUserLoggedIn() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isUserAdmin() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public User getCurrentUser() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
