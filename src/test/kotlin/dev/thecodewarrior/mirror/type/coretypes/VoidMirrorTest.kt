package dev.thecodewarrior.mirror.type.coretypes

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.annotations.TypeAnnotation1
import dev.thecodewarrior.mirror.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class VoidMirrorTest: MirrorTestBase() {
    @Test
    fun coreType_ofNotAnnotated_shouldReturnVoidClass() {
        assertEquals(Void.TYPE, Mirror.types.void.coreType)
    }

    @Test
    fun coreType_ofAnnotated_shouldReturnVoidClass() {
        val annotatedVoid = CoreTypeUtils.annotate(Void.TYPE, arrayOf(Mirror.newAnnotation<TypeAnnotation1>()))
        assertEquals(Void.TYPE, Mirror.reflect(annotatedVoid).coreType)
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotated_shouldReturnVoidClassWithNoAnnotations() {
        assertEquals(
            CoreTypeUtils.annotate(Void.TYPE),
            Mirror.types.void.coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotated_shouldReturnVoidClassWithAnnotations() {
        val annotatedVoid = CoreTypeUtils.annotate(Void.TYPE, arrayOf(Mirror.newAnnotation<TypeAnnotation1>()))
        assertEquals(
            annotatedVoid,
            Mirror.reflect(annotatedVoid).coreAnnotatedType
        )
    }
}