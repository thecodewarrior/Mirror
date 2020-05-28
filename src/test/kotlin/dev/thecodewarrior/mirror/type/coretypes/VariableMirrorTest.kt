package dev.thecodewarrior.mirror.type.coretypes

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class VariableMirrorTest: MTest() {
    val A by sources.add("A", """ 
        @Target(ElementType.TYPE_USE)
        @interface A {}
    """)

    val types = sources.types {
        typeVariables("T") {
            +"T"
            +"@A T"
        }
    }

    @Test
    fun coreType_ofNotAnnotated_shouldReturnTypeParameter() {
        assertEquals(
            types["T"].type,
            Mirror.reflect(types["T"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotated_shouldReturnTypeParameter() {
        assertEquals(
            types["T"].type,
            Mirror.reflect(types["@A T"]).coreType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotated_shouldReturnTypeParameter() {
        assertEquals(
            Mirror.toCanonical(types["T"]),
            Mirror.reflect(types["T"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotated_shouldReturnAnnotatedTypeParameter() {
        assertEquals(
            Mirror.toCanonical(types["T"]),
            Mirror.reflect(types["T"]).coreAnnotatedType
        )
    }

}