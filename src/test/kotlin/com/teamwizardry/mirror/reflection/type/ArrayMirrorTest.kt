package com.teamwizardry.mirror.reflection.type

import com.teamwizardry.mirror.reflection.Mirror
import com.teamwizardry.mirror.reflection.MirrorTestBase
import com.teamwizardry.mirror.reflection.javaType
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.lang.reflect.GenericArrayType

internal class ArrayMirrorTest: MirrorTestBase() {

    @Test
    fun getRawClass_onArray_shouldReturnArrayType() {
        val type = Mirror.reflect<Array<Any>>() as ArrayMirror
        assertEquals(javaType<Array<Any>>(), type.rawType)
    }

    @Test
    fun getComponent_onObjectArray_shouldReturnComponentType() {
        val type = Mirror.reflect<Array<String>>() as ArrayMirror
        assertEquals(Mirror.reflect<String>(), type.component)
    }

    @Test
    fun getComponent_onPrimitiveArray_shouldReturnPrimitiveComponent() {
        val type = Mirror.reflect<IntArray>() as ArrayMirror
        assertEquals(Mirror.reflect(Int::class.javaPrimitiveType!!), type.component)
    }

    @Test
    fun getComponent_onGenericArray_shouldReturnVariable() {
        class FieldHolder<T> {
            @JvmField
            val field: Array<T>? = null
        }
        val genericArray = FieldHolder::class.java.getField("field").genericType as GenericArrayType
        val type = Mirror.reflect(genericArray) as ArrayMirror
        assertEquals(Mirror.reflect(genericArray.genericComponentType), type.component)
    }

    @Test
    fun getRaw_onObjectType_shouldReturnItself() {
        val type = Mirror.reflect<Array<String>>() as ArrayMirror
        assertEquals(type, type.raw)
    }
}