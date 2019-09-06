package dev.thecodewarrior.mirror.type.coretypes

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.canonical
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.typeholders.TypeMirror_CoreTypesHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VariableMirrorTest: MirrorTestBase() {
    private val holder = TypeMirror_CoreTypesHolder()

    @Test
    fun coreType_ofNotAnnotated_shouldReturnTypeParameter() {
        assertEquals(
            holder["T"].type,
            Mirror.reflect(holder["T"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotated_shouldReturnTypeParameter() {
        assertEquals(
            holder["@TypeAnnotation1 T; T", 1].type,
            Mirror.reflect(holder["@TypeAnnotation1 T; T", 0]).coreType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotated_shouldReturnTypeParameter() {
        assertEquals(
            holder["T"].canonical,
            Mirror.reflect(holder["T"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotated_shouldReturnAnnotatedTypeParameter() {
        assertEquals(
            holder["@TypeAnnotation1 T; T", 0].canonical,
            Mirror.reflect(holder["@TypeAnnotation1 T; T", 0]).coreAnnotatedType
        )
    }

}