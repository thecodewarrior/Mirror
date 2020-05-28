package dev.thecodewarrior.mirror.type.coretypes

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class WildcardMirrorTest: MTest() {
    val A by sources.add("A", """
        @Target(ElementType.TYPE_USE)
        @interface A {}
    """)
    val X by sources.add("X", "class X {}")

    val types = sources.types {
        +"? extends X"
        +"? super X"
        +"@A ? extends X"
        +"? extends @A X"
    }

    @Test
    fun coreType_ofNotAnnotatedRaw_shouldReturnWildcard() {
        assertEquals(
            types["? extends X"].type,
            Mirror.reflect(types["? extends X"].type).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotated_shouldReturnWildcard() {
        assertEquals(
            types["? extends X"].type,
            Mirror.reflect(types["? extends X"]).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedLowerBounded_shouldReturnWildcard() {
        assertEquals(
            types["? super X"].type,
            Mirror.reflect(types["? super X"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotated_shouldReturnWildcard() {
        assertEquals(
            types["@A ? extends X"].type,
            Mirror.reflect(types["@A ? extends X"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedBound_shouldReturnWithBound() {
        assertEquals(
            types["? extends @A X"].type,
            Mirror.reflect(types["? extends @A X"]).coreType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotated_shouldReturnWildcard() {
        assertEquals(
            Mirror.toCanonical(types["? extends X"]),
            Mirror.reflect(types["? extends X"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotated_shouldReturnAnnotatedWildcard() {
        assertEquals(
            Mirror.toCanonical(types["@A ? extends X"]),
            Mirror.reflect(types["@A ? extends X"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedBound_shouldReturnWithBound() {
        val mirror = Mirror.reflect(types["? extends @A X"])
        val canon = Mirror.toCanonical(types["? extends @A X"])
        assertEquals(
            canon,
            mirror.coreAnnotatedType
        )
    }
}