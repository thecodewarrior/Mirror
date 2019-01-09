package com.teamwizardry.mirror.testsupport;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;

public class AnnotatedTypeHolder {
    public AnnotatedType get(String name) {
        Method[] methods = this.getClass().getDeclaredMethods();
        for(Method method : methods) {
            if(method.getName().equals(name)) {
                return method.getAnnotatedParameterTypes()[0];
            }
        }
        throw new IllegalArgumentException("No such method found: " + name);
    }
}
