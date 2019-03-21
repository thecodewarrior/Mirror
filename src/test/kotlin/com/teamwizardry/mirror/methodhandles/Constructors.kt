package com.teamwizardry.mirror.methodhandles

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class Constructors: MirrorTestBase() {

    @Test
    fun call_zeroArg() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.declaredConstructor()!!
        val obj = constructor<TestClass>()
        assertEquals(1, obj.v)
    }

    @Test
    fun call_private() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.declaredConstructor(Mirror.reflect<String>())!!
        val obj = constructor<TestClass>("hello")
        assertEquals("hello".length, obj.v)
    }

    @Test
    fun call_withParam() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.declaredConstructor(Mirror.reflectPrimitive<Int>())!!
        val obj = constructor<TestClass>(4)
        assertEquals(4, obj.v)
    }

    @Test
    fun call_zeroArg_passingParam() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.declaredConstructor()!!
        assertThrows<IllegalArgumentException> {
            constructor<TestClass>(2)
        }
    }

    @Test
    fun call_withParam_withoutParam() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.declaredConstructor(Mirror.reflectPrimitive<Int>())!!
        assertThrows<IllegalArgumentException> {
            constructor<TestClass>()
        }
    }

    @Test
    fun call_withParam_withIncorrectParamType() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.declaredConstructor(Mirror.reflectPrimitive<Int>())!!
        assertThrows<ClassCastException> {
            constructor<TestClass>("whoops!")
        }
    }

    @Test
    fun call_withParam_withExtraParam() {
        val testClass = Mirror.reflectClass<TestClass>()
        val constructor = testClass.declaredConstructor(Mirror.reflectPrimitive<Int>())!!
        assertThrows<IllegalArgumentException> {
            constructor<TestClass>(4, "whoops!")
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
