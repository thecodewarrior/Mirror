package dev.thecodewarrior.mirror.specialization

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.type.ArrayMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.reflect.AnnotatedArrayType

internal class ArraySpecializationTest: MirrorTestBase() {
    @Test
    @DisplayName("Specializing generic array should return an array mirror with the new type")
    fun basicSpecialization() {
        class GenericArrayHolder<T>(
            val array: Array<T>
        )
        val genericType = Mirror.reflectClass(GenericArrayHolder::class.java)
        val specializeWith = Mirror.reflectClass<Object1>()
        val specialized = genericType.withTypeArguments(specializeWith)
        val specializedArray = specialized.findPublicField("array")!!.type as ArrayMirror

        assertEquals(specializeWith, specializedArray.component)
    }

    @Test
    fun getRawClass_onGenericArray_shouldReturnObjectArray() {
        class FieldHolder<T>(
            @JvmField
            val field: Array<T>
        )
        val genericArray = FieldHolder::class.java.getField("field").annotatedType as AnnotatedArrayType
        val type = Mirror.reflect(genericArray) as ArrayMirror
        assertEquals(Mirror.reflect<Array<Any>>(), type.raw)
    }

    @Test
    @DisplayName("Specializing an array with an incompatible type should throw")
    fun specialize_withIncompatibleType_shouldThrow() {
        val array = Mirror.reflect<Array<List<*>>>() as ArrayMirror
        assertThrows<InvalidSpecializationException> {
            array.withComponent(Mirror.reflect<String>())
        }
    }
}