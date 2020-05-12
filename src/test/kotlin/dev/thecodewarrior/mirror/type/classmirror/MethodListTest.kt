package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MethodListTest: MTest() {
    @Test
    fun `calling find(String) with one matching method should return a list with a single mirror`() {
        val X by sources.add("X", "class X { void method1() {} void method2() {}}")
        sources.compile()
        assertEquals(
            listOf(Mirror.reflect(X._m("method1"))),
            Mirror.reflectClass(X).declaredMethods.find("method1")
        )
    }

    @Test
    fun `calling find(String) with multiple matching methods should return a list with all the matching mirrors`() {
        val X by sources.add("X", "class X { void method1() {} void method1(int foo) {} void method2() {}}")
        sources.compile()
        assertEquals(
            listOf(Mirror.reflect(X._m("method1")), Mirror.reflect(X._m("method1", Mirror.types.int.java))),
            Mirror.reflectClass(X).declaredMethods.find("method1")
        )
    }
}