package dev.thecodewarrior.mirror.methodhandles

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class InstanceMethods: MirrorTestBase() {

    @Test
    fun call_void() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.findDeclaredMethods("incrementCounter")[0]
        method<Unit>(this)
        assertEquals(1, counterValue)
    }

    @Test
    fun call_private() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.findDeclaredMethods("privateIncrementCounter")[0]
        method<Unit>(this)
        assertEquals(1, counterValue)
    }

    @Test
    fun call_withParam() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.findDeclaredMethods("addCounter")[0]
        method<Unit>(this, 5)
        assertEquals(5, counterValue)
    }

    @Test
    fun call_withReturn() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.findDeclaredMethods("getCounter")[0]
        counterValue = 3
        assertEquals(3, method(this))
    }

    @Test
    fun call_void_passingNoReceiver() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.findDeclaredMethods("incrementCounter")[0]
        assertThrows<NullPointerException> {
            method<Unit>(null)
        }
    }

    @Test
    fun call_void_passingWrongReceiverType() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.findDeclaredMethods("incrementCounter")[0]
        assertThrows<IllegalArgumentException> {
            method<Unit>("whoops!")
        }
    }

    @Test
    fun call_void_passingParam() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.findDeclaredMethods("incrementCounter")[0]
        assertThrows<IllegalArgumentException> {
            method<Unit>(this, "whoops!")
        }
    }

    @Test
    fun call_withParam_withoutParam() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.findDeclaredMethods("addCounter")[0]
        assertThrows<IllegalArgumentException> {
            method<Unit>(this)
        }
    }

    @Test
    fun call_withParam_withIncorrectParamType() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.findDeclaredMethods("addCounter")[0]
        assertThrows<ClassCastException> {
            method<Unit>(this, "whoops!")
        }
    }

    @Test
    fun call_withParam_withExtraParam() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.findDeclaredMethods("addCounter")[0]
        assertThrows<IllegalArgumentException> {
            method<Unit>(this, 5, "whoops!")
        }
    }

    override fun initializeForTest() {
        super.initializeForTest()
        counterValue = 0
    }

    var counterValue = 0

    fun incrementCounter() {
        counterValue++
    }

    private fun privateIncrementCounter() {
        counterValue++
    }

    fun addCounter(value: Int) {
        counterValue += value
    }

    fun getCounter(): Int {
        return counterValue
    }
}
