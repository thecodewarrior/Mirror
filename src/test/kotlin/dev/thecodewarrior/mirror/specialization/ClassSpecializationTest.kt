package dev.thecodewarrior.mirror.specialization

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.GenericObject1
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.Object2
import dev.thecodewarrior.mirror.testsupport.assertSameList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ClassSpecializationTest: MirrorTestBase() {
    @Test
    fun specialize_withGenericType_shouldReturnMirrorWithNewParametersAndCorrectRaw() {
        val genericType = Mirror.reflectClass(GenericObject1::class.java)
        val specializeWith = Mirror.reflectClass<Object1>()
        val specialized = genericType.withTypeArguments(specializeWith)

        assertNotEquals(genericType, specialized)
        assertSameList(listOf(specializeWith), specialized.typeParameters)
        assertEquals(genericType, specialized.raw)
    }

    @Test
    fun specialize_withZeroArgumentCount_shouldReturnRaw() {
        val genericType = Mirror.reflectClass<GenericObject1<Object1>>()
        assertEquals(Mirror.reflectClass(GenericObject1::class.java), genericType.withTypeArguments())
    }


    @Test
    fun specialize_withWrongArgumentCount_shouldThrow() {
        val genericType = Mirror.reflectClass(GenericObject1::class.java)
        val specializeWith = Mirror.reflectClass<Object1>()
        assertThrows<InvalidSpecializationException> {
            genericType.withTypeArguments(specializeWith, specializeWith)
        }
    }

    @Test
    fun specialize_withOwnTypeParameters_shouldReturnRawType() {
        val genericType = Mirror.reflectClass(GenericObject1::class.java)
        assertSame(genericType, genericType.withTypeArguments(genericType.typeParameters[0]))
    }

    @Test
    fun specialize_nonGenericWithNoArguments_shouldReturnRawType() {
        val nonGenericType = Mirror.reflectClass(Object1::class.java)
        assertSame(nonGenericType, nonGenericType.withTypeArguments())
    }

    @Test
    fun specialize_withAlreadySpecializedType_shouldHaveSameRaw() {
        val genericType = Mirror.reflectClass(GenericObject1::class.java)
        val specializeWith1 = Mirror.reflectClass<Object1>()
        val specializeWith2 = Mirror.reflectClass<Object2>()
        val specialized1 = genericType.withTypeArguments(specializeWith1)
        val specialized2 = specialized1.withTypeArguments(specializeWith2)
        assertSame(specialized1.raw, specialized2.raw)
    }
}