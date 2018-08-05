package com.teamwizardry.mirror

import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.type.ArrayMirror
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.VariableMirror
import com.teamwizardry.mirror.type.WildcardMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType
import java.util.*

internal class MirrorTest: MirrorTestBase() {

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
        class FieldHolder<T> {
            val field: Array<T>? = null
        }

        val genericArrayType = FieldHolder::class.java.getDeclaredField("field").type
        assertEquals(ArrayMirror::class.java, Mirror.reflect(genericArrayType).javaClass)
    }

    @Test
    @DisplayName("Reflecting a type variable should return a VariableMirror")
    fun reflect_shouldReturnVariableMirror_whenPassedVariable() {
        class FieldHolder<T> {
            val field: T? = null
        }

        val typeVariable = FieldHolder::class.java.getDeclaredField("field").genericType
        assertEquals(VariableMirror::class.java, Mirror.reflect(typeVariable).javaClass)
    }

    @Test
    @DisplayName("Reflecting a wildcard type should return a WildcardMirror")
    fun reflect_shouldReturnWildcardMirror_whenPassedWildcard() {
        class FieldHolder {
            @JvmField
            var field: Comparable<Date>? = null
        }

        val wildcard = (FieldHolder::class.java.getField("field").genericType as ParameterizedType).actualTypeArguments[0]
        assertEquals(WildcardMirror::class.java, Mirror.reflect(wildcard).javaClass)
    }

    @Test
    @DisplayName("Reflecting a field should return a field mirror with the correct name and type")
    fun reflectingBasicField() {
        class FieldHolder {
            @JvmField
            var field: String? = null
        }

        val field = FieldHolder::class.java.getField("field")

        val fieldMirror = Mirror.reflect(field)
        assertEquals("field", fieldMirror.name)
        val fieldType = fieldMirror.type
        assertEquals(Mirror.reflect<String>(), fieldType)
    }
}
