package org.jboss.capedwarf.bytecode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

/**
 *
 */
public abstract class Annotator {
    private CtClass clazz;
    private ClassFile classFile;
    private ConstPool constPool;

    public Annotator(CtClass clazz) {
        this.clazz = clazz;
        classFile = clazz.getClassFile();
        constPool = classFile.getConstPool();
    }

    protected CtClass getClazz() {
        return clazz;
    }

    protected ClassFile getClassFile() {
        return classFile;
    }

    protected ConstPool getConstPool() {
        return constPool;
    }

    protected ClassLoader getClassLoader() {
        return clazz.getClassPool().getClassLoader();
    }

    public abstract void addAnnotations() throws Exception;

    protected EnumMemberValue createEnumMemberValue(Enum enumValue) {
        int enumClassIndex = constPool.addUtf8Info(enumValue.getDeclaringClass().getName());
        int enumValueIndex = constPool.addUtf8Info(enumValue.name());
        return new EnumMemberValue(enumClassIndex, enumValueIndex, constPool);
    }

    protected ClassMemberValue createClassMemberValue(Class<?> clazz) {
        return createClassMemberValue(clazz.getName());
    }

    protected ClassMemberValue createClassMemberValue(String clazz) {
        return new ClassMemberValue(clazz, constPool);
    }

    @SuppressWarnings("unchecked")
    protected void addAnnotationsToClass(Class<? extends java.lang.annotation.Annotation>... annotationClasses) {
        addAnnotationsToClass(createAnnotations(annotationClasses));
    }

    protected void addAnnotationsToClass(Annotation... annotations) {
        AnnotationsAttribute aa = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
        if (aa == null) {
            classFile.addAttribute(createAnnotationAttribute(annotations));
        } else {
            addAnnotations(aa, annotations);
        }
    }

    protected void addAnnotationsToMethod(String methodName, Annotation... annotations) throws NotFoundException {
        addAnnotationsToMethod(clazz.getDeclaredMethod(methodName), annotations);
    }

    protected void addAnnotationsToMethod(CtMethod method, Collection<Annotation> annotations) {
        addAnnotationsToMethod(method, annotations.toArray(new Annotation[annotations.size()]));
    }

    protected void addAnnotationsToMethod(CtMethod method, Annotation... annotations) {
        MethodInfo methodInfo = method.getMethodInfo();
        AnnotationsAttribute aa = (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag);
        if (aa == null) {
            methodInfo.addAttribute(createAnnotationAttribute(annotations));
        } else {
            addAnnotations(aa, annotations);
        }
    }

    protected void addAnnotationsToField(String fieldName, Annotation... annotations) throws NotFoundException {
        clazz.getDeclaredField(fieldName).getFieldInfo().addAttribute(createAnnotationAttribute(annotations));
    }

    private Annotation[] createAnnotations(Class<? extends java.lang.annotation.Annotation>[] annotationClasses) {
        List<Annotation> annotationList = new LinkedList<Annotation>();
        for (Class<? extends java.lang.annotation.Annotation> annotationClass : annotationClasses) {
            annotationList.add(createAnnotation(annotationClass));
        }
        return annotationList.toArray(new Annotation[annotationList.size()]);
    }

    protected Annotation createAnnotation(Class<? extends java.lang.annotation.Annotation> annotationClass) {
        return new Annotation(annotationClass.getName(), constPool);
    }

    protected Annotation createAnnotation(Class<? extends java.lang.annotation.Annotation> annotationClass, MemberValue value) {
        return createAnnotation(annotationClass, "value", value);
    }

    protected Annotation createAnnotation(Class<? extends java.lang.annotation.Annotation> annotationClass, String name, MemberValue value) {
        Annotation queryParam = createAnnotation(annotationClass);
        queryParam.addMemberValue(name, value);
        return queryParam;
    }

    private AnnotationsAttribute createAnnotationAttribute(Annotation... annotations) {
        AnnotationsAttribute attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        addAnnotations(attribute, annotations);
        return attribute;
    }

    private void addAnnotations(AnnotationsAttribute attribute, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            attribute.addAnnotation(annotation);
        }
    }

    protected StringMemberValue memberValueOf(String value) {
        return new StringMemberValue(value, getConstPool());
    }

    protected ArrayMemberValue createSingleElementArrayMemberValue(Class<?> arrayType, MemberValue element) {
        ArrayMemberValue array = new ArrayMemberValue(createClassMemberValue(arrayType), getConstPool());
        array.setValue(new MemberValue[]{element});
        return array;
    }
}
