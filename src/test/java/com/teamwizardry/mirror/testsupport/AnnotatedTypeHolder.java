package com.teamwizardry.mirror.testsupport;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.*;

public class AnnotatedTypeHolder {
    @NotNull
    private Map<String, Map<Integer, AnnotatedType>> types = new HashMap<>();

    protected AnnotatedTypeHolder() {
        populateTypes();
    }

    @NotNull
    public AnnotatedType get(String name) {
        return get(name, 0);
    }

    @NotNull
    public AnnotatedType get(String name, int index) {
        Map<Integer, AnnotatedType> indexMap = types.get(name);
        if(indexMap == null) {
            throw new IllegalArgumentException("No such key found. key=`" + name + "`");
        }

        AnnotatedType type = indexMap.get(index);
        if(type == null)
            throw new IllegalArgumentException("No such index found. key=`" + name + "` index=" + index);

        return type;
    }

    private void populateTypes() {
        List<Field> fields = new ArrayList<>();
        List<Method> methods = new ArrayList<>();
        List<Parameter> parameters = new ArrayList<>();

        Class<?> clazz = this.getClass();
        while(clazz != null) {
            Collections.addAll(fields, clazz.getDeclaredFields());
            Collections.addAll(methods, clazz.getDeclaredMethods());
            for(Method method : clazz.getDeclaredMethods()) {
                Collections.addAll(parameters, method.getParameters());
            }

            clazz = clazz.getSuperclass();
        }

        for(Method method : methods) {
            TypeHolder holder = method.getDeclaredAnnotation(TypeHolder.class);
            if(holder != null) {
                if(holder.type() == HolderType.DIRECT) {
                    addType(holder.value(), holder.index(), method.getAnnotatedReturnType(),
                            method.getDeclaredAnnotation(Unwrap.class) != null);
                } else if(holder.type() == HolderType.PARAMS) {
                    int i = 0;
                    for(Parameter parameter : method.getParameters()) {
                        addType(holder.value(), holder.index() + i, parameter.getAnnotatedType(),
                                parameter.getDeclaredAnnotation(Unwrap.class) != null);
                        i++;
                    }
                }
            }
        }

        for(Field field : fields) {
            TypeHolder holder = field.getDeclaredAnnotation(TypeHolder.class);
            if(holder != null) {
                addType(holder.value(), holder.index(), field.getAnnotatedType(),
                        field.getDeclaredAnnotation(Unwrap.class) != null);
            }
        }

        for(Parameter parameter : parameters) {
            TypeHolder holder = parameter.getDeclaredAnnotation(TypeHolder.class);
            if(holder != null) {
                addType(holder.value(), holder.index(), parameter.getAnnotatedType(),
                        parameter.getDeclaredAnnotation(Unwrap.class) != null);
            }
        }
    }

    private void addType(String name, int index, AnnotatedType type, boolean unwrap) {
        Map<Integer, AnnotatedType> indexMap;
        if(types.containsKey(name)) {
            indexMap = types.get(name);
        } else {
            indexMap = new HashMap<>();
            types.put(name, indexMap);
        }

        if(unwrap) {
            if(type instanceof AnnotatedParameterizedType) {
                int i = 0;
                for(AnnotatedType arg : ((AnnotatedParameterizedType) type).getAnnotatedActualTypeArguments()) {
                    indexMap.put(index + i, arg);
                    i++;
                }
            } else {
                throw new IllegalArgumentException("Couldn't unwrap type of class " + type.getClass().getSimpleName());
            }
        }
        indexMap.put(index, type);
    }

    protected enum HolderType {
        PARAMS,
        DIRECT;
    }

    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface TypeHolder {
        String value();
        int index() default 0;
        HolderType type() default HolderType.PARAMS;
    }

    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Unwrap {
    }
}
