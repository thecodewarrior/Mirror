package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.annotations.TypeAnnotation1
import com.teamwizardry.mirror.annotations.TypeAnnotationArg1
import com.teamwizardry.mirror.testsupport.GenericObject1
import com.teamwizardry.mirror.testsupport.GenericObject1Sub
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.Object1
import com.teamwizardry.mirror.testsupport.Object1Sub
import com.teamwizardry.mirror.testsupport.Object1Sub2
import com.teamwizardry.mirror.testsupport.Object2
import com.teamwizardry.mirror.typeToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType
import java.util.Date

internal class TypeMirrorTest: MirrorTestBase() {
    private val holder = TypeMirrorTestAnnotatedTypes()

    @Test
    @DisplayName("Reflecting a non-array class should return a ClassMirror")
    fun reflect_shouldReturnClassMirror_whenPassedClass() {
        assertEquals(ClassMirror::class.java, Mirror.reflect(Any::class.java).javaClass)
    }

    @Test
    @DisplayName("Reflecting an array class should return an ArrayMirror")
    fun reflect_shouldReturnArrayMirror_whenPassedArray() {
        assertEquals(ArrayMirror::class.java, Mirror.reflect(typeToken<Array<Any>>()).javaClass)
    }

    @Test
    @DisplayName("Reflecting a generic array should return an ArrayMirror")
    fun reflect_shouldReturnArrayMirror_whenPassedGenericArray() {
        class FieldHolder<T>(
            val field: Array<T>
        )

        val genericArrayType = FieldHolder::class.java.getDeclaredField("field").type
        assertEquals(ArrayMirror::class.java, Mirror.reflect(genericArrayType).javaClass)
    }

    @Test
    @DisplayName("Reflecting a type variable should return a VariableMirror")
    fun reflect_shouldReturnVariableMirror_whenPassedVariable() {
        class FieldHolder<T>(
            val field: T
        )

        val typeVariable = FieldHolder::class.java.getDeclaredField("field").genericType
        assertEquals(VariableMirror::class.java, Mirror.reflect(typeVariable).javaClass)
    }

    @Test
    @DisplayName("Reflecting a wildcard type should return a WildcardMirror")
    fun reflect_shouldReturnWildcardMirror_whenPassedWildcard() {
        class FieldHolder(
            @JvmField
            var field: Comparable<Date>
        )

        val wildcard = (FieldHolder::class.java.getField("field").genericType as ParameterizedType).actualTypeArguments[0]
        assertEquals(WildcardMirror::class.java, Mirror.reflect(wildcard).javaClass)
    }

    @Test
    @DisplayName("Reflecting void should return a VoidMirror")
    fun reflect_shouldReturnVoidMirror_whenPassedVoid() {
        assertEquals(VoidMirror::class.java, Mirror.reflect(Void.TYPE).javaClass)
    }

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

    @Test
    @DisplayName("Reflecting a self-referential type should not infinitely recurse")
    fun reflect_onSelfReferentialType_shouldNotRecurse() {
        class TestType<T: TestType<T>>: GenericObject1<TestType<T>>()

        Mirror.reflect(typeToken<TestType<*>>())
    }

    @Test
    fun specificity_ofSelf_shouldBeEqual() {
        val type = Mirror.reflect<Object1>().specificity
        assertEquals(0, type.compareTo(type))
    }

    @Test
    fun specificity_ofSeparateTypes_shouldBeEqual() {
        val type1 = Mirror.reflect<Object1>().specificity
        val type2 = Mirror.reflect<Object2>().specificity
        assertEquals(0, type1.compareTo(type2))
        assertEquals(0, type2.compareTo(type1))
    }

    @Test
    fun specificity_ofSeparateSubTypes_shouldBeEqual() {
        val type1 = Mirror.reflect<Object1Sub>().specificity
        val type2 = Mirror.reflect<Object1Sub2>().specificity
        assertEquals(0, type1.compareTo(type2))
        assertEquals(0, type2.compareTo(type1))
    }

    @Test
    fun specificity_ofSubclass_shouldBeGreater() {
        val superClass = Mirror.reflect<Object1>().specificity
        val subClass = Mirror.reflect<Object1Sub>().specificity
        assertEquals(-1, superClass.compareTo(subClass))
        assertEquals(1, subClass.compareTo(superClass))
    }

    @Test
    fun specificity_ofSpecifiedGeneric_shouldBeGreater() {
        val unspecified = Mirror.reflect(GenericObject1::class.java).specificity
        val specified = Mirror.reflect<GenericObject1<String>>().specificity
        assertEquals(-1, unspecified.compareTo(specified))
        assertEquals(1, specified.compareTo(unspecified))
    }

    @Test
    fun specificity_ofSubclass_shouldBeEqualTo_specifiedGeneric() {
        val subclass = Mirror.reflect(GenericObject1Sub::class.java).specificity
        val specified = Mirror.reflect<GenericObject1<String>>().specificity
        assertEquals(0, specified.compareTo(subclass))
        assertEquals(0, subclass.compareTo(specified))
    }

    @Test
    fun specificity_ofIncompatibleGenerics_shouldBeEqual() {
        val list1 = Mirror.reflect<List<Object1>>().specificity
        val list2 = Mirror.reflect<ArrayList<Object2>>().specificity
        assertEquals(0, list1.compareTo(list2))
        assertEquals(0, list2.compareTo(list1))
    }
}