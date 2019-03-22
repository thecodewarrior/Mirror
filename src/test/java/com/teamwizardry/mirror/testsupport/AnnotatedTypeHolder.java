package com.teamwizardry.mirror.testsupport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AnnotatedTypeHolder {
    @NotNull
    protected Map<String, String> methods = new HashMap<>();

    @NotNull
    public AnnotatedType get(String name) {
        @Nullable String methodName = methods.get(name);
        if(methodName != null) {
            for(Method method : this.getClass().getDeclaredMethods()) {
                if(method.getName().equals(methodName)) {
                    return method.getAnnotatedParameterTypes()[0];
                }
            }
            throw new IllegalArgumentException("No such method found: key=" + name + ", method=" + methodName);
        }
        throw new IllegalArgumentException("No such key found: " + name);
    }
}
