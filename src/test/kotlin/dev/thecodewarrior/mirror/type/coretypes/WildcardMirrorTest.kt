package dev.thecodewarrior.mirror.type.coretypes

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.canonical
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.typeholders.TypeMirror_CoreTypesHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WildcardMirrorTest: MirrorTestBase() {
    private val holder = TypeMirror_CoreTypesHolder()

    @Test
    fun coreType_ofNotAnnotatedRaw_shouldReturnWildcard() {
        assertEquals(
            holder["? extends Object1"].type,
            Mirror.reflect(holder["? extends Object1"].type).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotated_shouldReturnWildcard() {
        assertEquals(
            holder["? extends Object1"].type,
            Mirror.reflect(holder["? extends Object1"]).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedLowerBounded_shouldReturnWildcard() {
        assertEquals(
            holder["? super Object1"].type,
            Mirror.reflect(holder["? super Object1"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotated_shouldReturnWildcard() {
        assertEquals(
            holder["@TypeAnnotation1 ? extends Object1"].type,
            Mirror.reflect(holder["@TypeAnnotation1 ? extends Object1"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedBound_shouldReturnWithBound() {
        assertEquals(
            holder["? extends @TypeAnnotation1 Object1"].type,
            Mirror.reflect(holder["? extends @TypeAnnotation1 Object1"]).coreType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotated_shouldReturnWildcard() {
        assertEquals(
            holder["? extends Object1"].canonical,
            Mirror.reflect(holder["? extends Object1"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotated_shouldReturnAnnotatedWildcard() {
        assertEquals(
            holder["@TypeAnnotation1 ? extends Object1"].canonical,
            Mirror.reflect(holder["@TypeAnnotation1 ? extends Object1"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedBound_shouldReturnWithBound() {
        val mirror = Mirror.reflect(holder["? extends @TypeAnnotation1 Object1"])
        val canon = holder["? extends @TypeAnnotation1 Object1"].canonical
        assertEquals(
            canon,
            mirror.coreAnnotatedType
        )
    }
}