package com.teamwizardry.mirror.specialization

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.GenericObject1
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.Object1
import com.teamwizardry.mirror.testsupport.Object2
import com.teamwizardry.mirror.testsupport.assertSameList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
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
        assertSameList(listOf(specializeWith), specialized.typeParameters)
        assertEquals(genericType, specialized.raw)
    }

    @Test
    @DisplayName("Specializing type with wrong number of arguments should throw InvalidSpecializationException")
    fun specialize_withWrongArgumentCount_shouldThrow() {
        val genericType = Mirror.reflectClass(GenericObject1::class.java)
        val specializeWith = Mirror.reflectClass<Object1>()
        assertThrows<InvalidSpecializationException> {
            genericType.specialize(specializeWith, specializeWith)
        }
    }

    @Test
    @DisplayName("Specializing a type with its own type parameters should return the raw type")
    fun specialize_withOwnTypeParameters_shouldReturnRawType() {
        val genericType = Mirror.reflectClass(GenericObject1::class.java)
        assertSame(genericType, genericType.specialize(genericType.typeParameters[0]))
    }

    @Test
    @DisplayName("Specializing a non-generic type with no type arguments should return the raw type")
    fun specialize_nonGenericWithNoArguments_shouldReturnRawType() {
        val nonGenericType = Mirror.reflectClass(Object1::class.java)
        assertSame(nonGenericType, nonGenericType.specialize())
    }

    @Test
    @DisplayName("Re-specializing a type that has already been specialized shouldn't throw an exception")
    fun specialize_withAlreadySpecializedType_shouldHaveSameRaw() {
        val genericType = Mirror.reflectClass(GenericObject1::class.java)
        val specializeWith1 = Mirror.reflectClass<Object1>()
        val specializeWith2 = Mirror.reflectClass<Object2>()
        val specialized1 = genericType.specialize(specializeWith1)
        val specialized2 = specialized1.specialize(specializeWith2)
        assertSame(specialized1.raw, specialized2.raw)
    }
}