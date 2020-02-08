package dev.thecodewarrior.mirror.methodhandles

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class StaticMethods: MirrorTestBase() {

    @Test
    fun call_staticVoid() {
        val thisType = Mirror.reflectClass<StaticMethods>()
        val method = thisType.findDeclaredMethods("incrementCounter")[0]
        method<Unit>(null)
        assertEquals(1, counterValue)
    }

    @Test
    fun call_staticPrivate() {
        val thisType = Mirror.reflectClass<StaticMethods>()
        val method = thisType.findDeclaredMethods("privateIncrementCounter")[0]
        method<Unit>(null)
        assertEquals(1, counterValue)
    }

    @Test
    fun call_staticWithParam() {
        val thisType = Mirror.reflectClass<StaticMethods>()
        val method = thisType.findDeclaredMethods("addCounter")[0]
        method<Unit>(null, 5)
        assertEquals(5, counterValue)
    }

    @Test
    fun call_staticWithReturn() {
        val thisType = Mirror.reflectClass<StaticMethods>()
        val method = thisType.findDeclaredMethods("getCounter")[0]
        counterValue = 3
        assertEquals(3, method(null))
    }

    @Test
    fun call_staticVoid_passingReceiver() {
        val thisType = Mirror.reflectClass<StaticMethods>()
        val method = thisType.findDeclaredMethods("incrementCounter")[0]
        assertThrows<IllegalArgumentException> {
            method<Unit>("whoops!")
        }
    }

    @Test
    fun call_staticVoid_passingParam() {
        val thisType = Mirror.reflectClass<StaticMethods>()
        val method = thisType.findDeclaredMethods("incrementCounter")[0]
        assertThrows<IllegalArgumentException> {
            method<Unit>(null, "whoops!")
        }
    }

    @Test
    fun call_staticWithParam_withoutParam() {
        val thisType = Mirror.reflectClass<StaticMethods>()
        val method = thisType.findDeclaredMethods("addCounter")[0]
        assertThrows<IllegalArgumentException> {
            method<Unit>(null)
        }
    }

    @Test
    fun call_staticWithParam_withIncorrectParamType() {
        val thisType = Mirror.reflectClass<StaticMethods>()
        val method = thisType.findDeclaredMethods("addCounter")[0]
        assertThrows<ClassCastException> {
            method<Unit>(null, "whoops!")
        }
    }

    @Test
    fun call_staticWithParam_withExtraParam() {
        val thisType = Mirror.reflectClass<StaticMethods>()
        val method = thisType.findDeclaredMethods("addCounter")[0]
        assertThrows<IllegalArgumentException> {
            method<Unit>(null, 5, "whoops!")
        }
    }

    override fun initializeForTest() {
        super.initializeForTest()
        counterValue = 0
    }

    companion object {
        var counterValue = 0

        @JvmStatic
        fun incrementCounter() {
            counterValue++
        }

        @JvmStatic
        private fun privateIncrementCounter() {
            counterValue++
        }

        @JvmStatic
        fun addCounter(value: Int) {
            counterValue += value
        }

        @JvmStatic
        fun getCounter(): Int {
            return counterValue
        }
    }
}
