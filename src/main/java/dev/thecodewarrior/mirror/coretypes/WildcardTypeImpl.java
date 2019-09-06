/*
 * License: Apache License, Version 2.0
 * See the NOTICE file in the root directory or at <a href="http://www.apache.org/licenses/LICENSE-2">apache.org</a>.
 */

package dev.thecodewarrior.mirror.coretypes;

import dev.thecodewarrior.mirror.coretypes.CoreTypeUtils;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

class WildcardTypeImpl implements WildcardType {
    private final Type[] upperBounds;
    private final Type[] lowerBounds;

    WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
        this.upperBounds = upperBounds;
        this.lowerBounds = lowerBounds;
    }

    public Type[] getUpperBounds() {
        return upperBounds.clone();
    }

    public Type[] getLowerBounds() {
        return lowerBounds.clone();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WildcardType)) {
            return false;
        }
        WildcardType that = (WildcardType) other;
        return Arrays.equals(this.getLowerBounds(), that.getLowerBounds()) && Arrays.equals(this.getUpperBounds(), that.getUpperBounds());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.getLowerBounds()) ^ Arrays.hashCode(this.getUpperBounds());
    }

    @Override
    public String toString() {
        if (lowerBounds.length > 0) {
            return "? super " + CoreTypeUtils.getTypeName(lowerBounds[0]);
        } else if (upperBounds[0] == Object.class) {
            return "?";
        } else {
            return "? extends " + CoreTypeUtils.getTypeName(upperBounds[0]);
        }
    }
}
