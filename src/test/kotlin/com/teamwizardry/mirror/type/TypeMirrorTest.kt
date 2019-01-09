package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.annotations.TypeAnnotation1
import com.teamwizardry.mirror.annotations.TypeAnnotationArg1
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class TypeMirrorTest: MirrorTestBase() {
    private val holder = TypeMirrorTestAnnotatedTypes()

    @Test
    @DisplayName("Getting the annotations of an unannotated type should return an empty list")
    fun getAnnotation_ofUnannotatedType_shouldReturnEmptyList() {
        val type = Mirror.reflect(holder["getAnnotation_ofUnannotatedType_shouldReturnEmptyList_1"])
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of type with one annotation should return that annotation")
    fun getAnnotation_ofAnnotatedType_shouldReturnAnnotation() {
        val type = Mirror.reflect(holder["getAnnotation_ofAnnotatedType_shouldReturnAnnotation_1"])
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>()
        ), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of a type with multiple annotations should return the correct annotations")
    fun getAnnotation_ofMultiAnnotatedType_shouldReturnAnnotations() {
        val type = Mirror.reflect(holder["getAnnotation_ofMultiAnnotatedType_shouldReturnAnnotations_1"])
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>(),
            Mirror.newAnnotation<TypeAnnotationArg1>(mapOf("arg" to 1))
        ), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of an annotated type parameter should return the correct annotations")
    fun getAnnotation_ofAnnotatedTypeParameter_shouldReturnAnnotations() {
        val outer = Mirror.reflect(holder["getAnnotation_ofAnnotatedTypeParameter_shouldReturnAnnotations_1"]) as ClassMirror
        val type = outer.typeParameters[0]
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>()
        ), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of an annotated array component should return the correct annotations")
    fun getAnnotation_ofAnnotatedArrayComponent_shouldReturnAnnotations() {
        val array = Mirror.reflect(holder["getAnnotation_ofAnnotatedArrayComponent_shouldReturnAnnotations_1"]) as ArrayMirror
        val type = array.component
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>()
        ), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of an array with an annotated component should return an empty list")
    fun getAnnotation_ofArrayWithAnnotatedComponent_shouldReturnEmptyList() {
        val type = Mirror.reflect(holder["getAnnotation_ofArrayWithAnnotatedComponent_shouldReturnAnnotations_1"]) as ArrayMirror
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of an annotated array with an unannotated component should return the correct annotations")
    fun getAnnotation_ofAnnotatedArrayWithUnannotatedComponent_shouldReturnAnnotations() {
        val type = Mirror.reflect(holder["getAnnotation_ofAnnotatedArrayWithUnannotatedComponent_shouldReturnAnnotations_1"]) as ArrayMirror
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>()
        ), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of the unannotated component of an annotated array should return an empty list")
    fun getAnnotation_ofUnannotatedComponentOfAnnotatedArray_shouldReturnEmptyList() {
        val array = Mirror.reflect(holder["getAnnotation_ofUnannotatedComponentOfAnnotatedArray_shouldReturnAnnotations_1"]) as ArrayMirror
        val type = array.component
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }
}