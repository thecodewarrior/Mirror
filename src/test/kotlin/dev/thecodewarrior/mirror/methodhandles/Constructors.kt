package dev.thecodewarrior.mirror.methodhandles

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class Constructors: MirrorTestBase() {

    @Test
    fun call_zeroArg() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.getDeclaredConstructor()
        val obj = constructor.call<TestClass>()
        assertEquals(1, obj.v)
    }

    @Test
    fun call_private() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.getDeclaredConstructor(Mirror.reflect<String>())
        val obj = constructor.call<TestClass>("hello")
        assertEquals("hello".length, obj.v)
    }

    @Test
    fun call_withParam() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.getDeclaredConstructor(Mirror.types.int)
        val obj = constructor.call<TestClass>(4)
        assertEquals(4, obj.v)
    }

    @Test
    fun call_zeroArg_passingParam() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.getDeclaredConstructor()
        assertThrows<IllegalArgumentException> {
            constructor.call<TestClass>(2)
        }
    }

    @Test
    fun call_withParam_withoutParam() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.getDeclaredConstructor(Mirror.types.int)
        assertThrows<IllegalArgumentException> {
            constructor.call<TestClass>()
        }
    }

    @Test
    fun call_withParam_withIncorrectParamType() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.getDeclaredConstructor(Mirror.types.int)
        assertThrows<ClassCastException> {
            constructor.call<TestClass>("whoops!")
        }
    }

    @Test
    fun call_withParam_withExtraParam() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.getDeclaredConstructor(Mirror.types.int)
        assertThrows<IllegalArgumentException> {
            constructor.call<TestClass>(4, "whoops!")
        }
    }

    class TestClass {
        val v: Int

        constructor() {
            v = 1
        }
        constructor(initial: Int) {
            v = initial
        }
        private constructor(lengthOf: String) {
            v = lengthOf.length
        }
    }
}
