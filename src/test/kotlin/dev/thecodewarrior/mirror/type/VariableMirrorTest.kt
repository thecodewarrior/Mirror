package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import dev.thecodewarrior.mirror.testsupport.assertSameList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class VariableMirrorTest: MTest() {
    val sources = TestSources()

    val A by sources.add("A", "@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.RUNTIME) @interface A {}")
    val I1 by sources.add("I1", "interface I1 {}")
    val I2 by sources.add("I2", "interface I2 {}")
    val X by sources.add("X", "class X {}")
    val types = sources.types {
        typeVariables("T") {
            add("T", "T")
        }
        typeVariables("T extends X") {
            add("T extends X", "T")
        }
        typeVariables("T extends I1 & I2") {
            add("T extends I1 & I2", "T")
        }
        typeVariables("T extends @A X") {
            add("T extends @A X", "T")
        }
        +"@A X"
    }

    init {
        sources.compile()
    }

    @Test
    fun getBounds_onUnboundedType_shouldReturnListOfObject() {
        val type = Mirror.reflect(types["T"]) as VariableMirror
        assertEquals(listOf(Mirror.reflect<Any>()), type.bounds)
    }

    @Test
    fun getBounds_onTypeWithSingleBound_shouldReturnListOfBound() {
        val type = Mirror.reflect(types["T extends X"]) as VariableMirror
        assertEquals(listOf(Mirror.reflect(X)), type.bounds)
    }

    @Test
    fun getBounds_onTypeWithMultipleBounds_shouldReturnListOfBoundsInSourceOrder() {
        val type = Mirror.reflect(types["T extends I1 & I2"]) as VariableMirror
        assertSameList(listOf(Mirror.reflect(I1), Mirror.reflect(I2)), type.bounds)
    }

    @Test
    fun getBounds_onTypeWithAnnotatedBound_shouldReturnListOfAnnotatedBound() {
        val type = Mirror.reflect(types["T extends @A X"]) as VariableMirror
        assertEquals(listOf(Mirror.reflect(types["@A X"])), type.bounds)
    }
}