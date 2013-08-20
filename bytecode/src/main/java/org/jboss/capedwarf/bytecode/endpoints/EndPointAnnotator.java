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

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import javassist.CtClass;
import javassist.CtMethod;
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

    public static final String DEFAULT_NAME = "myApi";
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
        Api api = (Api) getClazz().getAnnotation(Api.class);
        if (api == null) {
            return;
        }

        addAnnotationsToClass(
            createPathAnnotation("" // TODO: api.root()
                + "/" + (api.name().isEmpty() ? DEFAULT_NAME : api.name())
                + "/" + (api.version().isEmpty() ? DEFAULT_VERSION : api.version())));

        for (CtMethod method : getClazz().getDeclaredMethods()) {
            ApiMethod apiMethod = (ApiMethod) method.getAnnotation(ApiMethod.class);
            if (apiMethod != null) {
                convertApiMethodAnnotation(method, apiMethod);
            }
        }
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
            createProducesAnnotation("application/json"),
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
