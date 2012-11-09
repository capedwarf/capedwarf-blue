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

package org.jboss.capedwarf.tools;

import java.util.HashMap;
import java.util.Map;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class Arguments {
    private final Map<String,String> map;

    public Arguments(String[] args) {
        map = new HashMap<String, String>();
        for (String arg : args) {
            int equalsIndex = arg.indexOf("=");
            if (equalsIndex == -1) {
                map.put(arg, "");
            } else {
                String property = arg.substring(0, equalsIndex);
                String value = arg.substring(equalsIndex + 1);
                map.put(property, value);
            }
        }
    }

    public String get(String name) {
        return map.get(name);
    }

    public String get(String name, String defaultValue) {
        String value = map.get(name);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }
}
