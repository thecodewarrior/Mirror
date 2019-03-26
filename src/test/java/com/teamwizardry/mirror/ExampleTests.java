package com.teamwizardry.mirror;

import com.teamwizardry.mirror.annotations.TypeAnnotation1;
import com.teamwizardry.mirror.coretypes.AnnotationFormatException;
import com.teamwizardry.mirror.testsupport.Object1;
import com.teamwizardry.mirror.type.TypeMirror;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExampleTests {
    void holder(@TypeAnnotation1 Object1 holder) {}

    @Test
    @DisplayName("Here lie things I'm gonna test in java")
    void testInJava() {
        TypeMirror type = null;
        try {
            type = Mirror.reflect(ExampleTests.class.getDeclaredMethod("holder", Object1.class).getAnnotatedParameterTypes()[0]);
            assertEquals(Arrays.asList(
                    Mirror.newAnnotation(TypeAnnotation1.class)
            ), type.getTypeAnnotations());
        } catch (NoSuchMethodException | AnnotationFormatException e) {
            e.printStackTrace();
        }
    }
}
