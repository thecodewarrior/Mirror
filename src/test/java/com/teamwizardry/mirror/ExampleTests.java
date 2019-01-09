package com.teamwizardry.mirror;

import com.teamwizardry.mirror.testsupport.FuckingTypeAnnotation;
import com.teamwizardry.mirror.testsupport.Object1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

class ExampleTests {

    static class CustomHalfMap<T> extends HashMap<String, T> { }
    static class CustomMap extends CustomHalfMap<List<String>> { }

    @Test
    @DisplayName("Specializing a class should specialize the return types of each of its declared methods")
    void specializeFields() {

//        TypeToken token = new TypeToken<GenericObject1<Object1>>() {};
//        ParameterizedType type = (ParameterizedType)token.get();
//        ArrayList fuck = new ArrayList();
//        fuck.add(Object1.class);
//        ArrayList thisShit = new ArrayList();
//        thisShit.addAll(Arrays.asList(type.getActualTypeArguments()));
//        Assertions.assertEquals(fuck, thisShit);


        TypeToken token = new TypeToken<@FuckingTypeAnnotation Object1>() {};
        AnnotatedType type = token.getAnnotated();
        Assertions.assertEquals(
                Arrays.asList(Mirror.newAnnotation(FuckingTypeAnnotation.class)),
                Arrays.asList(type.getDeclaredAnnotations())
        );
    }
}
