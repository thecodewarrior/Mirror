package com.teamwizardry.mirror;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

class ExampleTests {

    static class CustomHalfMap<T> extends HashMap<String, T> { }
    static class CustomMap extends CustomHalfMap<List<String>> { }

    @Test
    @DisplayName("Specializing a class should specialize the return types of each of its declared methods")
    void specializeFields() {
//        Class<?> clazz = CustomMap.class;
//
//        ClassMirror baseType = Mirror.reflectClass(clazz);
//        ClassMirror mapType = baseType.findSupertype(Map.class);
//        MethodMirror getMethod = mapType.methodByRaw("get", Object.class);
//
//        mapType.getTypeParameters().get(0); // == String
//        mapType.getTypeParameters().get(1); // == List<String>
//
//        getMethod.getParameters().get(0).getType(); // == String
//        getMethod.getReturnType(); // == List<String>
    }
}
