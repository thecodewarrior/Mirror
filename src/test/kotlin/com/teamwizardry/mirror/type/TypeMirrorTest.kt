package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.annotations.TypeAnnotation1
import com.teamwizardry.mirror.annotations.TypeAnnotationArg1
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class TypeMirrorTest: MirrorTestBase() {
    private val HOLDER = TypeMirrorTestAnnotatedTypes()
    @Test
    @DisplayName("Getting the annotations of an unannotated type should return an empty list")
    fun getAnnotation_ofUnannotatedType_shouldReturnEmptyList() {
        val type = Mirror.reflect(HOLDER["unannotated"])
        assertEquals(emptyList<Annotation>(), type.annotations)
    }

    @Test
    @DisplayName("Getting the annotations of type with one annotation should return that annotation")
    fun getAnnotation_ofAnnotatedType_shouldReturnAnnotation() {
        val type = Mirror.reflect(HOLDER["annotated"])
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>()
        ), type.annotations)
    }

    @Test
    @DisplayName("Getting the annotations of a type with multiple annotations should return the correct annotations")
    fun getAnnotation_ofMultiAnnotatedType_shouldReturnAnnotations() {
        val type = Mirror.reflect(HOLDER["multiAnnotated"])
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>(),
            Mirror.newAnnotation<TypeAnnotationArg1>(mapOf("arg" to 1))
        ), type.annotations)
    }
}