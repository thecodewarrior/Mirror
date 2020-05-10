package dev.thecodewarrior.mirror

import dev.thecodewarrior.mirror.coretypes.AnnotationFormatException
import dev.thecodewarrior.mirror.testsupport.AnnotationWithParameter
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.reflect.AnnotatedArrayType

internal class MirrorTest: MTest() {

    @Test
    fun newAnnotation_isExternalLibrary() {
        // newAnnotation is from an external library, so it doesn't require many tests
    }

    @Test
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
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"X"
            +"X[]"
        }
        sources.compile()
        assertEquals(
            Mirror.reflect(types["X[]"]),
            Mirror.createArrayType(Mirror.reflect(types["X"]))
        )
    }

    @Test
    fun createArrayType_withArray_shouldReturn2dArray() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"X[]"
            +"X[][]"
        }
        sources.compile()
        assertEquals(
            Mirror.reflect(types["X[][]"]),
            Mirror.createArrayType(Mirror.reflect(types["X[]"]))
        )
    }

    @Test
    fun createArrayType_withGeneric_shouldReturnGeneric() {
        val sources = TestSources()
        val types = sources.types {
            typeVariables("T") {
                +"T"
                +"T[]"
            }
        }
        sources.compile()
        assertEquals(
            Mirror.reflect(types["T[]"]),
            Mirror.createArrayType(Mirror.reflect(types["T"]))
        )
    }

    @Test
    fun createArrayType_withSpecialized_shouldReturnSpecialized() {
        val sources = TestSources()
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"Generic<Y>"
            +"Generic<Y>[]"
        }
        sources.compile()
        assertEquals(
            Mirror.reflect(types["Generic<Y>[]"]),
            Mirror.createArrayType(Mirror.reflect(types["Generic<Y>"]))
        )
    }
}
