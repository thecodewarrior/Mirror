/*
 * License: Apache License, Version 2.0
 * See the NOTICE file in the root directory or at <a href="http://www.apache.org/licenses/LICENSE-2">apache.org</a>.
 */

package com.teamwizardry.mirror.coretypes;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TypeVariableImpl<D extends GenericDeclaration> implements TypeVariable<D> {

    private final Map<Class<? extends Annotation>, Annotation> annotations;
    private final D genericDeclaration;
    private final String name;
    private final AnnotatedType[] bounds;

    TypeVariableImpl(TypeVariable<D> variable, AnnotatedType[] bounds) {
        this(variable, variable.getAnnotations(), bounds);
    }

    TypeVariableImpl(TypeVariable<D> variable, Annotation[] annotations, AnnotatedType[] bounds) {
        Objects.requireNonNull(variable);
        this.genericDeclaration = variable.getGenericDeclaration();
        this.name = variable.getName();
        this.annotations = new HashMap<>();
        for (Annotation annotation : annotations) {
            this.annotations.put(annotation.annotationType(), annotation);
        }
        if (bounds == null || bounds.length == 0) {
            throw new IllegalArgumentException("There must be at least one bound. For an unbound variable, the bound must be Object");
        }
        this.bounds = bounds;
    }

    private static AnnotatedType[] annotateBounds(Type[] bounds) {
        if (bounds == null || bounds.length == 0) {
            throw new IllegalArgumentException("There must be at least one bound. For an unbound variable, the bound must be Object");
        }
        return Arrays.stream(bounds).map(CoreTypeUtils::annotate).toArray(AnnotatedType[]::new);
    }

    @Override
    public Type[] getBounds() {
        return Arrays.stream(this.bounds).map(AnnotatedType::getType).toArray(Type[]::new);
    }

    @Override
    public D getGenericDeclaration() {
        return this.genericDeclaration;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public AnnotatedType[] getAnnotatedBounds() {
        return this.bounds;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return (T) this.annotations.get(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations.values().toArray(new Annotation[0]);
    }

    //should this maybe return only annotations directly on the variable?
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getAnnotations();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof TypeVariable) {
            TypeVariable that = (TypeVariable) other;
            return Objects.equals(this.genericDeclaration, that.getGenericDeclaration()) && Objects.equals(this.name, that.getName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.genericDeclaration.hashCode() ^ this.name.hashCode();
    }

    @Override
    public String toString() {
        return annotationsString() + this.getName();
    }

    private String annotationsString() {
        return annotations.isEmpty() ? "" : annotations.values().stream()
                .map(Annotation::toString)
                .collect(Collectors.joining(", ")) + " ";
    }
}
