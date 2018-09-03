package com.teamwizardry.mirror.specialization

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.GenericCheckedExceptionMethodHolder
import com.teamwizardry.mirror.testsupport.GenericInterface1
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class MethodSpecializationTest: MirrorTestBase() {
    @Test
    @DisplayName("Specializing a class should specialize the return types of each of its declared methods")
    fun specializeMethodReturns() {
        class MethodHolder<T> {
            fun directGeneric(): T { TODO() }
            fun indirectGeneric(): GenericInterface1<T> { TODO() }
        }

        val baseType = Mirror.reflectClass<MethodHolder<*>>()
        val specializedType = baseType.specialize(Mirror.reflect<String>())
        val specializedDirectMethod = specializedType.declaredMethods.find { it.name == "directGeneric" }!!
        val specializedIndirectMethod = specializedType.declaredMethods.find { it.name == "indirectGeneric" }!!

        assertEquals(Mirror.reflect<String>(), specializedDirectMethod.returnType)
        assertEquals(Mirror.reflect<GenericInterface1<String>>(), specializedIndirectMethod.returnType)
    }

    @Test
    @DisplayName("Specializing a class should specialize the return types of each of its declared methods")
    fun specializeMethodParams() {
        class MethodHolder<T> {
            fun directGeneric(param1: T) { TODO() }
            fun indirectGeneric(param1: GenericInterface1<T>) { TODO() }
        }

        val baseType = Mirror.reflectClass<MethodHolder<*>>()
        val specializedType = baseType.specialize(Mirror.reflect<String>())
        val specializedDirectMethod = specializedType.declaredMethods.find { it.name == "directGeneric" }!!
        val specializedIndirectMethod = specializedType.declaredMethods.find { it.name == "indirectGeneric" }!!

        assertEquals(Mirror.reflect<String>(), specializedDirectMethod.parameterTypes[0])
        assertEquals(Mirror.reflect<GenericInterface1<String>>(), specializedIndirectMethod.parameterTypes[0])
    }

    @Test
    @DisplayName("Specializing a class should specialize the checked exception types of each of its declared methods")
    fun specializeMethodExceptions() {
        val baseType = Mirror.reflectClass<GenericCheckedExceptionMethodHolder<*>>()
        val specializedType = baseType.specialize(Mirror.reflect<String>())
        val specializedMethod = specializedType.declaredMethods.find { it.name == "generic" }!!

        assertEquals(Mirror.reflect<String>(), specializedMethod.exceptionTypes[0])
    }
}