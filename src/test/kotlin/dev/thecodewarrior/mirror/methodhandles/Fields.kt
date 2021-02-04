package dev.thecodewarrior.mirror.methodhandles

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.assertMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class Fields: MTest() {
    @Test
    fun `getting a static field with a null receiver should return its value`() {
        val X by sources.add("X", "class X { static int field = 10; }")
        sources.compile()
        val field = Mirror.reflect(X._f("field"))
        assertEquals(10, field.get(null))
    }

    @Test
    fun `getting a static field with a non-null receiver should not throw`() {
        val X by sources.add("X", "class X { static int field = 10; }")
        sources.compile()
        val instance = X._new<Any>()
        val field = Mirror.reflect(X._f("field"))
        assertDoesNotThrow {
            field.get<Int>(instance)
        }
    }

    @Test
    fun `setting a static field with a null receiver should change its value`() {
        val X by sources.add("X", "class X { static int field = 10; }")
        sources.compile()
        val field = Mirror.reflect(X._f("field"))
        field.set(null, 20)
        assertEquals(20, X._get("field"))
    }

    @Test
    fun `setting a static field with a non-null receiver should not throw`() {
        val X by sources.add("X", "class X { static int field = 10; }")
        sources.compile()
        val instance = X._new<Any>()
        val field = Mirror.reflect(X._f("field"))
        assertDoesNotThrow {
            field.set(instance, 20)
        }
    }

    @Test
    fun `getting a field with a nonnull receiver should return its value`() {
        val X by sources.add("X", "class X { int field = 10; }")
        sources.compile()
        val instance = X._new<Any>()
        val field = Mirror.reflect(X._f("field"))
        assertEquals(10, instance._get("field"))
    }

    @Test
    fun `getting a field with a null receiver should throw`() {
        val X by sources.add("X", "class X { int field = 10; }")
        sources.compile()
        val field = Mirror.reflect(X._f("field"))
        assertThrows<NullPointerException> {
            field.get(null)
        }
    }

    @Test
    fun `getting a field with the wrong receiver type should throw`() {
        val X by sources.add("X", "class X { int field = 10; }")
        sources.compile()
        val field = Mirror.reflect(X._f("field"))
        assertThrows<ClassCastException> {
            field.get("")
        }.assertMessage("Cannot cast java.lang.String to gen.X")
    }

    @Test
    fun `setting a field with a non-null receiver should change its value`() {
        val X by sources.add("X", "class X { int field = 10; }")
        sources.compile()
        val instance = X._new<Any>()
        val field = Mirror.reflect(X._f("field"))
        field.set(instance, 20)
        assertEquals(20, instance._get("field"))
    }

    @Test
    fun `setting a field with a null receiver should throw`() {
        val X by sources.add("X", "class X { int field = 10; }")
        sources.compile()
        val field = Mirror.reflect(X._f("field"))
        assertThrows<NullPointerException> {
            field.set(null, 20)
        }
    }

    @Test
    fun `getting a private field with should return its value`() {
        val X by sources.add("X", "class X { private static int field = 10; }")
        sources.compile()
        val field = Mirror.reflect(X._f("field"))
        assertEquals(10, field.get(null))
    }

    @Test
    fun `setting a private field should change its value`() {
        val X by sources.add("X", "class X { private static int field = 10; }")
        sources.compile()
        val field = Mirror.reflect(X._f("field"))
        field.set(null, 20)
        assertEquals(20, X._get("field"))
    }

    @Test
    fun `setting a final field should change its value`() {
        val X by sources.add("X", "class X { final static int field = 10; }")
        sources.compile()
        val field = Mirror.reflect(X._f("field"))
        field.set(null, 20)
        assertEquals(20, X._get("field"))
    }
}
