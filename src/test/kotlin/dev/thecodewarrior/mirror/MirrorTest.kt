package dev.thecodewarrior.mirror

import dev.thecodewarrior.mirror.coretypes.AnnotationFormatException
import dev.thecodewarrior.mirror.testsupport.AnnotationWithParameter
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.Mirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.reflect.AnnotatedArrayType

internal class MirrorTest: MirrorTestBase() {

    @Test
    @DisplayName("newAnnotation delegates to an external tested library")
    fun newAnnotation_needsNoTests() {
    }

    @Test
    @DisplayName("newAnnotation with incorrect data should throw")
    fun newAnnotation_withIncorrectData_shouldThrow() {
        assertThrows<AnnotationFormatException> {
            Mirror.newAnnotation<AnnotationWithParameter>("foo" to "invalid")
        }
        assertThrows<AnnotationFormatException> {
            Mirror.newAnnotation<AnnotationWithParameter>("bar" to 0)
        }
    }

    @Test
    fun createArrayType_withClass_shouldReturnTypedArray() {
        assertEquals(
            Mirror.reflect<Array<Object1>>(),
            Mirror.createArrayType(Mirror.reflect<Object1>())
        )
    }

    @Test
    fun createArrayType_withArray_shouldReturn2dArray() {
        assertEquals(
            Mirror.reflect<Array<Array<Any>>>(),
            Mirror.createArrayType(Mirror.reflect<Array<Any>>())
        )
    }

    @Test
    fun createArrayType_withGeneric_shouldReturnGeneric() {
        class FieldHolder<T>(
            @JvmField
            val field: Array<T>
        )
        val genericArray = FieldHolder::class.java.getField("field").annotatedType as AnnotatedArrayType
        val typeVariable = FieldHolder::class.java.typeParameters[0]
        assertEquals(
            Mirror.reflect(genericArray),
            Mirror.createArrayType(Mirror.reflect(typeVariable))
        )
    }

    @Test
    fun createArrayType_withSpecialized_shouldReturnSpecialized() {
        assertEquals(
            Mirror.reflect<Array<List<String>>>(),
            Mirror.createArrayType(Mirror.reflect<List<String>>())
        )
    }
}
