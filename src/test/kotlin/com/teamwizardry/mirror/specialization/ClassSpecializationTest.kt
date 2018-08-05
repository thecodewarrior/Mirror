package com.teamwizardry.mirror.specialization

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.GenericObject1
import com.teamwizardry.mirror.testsupport.Object1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ClassSpecializationTest: MirrorTestBase() {
    @Test
    @DisplayName("Specializing generic type should return mirror with new type parameters and correct value for raw")
    fun basicSpecialization() {
        val genericType = Mirror.reflectClass(GenericObject1::class.java)
        val specializeWith = Mirror.reflectClass<Object1>()
        val specialized = genericType.specialize(specializeWith)

        assertNotEquals(genericType, specialized)
        assertEquals(listOf(specializeWith), specialized.typeParameters)
        assertEquals(genericType, specialized.raw)
    }

    @Test
    @DisplayName("Specializing type with wrong number of arguments should throw IllegalArgumentException")
    fun specializingWrongParameterCount() {
        val genericType = Mirror.reflectClass(GenericObject1::class.java)
        val specializeWith = Mirror.reflectClass<Object1>()
        assertThrows<IllegalArgumentException> {
            genericType.specialize(specializeWith, specializeWith)
        }
    }
}