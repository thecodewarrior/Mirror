package com.teamwizardry.mirror.specialization

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.GenericInterface1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class FieldSpecializationTest {
    @Test
    @DisplayName("Specializing a class should specialize the types of each of its declared fields")
    fun specializeFields() {
        class FieldHolder<T>(
            @JvmField
            val directGeneric: T,
            @JvmField
            val indirectGeneric: GenericInterface1<T>
        )

        val baseType = Mirror.reflectClass<FieldHolder<*>>()
        val specializedType = baseType.withTypeArguments(Mirror.reflect<String>())
        val specializedDirectField = specializedType.declaredFields.find { it.name == "directGeneric" }!!
        val specializedIndirectField = specializedType.declaredFields.find { it.name == "indirectGeneric" }!!

        assertEquals(Mirror.reflect<String>(), specializedDirectField.type)
        assertEquals(Mirror.reflect<GenericInterface1<String>>(), specializedIndirectField.type)
    }
}