package dev.thecodewarrior.mirror.methodhandles

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class FastCall: MTest() {
    @Test
    fun `fast calling a void method should correctly invoke it`() {
        val X by sources.add("X", "class X { int field = 10; void method() { this.field = 20; } }")
        sources.compile()
        val instance = X._new<Any>()
        Mirror.reflect(X._m("method")).callFast<Unit>(instance)
        assertEquals(20, instance._get<Any>("field"))
    }

    @Test
    fun `fast calling a private method should not throw`() {
        val X by sources.add("X", "class X { private void method() { } }")
        sources.compile()
        val instance = X._new<Any>()
        assertDoesNotThrow {
            Mirror.reflect(X._m("method")).callFast<Unit>(instance)
        }
    }

    @Test
    fun `fast calling a method with arguments should correctly pass them`() {
        val X by sources.add("X", "class X { int field = 10; void method(int param) { this.field = param; } }")
        sources.compile()
        val instance = X._new<Any>()
        Mirror.reflect(X._m("method")).callFast<Unit>(instance, 20)
        assertEquals(20, instance._get<Any>("field"))
    }

    @Test
    fun `fast calling a method with a return value should correctly return it`() {
        val X by sources.add("X", "class X { int method() { return 20; } }")
        sources.compile()
        val instance = X._new<Any>()
        assertEquals(20, Mirror.reflect(X._m("method")).call(instance))
    }

    @Test
    fun `fast calling a method with a null receiver should throw`() {
        val X by sources.add("X", "class X { void method() { } }")
        sources.compile()
        assertThrows<NullPointerException> {
            Mirror.reflect(X._m("method")).callFast<Unit>(null)
        }
    }

    @Test
    fun `fast calling a method with the wrong receiver type should throw`() {
        val X by sources.add("X", "class X { void method() { } }")
        sources.compile()
        assertThrows<ClassCastException> {
            Mirror.reflect(X._m("method")).callFast<Unit>("")
        }
    }

    @Test
    fun `fast calling a method with arguments and no parameters should not throw`() {
        val X by sources.add("X", "class X { void method() { } }")
        sources.compile()
        val instance = X._new<Any>()
        assertDoesNotThrow {
            Mirror.reflect(X._m("method")).callFast<Unit>(instance, 0)
        }
    }

    @Test
    fun `fast calling a method with too many arguments should throw`() {
        val X by sources.add("X", "class X { void method(int a) { } }")
        sources.compile()
        val instance = X._new<Any>()
        assertThrows<IllegalArgumentException> {
            Mirror.reflect(X._m("method")).callFast<Unit>(instance, 0, 1)
        }
    }

    @Test
    fun `fast calling a method with too few arguments should throw`() {
        val X by sources.add("X", "class X { void method(int a, int b) { } }")
        sources.compile()
        val instance = X._new<Any>()
        assertThrows<IllegalArgumentException> {
            Mirror.reflect(X._m("method")).callFast<Unit>(instance, 0)
        }
    }

    @Test
    fun `fast calling a method with wrong parameter types should throw`() {
        val X by sources.add("X", "class X { void method(int a, float b) { } }")
        sources.compile()
        val instance = X._new<Any>()
        assertThrows<ClassCastException> {
            Mirror.reflect(X._m("method")).callFast<Unit>(instance, 0, "")
        }
    }
}
