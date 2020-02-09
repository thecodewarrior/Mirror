package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.typeholders.member.MethodMirrorHolder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MethodMirrorTest : MirrorTestBase() {
    val holder = MethodMirrorHolder()

    @Test
    fun isBridge_withNonBridgeMethod_shouldReturnFalse() {
        val bridge = Mirror.reflect(holder.c("GenericInheritor").m("nonBridge", String::class.java))
        assertFalse(bridge.isBridge)
    }

    @Test
    fun isBridge_withBridgeMethod_shouldReturnTrue() {
        val bridge = Mirror.reflect(holder.c("GenericInheritor").m("genericMethod", Any::class.java))
        assertTrue(bridge.isBridge)
    }

    @Test
    fun isDefault_withDefaultMethod_shouldReturnTrue() {
        val defaultMethod = Mirror.reflect(holder.m("default int ()"))
        assertTrue(defaultMethod.isDefault)
    }

    @Test
    fun isDefault_withNonDefaultMethod_shouldReturnFalse() {
        val nonDefaultMethod = Mirror.reflect(holder.m("non-default int ()"))
        assertFalse(nonDefaultMethod.isDefault)
    }

    @Test
    fun isDefault_withClassMethod_shouldReturnFalse() {
        val classMethod = Mirror.reflect(holder.m("void ()"))
        assertFalse(classMethod.isDefault)
    }

    @Test
    fun isDefault_withImplementedDefaultMethod_shouldReturnFalse() {
        val classMethod = Mirror.reflect(holder.m("overridden default int ()"))
        assertFalse(classMethod.isDefault)
    }

    @Test
    fun defaultValue_withStringDefaultValue_shouldReturnDefaultValue() {
        val defaulted = Mirror.reflect(holder.c("@DefaultedAnnotation").m("stringDefaulted"))
        Assertions.assertEquals("default value", defaulted.defaultValue)
    }

    @Test
    fun defaultValue_withIntDefaultValue_shouldReturnDefaultValue() {
        val defaulted = Mirror.reflect(holder.c("@DefaultedAnnotation").m("intDefaulted"))
        Assertions.assertEquals(10, defaulted.defaultValue)
    }

}