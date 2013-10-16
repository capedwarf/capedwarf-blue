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

package org.jboss.capedwarf.bytecode.endpoints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import org.jboss.capedwarf.bytecode.Annotator;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class EndPointAnnotator extends Annotator {

    public static final String DEFAULT_NAME = "myapi";
    public static final String DEFAULT_VERSION = "v1";

    private static Map<String, Class<? extends java.lang.annotation.Annotation>> HTTP_METHODS = new HashMap<>();

    static {
        HTTP_METHODS.put(ApiMethod.HttpMethod.GET, GET.class);
        HTTP_METHODS.put(ApiMethod.HttpMethod.POST, POST.class);
        HTTP_METHODS.put(ApiMethod.HttpMethod.PUT, PUT.class);
        HTTP_METHODS.put(ApiMethod.HttpMethod.DELETE, DELETE.class);
    }

    public EndPointAnnotator(CtClass clazz) {
        super(clazz);
    }

    @Override
    public void addAnnotations() throws Exception {
        Api api = findApi(getClazz());
        if (api == null) {
            return;
        }

        addAnnotationsToClass(
            createPathAnnotation("" // TODO: api.root()
                + "/" + (api.name().isEmpty() ? DEFAULT_NAME : api.name())
                + "/" + (api.version().isEmpty() ? DEFAULT_VERSION : api.version())));

        for (CtMethod method : getClazz().getDeclaredMethods()) {
            ApiMethod apiMethod = findApiMethod(method);
            if (apiMethod != null) {
                convertApiMethodAnnotation(method, apiMethod);
            }
        }
    }

    protected Api findApi(CtClass ctClass) throws Exception {
        if (ctClass == null) {
            return null;
        }

        Api api = (Api) ctClass.getAnnotation(Api.class);
        return (api == null) ? findApi(ctClass.getSuperclass()) : api;
    }

    protected ApiMethod findApiMethod(CtMethod ctMethod) throws Exception {
        if (ctMethod == null) {
            return null;
        }

        ApiMethod method = (ApiMethod) ctMethod.getAnnotation(ApiMethod.class);
        if (method != null) {
            return method;
        }

        CtClass declaringClass = ctMethod.getDeclaringClass();
        CtClass superClass = declaringClass.getSuperclass();
        if (superClass == null) {
            return null;
        }
        return findApiMethod(findCtMethod(superClass, ctMethod));
    }

    protected CtMethod findCtMethod(CtClass clazz, CtMethod method) throws Exception {
        if (clazz == null) {
            return null;
        }

        try {
            return clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
        } catch (NotFoundException ignored) {
        }
        return findCtMethod(clazz.getSuperclass(), method);
    }

    private Annotation createPathAnnotation(String urlPath) {
        return createAnnotation(Path.class, memberValueOf(urlPath));
    }

    private void convertApiMethodAnnotation(CtMethod method, ApiMethod apiMethod) {
        Class<? extends java.lang.annotation.Annotation> httpMethod = HTTP_METHODS.get(apiMethod.httpMethod());
        if (httpMethod == null) {
            return;
        }

        String path = apiMethod.path();
        if (path == null || path.isEmpty()) {
            path = method.getName();
        }

        addAnnotationsToMethod(method, Arrays.asList(
            createAnnotation(httpMethod),
            createProducesAnnotation(MediaType.APPLICATION_JSON),
            createPathAnnotation(path)
        ));

        convertAnnotationsOnParameters(method, path);
    }

    private void convertAnnotationsOnParameters(CtMethod method, String path) {
        ParameterAnnotationsAttribute attributeInfo = (ParameterAnnotationsAttribute) method.getMethodInfo().getAttribute(ParameterAnnotationsAttribute.visibleTag);
        if (attributeInfo == null) {
            return;
        }
        Annotation[][] paramArrays = attributeInfo.getAnnotations();
        for (int i = 0; i < paramArrays.length; i++) {
            Annotation[] paramAnnotations = paramArrays[i];
            for (Annotation paramAnnotation : paramAnnotations) {
                if (paramAnnotation.getTypeName().equals(Named.class.getName())) {
                    MemberValue value = paramAnnotation.getMemberValue("value");
                    String paramName = ((StringMemberValue) value).getValue();
                    Annotation param = createAnnotation(isPathParam(path, paramName) ? PathParam.class : QueryParam.class, value);
                    paramAnnotations = addToArray(paramAnnotations, param);
                    paramArrays[i] = paramAnnotations;
                }
            }
        }
        attributeInfo.setAnnotations(paramArrays);
    }

    private boolean isPathParam(String path, String paramName) {
        return path.contains("{" + paramName + "}");
    }

    private Annotation createProducesAnnotation(String value) {
        StringMemberValue element = memberValueOf(value);
        ArrayMemberValue array = createSingleElementArrayMemberValue(String.class, element);
        return createAnnotation(Produces.class, array);
    }

    private Annotation[] addToArray(Annotation[] paramAnnotations, Annotation queryParam) {
        Annotation newParamAnnotations[] = new Annotation[paramAnnotations.length + 1];
        System.arraycopy(paramAnnotations, 0, newParamAnnotations, 0, paramAnnotations.length);
        newParamAnnotations[newParamAnnotations.length - 1] = queryParam;
        return newParamAnnotations;
    }
}
