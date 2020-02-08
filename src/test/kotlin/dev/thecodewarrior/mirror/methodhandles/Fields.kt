package dev.thecodewarrior.mirror.methodhandles

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class Fields: MirrorTestBase() {

    @Test
    fun get_static() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("staticCounterValue")!!
        assertEquals(1, field.get(null))
    }

    @Test
    fun set_static() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("staticCounterValue")!!
        field.set(null, 5)
        assertEquals(5, staticCounterValue)
    }

    @Test
    fun get_instance() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("instanceCounterValue")!!
        assertEquals(2, field.get(this))
    }

    @Test
    fun set_instance() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("instanceCounterValue")!!
        field.set(this, 5)
        assertEquals(5, instanceCounterValue)
    }

    @Test
    fun get_staticPrivate() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("staticPrivateField")!!
        assertEquals(1, field.get(null))
    }

    @Test
    fun set_staticPrivate() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("staticPrivateField")!!
        field.set(null, 5)
        assertEquals(5, staticPrivateField)
    }

    @Test
    fun get_instancePrivate() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("instancePrivateField")!!
        assertEquals(2, field.get(this))
    }

    @Test
    fun set_instancePrivate() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("instancePrivateField")!!
        field.set(this, 5)
        assertEquals(5, instancePrivateField)
    }

    @Test
    fun set_staticFinal() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("staticFinalValue")!!
        assertThrows<IllegalStateException> {
            field.set(null, 5)
        }
    }

    @Test
    fun set_instanceFinal() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("instanceFinalValue")!!
        assertThrows<IllegalStateException> {
            field.set(this, 5)
        }
    }

    @Test
    fun get_static_passingReceiver() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("staticCounterValue")!!
        assertThrows<IllegalArgumentException> {
            field.get(this)
        }
    }

    @Test
    fun set_static_passingReceiver() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("staticCounterValue")!!
        assertThrows<IllegalArgumentException> {
            field.set(this, 5)
        }
    }

    @Test
    fun get_instance_withoutReceiver() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("instanceCounterValue")!!
        assertThrows<NullPointerException> {
            field.get(null)
        }
    }

    @Test
    fun set_instance_withoutReceiver() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("instanceCounterValue")!!
        assertThrows<NullPointerException> {
            field.set(null, 5)
        }
    }

    @Test
    fun set_static_withWrongType() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("staticCounterValue")!!
        assertThrows<ClassCastException> {
            field.set(null, "whoops!")
        }
    }

    @Test
    fun set_instance_withWrongType() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("instanceCounterValue")!!
        assertThrows<ClassCastException> {
            field.set(this, "whoops!")
        }
    }

    @Test
    fun set_instance_withWrongReceiverType() {
        val thisType = Mirror.reflectClass<Fields>()
        val field = thisType.findDeclaredField("instanceCounterValue")!!
        assertThrows<IllegalArgumentException> {
            field.set("whoops!", 5)
        }
    }

    override fun initializeForTest() {
        super.initializeForTest()
        instanceCounterValue = 2
        staticCounterValue = 1
        instancePrivateField = 2
        staticPrivateField = 1
    }

    var instanceCounterValue = 2
    val instanceFinalValue = 42
    private var instancePrivateField = 2

    companion object {
        @JvmField var staticCounterValue = 1
        @JvmField val staticFinalValue = 42
        private var staticPrivateField = 1
    }
}
