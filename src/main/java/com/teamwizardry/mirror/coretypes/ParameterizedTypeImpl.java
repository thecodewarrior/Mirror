/*
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or at <a href="http://www.apache.org/licenses/LICENSE-2">apache.org</a>.
 */

package com.teamwizardry.mirror.coretypes;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

class ParameterizedTypeImpl implements ParameterizedType {
    private final Class<?> rawType;
    private final Type[] actualTypeArguments;
    private final Type ownerType;

    ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
        this.ownerType = ownerType;
    }

    public Type getRawType() {
        return rawType;
    }

    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ParameterizedType)) return false;

        ParameterizedType that = (ParameterizedType) other;
        return this == that ||
                Objects.equals(this.ownerType, that.getOwnerType())
                        && Objects.equals(this.rawType, that.getRawType())
                        && Arrays.equals(this.actualTypeArguments, that.getActualTypeArguments());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.actualTypeArguments) ^ Objects.hashCode(this.ownerType) ^ Objects.hashCode(this.rawType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        String clazz = rawType.getName();

        if (ownerType != null) {
            sb.append(GenericTypeReflector.getTypeName(ownerType)).append('.');

            String prefix = (ownerType instanceof ParameterizedType) ? ((Class<?>) ((ParameterizedType) ownerType).getRawType()).getName() + '$'
                    : ((Class<?>) ownerType).getName() + '$';
            if (clazz.startsWith(prefix))
                clazz = clazz.substring(prefix.length());
        }
        sb.append(clazz);

        if (actualTypeArguments.length != 0) {
            sb.append('<');
            for (int i = 0; i < actualTypeArguments.length; i++) {
                Type arg = actualTypeArguments[i];
                if (i != 0)
                    sb.append(", ");
                sb.append(GenericTypeReflector.getTypeName(arg));
            }
            sb.append('>');
        }

        return sb.toString();
    }
}
