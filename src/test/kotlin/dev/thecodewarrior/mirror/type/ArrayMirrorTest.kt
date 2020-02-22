package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import dev.thecodewarrior.mirror.typeToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ArrayMirrorTest: MTest() {

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
        val sources = TestSources()
        val types = sources.types {
            typeVariables("T") {
                +"T"
                +"T[]"
            }
        }
        sources.compile()
        val type = Mirror.reflect(types["T[]"]) as ArrayMirror
        assertEquals(Mirror.reflect(types["T"]), type.component)
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