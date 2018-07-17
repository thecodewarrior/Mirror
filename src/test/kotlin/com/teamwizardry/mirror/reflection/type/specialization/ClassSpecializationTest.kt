package com.teamwizardry.mirror.reflection.type.specialization

import com.teamwizardry.mirror.reflection.Mirror
import com.teamwizardry.mirror.reflection.MirrorTestBase
import com.teamwizardry.mirror.reflection.type.Object1
import com.teamwizardry.mirror.reflection.type.TypeMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ClassSpecializationTest: MirrorTestBase() {
    @Test
    fun classSpecialize_onSimpleGeneric_shouldReturnTypeWithNewTypeParametersAndRaw() {
        class SimpleGeneric<T>
        val genericType = Mirror.reflectClass(SimpleGeneric::class.java)
        val specializeWith = Mirror.reflectClass<Object1>()
        val specialized = genericType.specialize(specializeWith)

        assertNotEquals(genericType, specialized)
        assertEquals(listOf(specializeWith), specialized.typeParameters)
        assertEquals(genericType, specialized.raw)
    }

    @Test
    fun classSpecialize_withIncorrectParameterCount_shouldThrowIllegalArgumentException() {
        class SimpleGeneric<T>
        val genericType = Mirror.reflectClass(SimpleGeneric::class.java)
        val specializeWith = Mirror.reflectClass<Object1>()
        assertThrows<IllegalArgumentException> {
            genericType.specialize(specializeWith, specializeWith)
        }
    }

    @Test
    fun reflect_withSpecializedSupertype_shouldReturnTypeWithSpecializedSupertype() {
        open class SimpleGeneric<T>
        class SimpleSubtype: SimpleGeneric<String>()
        val simpleType = Mirror.reflectClass(SimpleSubtype::class.java)
        val supertype = simpleType.superclass!!

        val generic = Mirror.reflectClass(SimpleGeneric::class.java)
        assertEquals(generic.specialize(Mirror.reflect<String>()), supertype)
    }

    @Test
    fun reflect_withTypePassingVariableUpToSupertype_shouldReturnTypeWithSupertypeSpecializedWithVariable() {
        open class SimpleGeneric<T>
        class SimpleSubtype<A>: SimpleGeneric<A>()
        val simpleType = Mirror.reflectClass(SimpleSubtype::class.java)
        val supertype = simpleType.superclass!!
        val simpleTypeVariable = simpleType.typeParameters[0]

        assertEquals(listOf(simpleTypeVariable), supertype.typeParameters)
    }

    @Test
    fun classSpecialize_withTypePassingVariableUpToSupertype_shouldReturnTypeWithSupertypeSpecializedToNewParameter() {
        open class SimpleGeneric<T>
        class SimpleSubtype<A>: SimpleGeneric<A>()
        val specialized = Mirror.reflectClass(SimpleSubtype::class.java).specialize(Mirror.reflect<String>())
        val supertype = specialized.superclass!!

        assertEquals(listOf(Mirror.reflect<String>()), supertype.typeParameters)
    }
}