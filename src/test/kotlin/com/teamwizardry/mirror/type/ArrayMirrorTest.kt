package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.typeToken
import com.teamwizardry.mirror.typeholders.TypeMirrorHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ArrayMirrorTest: MirrorTestBase() {
    val holder = TypeMirrorHolder()

    @Test
    fun getRawClass_onArray_shouldReturnArrayType() {
        val type = Mirror.reflect<Array<Any>>() as ArrayMirror
        assertEquals(typeToken<Array<Any>>(), type.java)
    }

    @Test
    fun getComponent_onObjectArray_shouldReturnComponentType() {
        val type = Mirror.reflect<Array<String>>() as ArrayMirror
        assertEquals(Mirror.reflect<String>(), type.component)
    }

    @Test
    fun getComponent_onPrimitiveArray_shouldReturnPrimitiveComponent() {
        val type = Mirror.reflect<IntArray>() as ArrayMirror
        val component = Mirror.reflect(Int::class.javaPrimitiveType!!)
        assertEquals(component, type.component)
    }

    @Test
    fun getComponent_onGenericArray_shouldReturnVariable() {
        val type = Mirror.reflect(holder["T[]; T", 0]) as ArrayMirror
        assertEquals(Mirror.reflect(holder["T[]; T", 1]), type.component)
    }

    @Test
    fun getRaw_onObjectType_shouldReturnItself() {
        val type = Mirror.reflect<Array<String>>() as ArrayMirror
        assertEquals(type, type.raw)
    }

    @Test
    fun getRaw_onGenericType_shouldReturnErasure() {
        val type = Mirror.reflect<Array<List<String>>>() as ArrayMirror
        assertEquals(type.raw.component, Mirror.reflect(List::class.java))
    }
}