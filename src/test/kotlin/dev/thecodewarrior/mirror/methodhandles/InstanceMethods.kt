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
        val method = thisType.getMethod("incrementCounter")
        method<Unit>(this)
        assertEquals(1, counterValue)
    }

    @Test
    fun call_private() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.getMethod("privateIncrementCounter")
        method<Unit>(this)
        assertEquals(1, counterValue)
    }

    @Test
    fun call_withParam() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.getMethod("addCounter", Mirror.types.int)
        method<Unit>(this, 5)
        assertEquals(5, counterValue)
    }

    @Test
    fun call_withReturn() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.getMethod("getCounter")
        counterValue = 3
        assertEquals(3, method(this))
    }

    @Test
    fun call_void_passingNoReceiver() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.getMethod("incrementCounter")
        assertThrows<NullPointerException> {
            method<Unit>(null)
        }
    }

    @Test
    fun call_void_passingWrongReceiverType() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.getMethod("incrementCounter")
        assertThrows<IllegalArgumentException> {
            method<Unit>("whoops!")
        }
    }

    @Test
    fun call_void_passingParam() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.getMethod("incrementCounter")
        assertThrows<IllegalArgumentException> {
            method<Unit>(this, "whoops!")
        }
    }

    @Test
    fun call_withParam_withoutParam() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.getMethod("addCounter", Mirror.types.int)
        assertThrows<IllegalArgumentException> {
            method<Unit>(this)
        }
    }

    @Test
    fun call_withParam_withIncorrectParamType() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.getMethod("addCounter", Mirror.types.int)
        assertThrows<ClassCastException> {
            method<Unit>(this, "whoops!")
        }
    }

    @Test
    fun call_withParam_withExtraParam() {
        val thisType = Mirror.reflectClass<InstanceMethods>()
        val method = thisType.getMethod("addCounter", Mirror.types.int)
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
