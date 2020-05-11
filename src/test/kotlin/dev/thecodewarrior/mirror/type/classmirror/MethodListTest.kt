package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MethodListTest: MTest() {
    @Test
    fun `calling get(Method) with a method present in the list should return the mirror`() {
        val X by sources.add("X", "class X<T> { void method1() {} void method2() {}}")
        sources.compile()
        assertEquals(
            Mirror.reflect(X._m("method1")),
            Mirror.reflectClass(X).declaredMethods.get(X._m("method1"))
        )
    }

    @Test
    fun `calling get(Method) with a method not present in the list should throw`() {
        val X by sources.add("X", "class X<T> { void method1() {} void method2() {}}")
        sources.compile()
        assertEquals(
            Mirror.reflect(X._m("method1")),
            Mirror.reflectClass(X).declaredMethods.get(X._m("method1"))
        )
    }

    @Test
    fun `calling get(Method) on a generic class should return the unspecialized mirror`() {
        val Generic by sources.add("Generic", "class Generic<T> { void method1() {} void method2() {}}")
        sources.compile()
        assertEquals(
            Mirror.reflect(Generic._m("method1")),
            Mirror.reflectClass(Generic).declaredMethods.get(Generic._m("method1"))
        )
    }

    @Test
    fun `calling get(Method) on specialized class should return the specialized mirror`() {
        val X by sources.add("X", "class X {}")
        val Generic by sources.add("Generic", "class Generic<T> { void method1(T param) {} void method2() {}}")
        val types = sources.types {
            +"Generic<X>"
        }
        sources.compile()
        assertEquals(
            Mirror.reflectClass(X),
            Mirror.reflectClass(types["Generic<X>"]).declaredMethods.get(Generic._m("method1")).parameterTypes[0]
        )
    }

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