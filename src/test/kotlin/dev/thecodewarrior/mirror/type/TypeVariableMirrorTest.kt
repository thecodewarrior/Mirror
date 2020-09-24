package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.assertSameList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TypeVariableMirrorTest: MTest() {
    val A by sources.add("A", "@rt(TYPE_USE) @interface A {}")
    val B by sources.add("B", "@rt(TYPE_PARAMETER) @interface B {}")
    val I1 by sources.add("I1", "interface I1 {}")
    val I2 by sources.add("I2", "interface I2 {}")
    val X by sources.add("X", "class X {}")
    val types = sources.types {
        block("T") {
            +"@A T"
        }
        block("@B T") {
        }
        block("T extends X") {}
        block("T extends I1 & I2") {}
        block("T extends @A X") {}
        +"@A X"
    }

    @Test
    fun getBounds_onUnboundedType_shouldReturnListOfObject() {
        val type = Mirror.reflect(types["T"]) as TypeVariableMirror
        assertEquals(listOf(Mirror.reflect<Any>()), type.bounds)
    }

    @Test
    fun getBounds_onTypeWithSingleBound_shouldReturnListOfBound() {
        val type = Mirror.reflect(types["T extends X"]) as TypeVariableMirror
        assertEquals(listOf(Mirror.reflect(X)), type.bounds)
    }

    @Test
    fun getBounds_onTypeWithMultipleBounds_shouldReturnListOfBoundsInSourceOrder() {
        val type = Mirror.reflect(types["T extends I1 & I2"]) as TypeVariableMirror
        assertSameList(listOf(Mirror.reflect(I1), Mirror.reflect(I2)), type.bounds)
    }

    @Test
    fun getBounds_onTypeWithAnnotatedBound_shouldReturnListOfAnnotatedBound() {
        val type = Mirror.reflect(types["T extends @A X"]) as TypeVariableMirror
        assertEquals(listOf(Mirror.reflect(types["@A X"])), type.bounds)
    }

    @Test
    fun `'toString' of unannotated type variable should return its name`() {
        val type = Mirror.reflect(types["T"]) as TypeVariableMirror
        assertEquals("T", type.toString())
    }

    @Test
    fun `'toString' of annotated type variable use should return its type annotations then name`() {
        val type = Mirror.reflect(types["@A T"]) as TypeVariableMirror
        assertEquals("@gen.A() T", type.toString())
    }

    @Test
    fun `'toDeclarationString' of bounded type variable should return its name then bounds`() {
        val type = Mirror.reflect(types["T extends X"]) as TypeVariableMirror
        assertEquals("T extends gen.X", type.toDeclarationString())
    }

    @Test
    fun `'toDeclarationString' of annotated type variable declaration should return its name then bounds`() {
        val type = Mirror.reflect(types["@B T"]) as TypeVariableMirror
        assertEquals("@gen.B() T", type.toDeclarationString())
    }
}