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

package org.jboss.capedwarf.aspects.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.capedwarf.aspects.Aspect;
import org.jboss.capedwarf.aspects.AspectAdapter;
import org.jboss.capedwarf.aspects.GlobalTimeLimitAspect;
import org.jboss.capedwarf.aspects.InvocationTimeLimitAspect;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class AspectRegistry {
    private static final AspectWrapperComparator COMPARATOR = new AspectWrapperComparator();
    private static final Map<Key, AspectWrapper[]> aspectsMap = new ConcurrentHashMap<Key, AspectWrapper[]>();

    private static final Set<AspectWrapper> defaultAspects = new HashSet<AspectWrapper>();
    private static final Map<Class<? extends Annotation>, Aspect> aspectsPerAnnotation = new HashMap<Class<? extends Annotation>, Aspect>();

    static {
        // default aspects
        addDefaultAspect(new GlobalTimeLimitAspect());
        // per annotation aspects
        addAspect(new InvocationTimeLimitAspect());
    }

    public static <T extends Annotation> void addDefaultAspect(Aspect<T> aspect) {
        defaultAspects.add(new AspectWrapper(aspect));
    }

    public static <T extends Annotation> void addAspect(Aspect<T> aspect) {
        aspectsPerAnnotation.put(aspect.annotation(), aspect);
    }

    static AspectWrapper[] findAspects(AspectInfo info) {
        final Key key = new Key(info.getApiInterface(), info.getMethod());
        AspectWrapper[] aspects = aspectsMap.get(key);
        if (aspects == null) {
            aspects = buildAspects(info);
            aspectsMap.put(key, aspects);
        }
        return aspects;
    }

    private static AspectWrapper[] buildAspects(AspectInfo info) {
        final Set<AspectWrapper> aspects = new HashSet<AspectWrapper>();
        aspects.addAll(defaultAspects);

        Method method = info.getMethod();
        Annotation[] annotations = method.getAnnotations();
        aspects.addAll(buildAspects(annotations));

        annotations = info.getApiImpl().getClass().getAnnotations();
        aspects.addAll(buildAspects(annotations));

        List<AspectWrapper> list = new ArrayList<AspectWrapper>(aspects);
        Collections.sort(list, COMPARATOR);
        return list.toArray(new AspectWrapper[list.size()]);
    }

    private static Collection<? extends AspectWrapper> buildAspects(Annotation... annotations) {
        Set<AspectWrapper> aspects = new HashSet<AspectWrapper>();
        for (Annotation annotation : annotations) {
            Aspect aspect = aspectsPerAnnotation.get(annotation.annotationType());
            if (aspect != null) {
                if (aspect instanceof AspectAdapter) {
                    //noinspection unchecked
                    aspect = AspectAdapter.class.cast(aspect).adapt(annotation);
                }
                aspects.add(new AspectWrapper(aspect, annotation));
            }
        }
        return aspects;
    }

    private static class Key {
        private Class<?> apiInterface;
        private String methodName;
        private Class<?>[]parameterTypes;
        private Class<?> returnType;

        private Key(Class<?> apiInterface, Method method) {
            this.apiInterface = apiInterface;
            this.methodName = method.getName();
            this.parameterTypes = method.getParameterTypes();
            this.returnType = method.getReturnType();
        }

        public int hashCode() {
            int hash = 0;
            hash += apiInterface.hashCode();
            hash += 3 * methodName.hashCode();
            hash += 7 * Arrays.hashCode(parameterTypes);
            hash += 11 * returnType.hashCode();
            return hash;
        }

        @SuppressWarnings("RedundantIfStatement")
        public boolean equals(Object obj) {
            if (obj instanceof Key == false)
                return false;

            Key other = (Key) obj;
            if (apiInterface.equals(other.apiInterface) == false)
                return false;
            if (methodName.equals(other.methodName) == false)
                return false;
            if (Arrays.equals(parameterTypes, other.parameterTypes) == false)
                return false;
            if (returnType.equals(other.returnType) == false)
                return false;

            return true;
        }
    }

    private static class AspectWrapperComparator implements Comparator<AspectWrapper> {
        public int compare(AspectWrapper a1, AspectWrapper a2) {
            return a2.priority() - a1.priority();
        }
    }
}
