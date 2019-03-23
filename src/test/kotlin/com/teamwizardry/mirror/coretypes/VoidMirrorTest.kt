package com.teamwizardry.mirror.coretypes

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.annotations.TypeAnnotation1
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import io.leangen.geantyref.GenericTypeReflector
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VoidMirrorTest: MirrorTestBase() {
    @Test
    fun coreType_ofNotAnnotated_shouldReturnVoidClass() {
        assertEquals(Void.TYPE, Mirror.Types.void.coreType)
    }

    @Test
    fun coreType_ofAnnotated_shouldReturnVoidClass() {
        val annotatedVoid = GenericTypeReflector.annotate(Void.TYPE, arrayOf(Mirror.newAnnotation<TypeAnnotation1>()))
        assertEquals(Void.TYPE, Mirror.reflect(annotatedVoid).coreType)
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotated_shouldReturnVoidClassWithNoAnnotations() {
        assertEquals(
            GenericTypeReflector.annotate(Void.TYPE),
            Mirror.Types.void.coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotated_shouldReturnVoidClassWithAnnotations() {
        val annotatedVoid = GenericTypeReflector.annotate(Void.TYPE, arrayOf(Mirror.newAnnotation<TypeAnnotation1>()))
        assertEquals(
            annotatedVoid,
            Mirror.reflect(annotatedVoid).coreAnnotatedType
        )
    }
}