package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.Interface1
import dev.thecodewarrior.mirror.testsupport.Interface2
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.assertSameList
import dev.thecodewarrior.mirror.typeholders.TypeMirrorHolder
import dev.thecodewarrior.mirror.type.VariableMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class VariableMirrorTest: MirrorTestBase(TypeMirrorHolder()) {

    @Test
    fun getBounds_onUnboundedType_shouldReturnListOfObject() {
        val type = Mirror.reflect(holder["T"]) as VariableMirror
        assertEquals(listOf(Mirror.reflect<Any>()), type.bounds)
    }

    @Test
    fun getBounds_onTypeWithSingleBound_shouldReturnListOfBound() {
        val type = Mirror.reflect(holder["T extends Object1"]) as VariableMirror
        assertEquals(listOf(Mirror.reflect<Object1>()), type.bounds)
    }

    @Test
    fun getBounds_onTypeWithMultipleBounds_shouldReturnListOfBoundsInSourceOrder() {
        // Interface2 before Interface1 to thwart any sort-by-name bugs
        val type = Mirror.reflect(holder["T extends Interface2 & Interface1"]) as VariableMirror
        assertSameList(listOf(Mirror.reflect<Interface2>(), Mirror.reflect<Interface1>()), type.bounds)
    }

    @Test
    fun getBounds_onTypeWithAnnotatedBound_shouldReturnListOfAnnotatedBound() {
        val type = Mirror.reflect(holder["T extends @TypeAnnotation1 Object1"]) as VariableMirror
        assertEquals(listOf(Mirror.reflect(holder["@TypeAnnotation1 Object1"])), type.bounds)
    }
}