package dev.thecodewarrior.mirror.methodhandles

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

internal class StaticMethods: MTest() {
    @Test
    fun `calling a static void method should correctly invoke it`() {
        val X by sources.add("X", "class X { static int field = 10; static void method() { field = 20; } }")
        sources.compile()
        Mirror.reflect(X._m("method")).call<Unit>(null)
        assertEquals(20, X._get<Any>("field"))
    }

    @Test
    fun `calling a static private method should not throw`() {
        val X by sources.add("X", "class X { private static void method() { } }")
        sources.compile()
        assertDoesNotThrow {
            Mirror.reflect(X._m("method")).call<Unit>(null)
        }
    }

    @Test
    fun `calling a static method with arguments should correctly pass them`() {
        val X by sources.add("X", "class X { static int field = 10; static void method(int param) { field = param; } }")
        sources.compile()
        Mirror.reflect(X._m("method")).call<Unit>(null, 20)
        assertEquals(20, X._get<Any>("field"))
    }

    @Test
    fun `calling a static method with a return value should correctly return it`() {
        val X by sources.add("X", "class X { static int method() { return 20; } }")
        sources.compile()
        assertEquals(20, Mirror.reflect(X._m("method")).call(null))
    }

    @Test
    fun `calling a static method with a receiver should not throw`() {
        val X by sources.add("X", "class X { static void method() { } }")
        sources.compile()
        val instance = X._new<Any>()
        assertDoesNotThrow { // Core Reflection just ignores the instance
            Mirror.reflect(X._m("method")).call<Unit>(instance)
        }
    }

    @Test
    fun `calling a static method with the wrong receiver type should throw`() {
        val X by sources.add("X", "class X { static void method() { } }")
        sources.compile()
        assertDoesNotThrow { // Core Reflection just ignores the instance
            Mirror.reflect(X._m("method")).call<Unit>("")
        }
    }

    @Test
    fun `calling a static method with arguments and no parameters should throw`() {
        val X by sources.add("X", "class X { static void method() { } }")
        sources.compile()
        assertThrows<IllegalArgumentException> {
            Mirror.reflect(X._m("method")).call<Unit>(null, 0)
        }
    }

    @Test
    fun `calling a static method with too many arguments should throw`() {
        val X by sources.add("X", "class X { static void method(int a) { } }")
        sources.compile()
        assertThrows<IllegalArgumentException> {
            Mirror.reflect(X._m("method")).call<Unit>(null, 0, 1)
        }
    }

    @Test
    fun `calling a static method with too few arguments should throw`() {
        val X by sources.add("X", "class X { static void method(int a, int b) { } }")
        sources.compile()
        assertThrows<IllegalArgumentException> {
            Mirror.reflect(X._m("method")).call<Unit>(null, 0)
        }
    }

    @Test
    fun `calling a static method with wrong parameter types should throw`() {
        val X by sources.add("X", "class X { static void method(int a, float b) { } }")
        sources.compile()
        assertThrows<ClassCastException> {
            Mirror.reflect(X._m("method")).call<Unit>(null, 0, "")
        }
    }
}
