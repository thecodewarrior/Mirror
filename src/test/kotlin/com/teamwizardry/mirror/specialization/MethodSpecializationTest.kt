package com.teamwizardry.mirror.specialization

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.GenericInterface1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MethodSpecializationTest {
    @Test
    @DisplayName("Specializing a class should specialize the return types of each of its declared methods")
    fun specializeFields() {
        class MethodHolder<T> {
            fun directGeneric(): T { TODO() }
            fun indirectGeneric(): GenericInterface1<T> { TODO() }
        }

        val baseType = Mirror.reflectClass<MethodHolder<*>>()
        val specializedType = baseType.specialize(Mirror.reflect<String>())
        val specializedDirectField = specializedType.declaredMethods.find { it.name == "directGeneric" }!!
        val specializedIndirectField = specializedType.declaredMethods.find { it.name == "indirectGeneric" }!!

        assertEquals(Mirror.reflect<String>(), specializedDirectField.returnType)
        assertEquals(Mirror.reflect<GenericInterface1<String>>(), specializedIndirectField.returnType)
    }
}