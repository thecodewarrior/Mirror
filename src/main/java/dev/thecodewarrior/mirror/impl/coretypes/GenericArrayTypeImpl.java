/*
 * License: Apache License, Version 2.0
 * See the NOTICE file in the root directory or at <a href="http://www.apache.org/licenses/LICENSE-2">apache.org</a>.
 */

package dev.thecodewarrior.mirror.impl.coretypes;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

class GenericArrayTypeImpl implements GenericArrayType {
    private Type componentType;

    GenericArrayTypeImpl(Type componentType) {
        super();
        this.componentType = componentType;
    }

    static Class<?> createArrayType(Class<?> componentType) {
        // there's no (clean) other way to create a array class, than creating an instance of it
        return Array.newInstance(componentType, 0).getClass();
    }

    static Type createArrayType(Type componentType) {
        if (componentType instanceof Class) {
            return createArrayType((Class<?>) componentType);
        } else {
            return new GenericArrayTypeImpl(componentType);
        }
    }

    public Type getGenericComponentType() {
        return componentType;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GenericArrayType
                && Objects.equals(this.componentType, ((GenericArrayType) other).getGenericComponentType());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.componentType);
    }

    @Override
    public String toString() {
        return componentType + "[]";
    }
}
