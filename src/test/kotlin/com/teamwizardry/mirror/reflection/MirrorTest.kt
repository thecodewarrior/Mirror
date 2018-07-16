package com.teamwizardry.mirror.reflection

import com.teamwizardry.mirror.reflection.type.ArrayMirror
import com.teamwizardry.mirror.reflection.type.ClassMirror
import com.teamwizardry.mirror.reflection.type.VariableMirror
import com.teamwizardry.mirror.reflection.type.WildcardMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType
import java.util.*

internal class MirrorTest: MirrorTestBase() {

    @Test
    fun reflect_shouldReturnClassMirror_whenPassedClass() {
        assertEquals(ClassMirror::class.java, Mirror.reflect(Any::class.java).javaClass)
    }

    @Test
    fun reflect_shouldReturnArrayMirror_whenPassedArray() {
        assertEquals(ArrayMirror::class.java, Mirror.reflect(javaType<Array<Any>>()).javaClass)
    }

    @Test
    fun reflect_shouldReturnArrayMirror_whenPassedGenericArray() {
        class FieldHolder<T> {
            val field: Array<T>? = null
        }

        val genericArrayType = FieldHolder::class.java.getDeclaredField("field").type
        assertEquals(ArrayMirror::class.java, Mirror.reflect(genericArrayType).javaClass)
    }

    @Test
    fun reflect_shouldReturnVariableMirror_whenPassedVariable() {
        class FieldHolder<T> {
            val field: T? = null
        }

        val typeVariable = FieldHolder::class.java.getDeclaredField("field").genericType
        assertEquals(VariableMirror::class.java, Mirror.reflect(typeVariable).javaClass)
    }

    @Test
    fun reflect_shouldReturnWildcardMirror_whenPassedWildcard() {
        class FieldHolder {
            @JvmField
            var field: Comparable<Date>? = null
        }

        val wildcard = (FieldHolder::class.java.getField("field").genericType as ParameterizedType).actualTypeArguments[0]
        assertEquals(WildcardMirror::class.java, Mirror.reflect(wildcard).javaClass)
    }
}
