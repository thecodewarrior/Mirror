package dev.thecodewarrior.mirror.methodhandles

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class Constructors: MTest() {
    val Public by sources.add("Public", """
        class Public {
            public int value = 0;
            public Public() {
                this.value = 1;
            }
            public Public(int param) {
                this.value = param;
            }
        }
    """.trimIndent())
    val Private by sources.add("Private", """
        class Private {
            public int value = 0;
            private Private() {
                this.value = 1;
            }
            private Private(int param) {
                this.value = param;
            }
        }
    """.trimIndent())

    @Test
    fun call_zeroArg() {
        val testClass = Mirror.reflectClass(Public)
        val constructor = testClass.getDeclaredConstructor()
        val obj = constructor.call<Any>()
        assertEquals(1, obj._get("value"))
    }

    @Test
    fun call_private_zeroArg() {
        val testClass = Mirror.reflectClass(Private)
        val constructor = testClass.getDeclaredConstructor()
        val obj = constructor.call<Any>()
        assertEquals(1, obj._get("value"))
    }

    @Test
    fun call_withParam() {
        val testClass = Mirror.reflectClass(Public)
        val constructor = testClass.getDeclaredConstructor(Mirror.types.int)
        val obj = constructor.call<Any>(4)
        assertEquals(4, obj._get("value"))
    }

    @Test
    fun call_private_withParam() {
        val testClass = Mirror.reflectClass(Private)
        val constructor = testClass.getDeclaredConstructor(Mirror.types.int)
        val obj = constructor.call<Any>(4)
        assertEquals(4, obj._get("value"))
    }

    @Test
    fun call_zeroArg_passingParam() {
        val testClass = Mirror.reflectClass(Public)
        val constructor = testClass.getDeclaredConstructor()
        assertThrows<IllegalArgumentException> {
            constructor.call<Any>(4)
        }
    }

    @Test
    fun call_withParam_withoutParam() {
        val testClass = Mirror.reflectClass(Public)
        val constructor = testClass.getDeclaredConstructor(Mirror.types.int)
        assertThrows<IllegalArgumentException> {
            constructor.call<Any>()
        }
    }

    @Test
    fun call_withParam_withIncorrectParamType() {
        val testClass = Mirror.reflectClass(Public)
        val constructor = testClass.getDeclaredConstructor(Mirror.types.int)
        assertThrows<ClassCastException> {
            constructor.call<Any>("whoops!")
        }
    }

    @Test
    fun call_withParam_withExtraParam() {
        val testClass = Mirror.reflectClass(Public)
        val constructor = testClass.getDeclaredConstructor(Mirror.types.int)
        assertThrows<IllegalArgumentException> {
            constructor.call<Any>(4, "whoops!")
        }
    }
}
