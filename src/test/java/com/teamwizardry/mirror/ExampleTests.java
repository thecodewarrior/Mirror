package com.teamwizardry.mirror;

import com.teamwizardry.mirror.annotations.TypeAnnotation1;
import com.teamwizardry.mirror.testsupport.Object1;
import com.teamwizardry.mirror.type.TypeMirror;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class ExampleTests {
    void holder(@TypeAnnotation1 Object1 holder) {}

    @Test
    @DisplayName("Here lie things I'm gonna test in java")
    void testInJava() {
        TypeMirror type = null;
        List<TypeMirror> mirrors = Arrays.asList(
                Mirror.reflect(new TypeToken<List<Number>>() {}),
                Mirror.reflect(new TypeToken<List<Integer>>() {}),
                Mirror.reflect(new TypeToken<ArrayList<Integer>>() {}),
                Mirror.reflect(new TypeToken<List<Object>>() {})
        );
        mirrors.sort(Comparator.comparing(TypeMirror::getSpecificity));
        mirrors.size();
    }
}
