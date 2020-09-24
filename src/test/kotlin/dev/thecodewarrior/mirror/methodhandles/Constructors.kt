package dev.thecodewarrior.mirror.methodhandles

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

internal class Constructors: MTest() {
    @Test
    fun `calling a constructor should correctly return an instance`() {
        val X by sources.add("X", "class X { int field; X() { field = 20; } }")
        sources.compile()
        val instance = Mirror.reflect(X._constructor()).call<Any>()
        assertEquals(X, instance.javaClass)
        assertEquals(20, instance._get<Any>("field"))
    }

    @Test
    fun `calling a private constructor should not throw`() {
        val X by sources.add("X", "class X { int field; private X() { field = 20; } }")
        sources.compile()
        assertDoesNotThrow {
            Mirror.reflect(X._constructor()).call<Any>()
        }
    }

    @Test
    fun `calling a constructor with arguments should pass them`() {
        val X by sources.add("X", "class X { int field; X(int param) { field = param; } }")
        sources.compile()
        val instance = Mirror.reflect(X._constructor()).call<Any>(20)
        assertEquals(X, instance.javaClass)
        assertEquals(20, instance._get<Any>("field"))
    }

    @Test
    fun `calling a constructor with arguments and no parameters should throw`() {
        val X by sources.add("X", "class X { X() { } }")
        sources.compile()
        assertThrows<IllegalArgumentException> {
            Mirror.reflect(X._constructor()).call<Any>(0)
        }
    }

    @Test
    fun `calling a constructor with too many arguments should throw`() {
        val X by sources.add("X", "class X { X(int a) { } }")
        sources.compile()
        assertThrows<IllegalArgumentException> {
            Mirror.reflect(X._constructor()).call<Any>(0, 1)
        }
    }

    @Test
    fun `calling a constructor with too few arguments should throw`() {
        val X by sources.add("X", "class X { X(int a, int b) { } }")
        sources.compile()
        assertThrows<IllegalArgumentException> {
            Mirror.reflect(X._constructor()).call<Any>(0)
        }
    }

    @Test
    fun `fast calling a constructor should correctly return an instance`() {
        val X by sources.add("X", "class X { int field; X() { field = 20; } }")
        sources.compile()
        val instance = Mirror.reflect(X._constructor()).callFast<Any>()
        assertEquals(X, instance.javaClass)
        assertEquals(20, instance._get<Any>("field"))
    }

    @Test
    fun `fast calling a private constructor should not throw`() {
        val X by sources.add("X", "class X { int field; private X() { field = 20; } }")
        sources.compile()
        assertDoesNotThrow {
            Mirror.reflect(X._constructor()).callFast<Any>()
        }
    }

    @Test
    fun `fast calling a constructor with arguments should pass them`() {
        val X by sources.add("X", "class X { int field; X(int param) { field = param; } }")
        sources.compile()
        val instance = Mirror.reflect(X._constructor()).callFast<Any>(20)
        assertEquals(X, instance.javaClass)
        assertEquals(20, instance._get<Any>("field"))
    }

    @Test
    fun `fast calling a constructor with arguments and no parameters should not throw`() {
        val X by sources.add("X", "class X { X() { } }")
        sources.compile()
        // for some reason passing arguments to a zero-arg method is fine, but not constructors?
        assertThrows<IllegalArgumentException> {
            Mirror.reflect(X._constructor()).callFast<Any>(0)
        }
    }

    @Test
    fun `fast calling a constructor with too many arguments should throw`() {
        val X by sources.add("X", "class X { X(int a) { } }")
        sources.compile()
        assertThrows<IllegalArgumentException> {
            Mirror.reflect(X._constructor()).callFast<Any>(0, 1)
        }
    }

    @Test
    fun `fast calling a constructor with too few arguments should throw`() {
        val X by sources.add("X", "class X { X(int a, int b) { } }")
        sources.compile()
        assertThrows<IllegalArgumentException> {
            Mirror.reflect(X._constructor()).callFast<Any>(0)
        }
    }
}
