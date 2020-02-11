package dev.thecodewarrior.mirror.type.coretypes

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.typeholders.type.coretypes.WildcardMirrorTypeHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class WildcardMirrorTest: MirrorTestBase(WildcardMirrorTypeHolder()) {

    @Test
    fun coreType_ofNotAnnotatedRaw_shouldReturnWildcard() {
        assertEquals(
            _t("? extends X").type,
            Mirror.reflect(_t("? extends X").type).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotated_shouldReturnWildcard() {
        assertEquals(
            _t("? extends X").type,
            Mirror.reflect(_t("? extends X")).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedLowerBounded_shouldReturnWildcard() {
        assertEquals(
            _t("? super X").type,
            Mirror.reflect(_t("? super X")).coreType
        )
    }

    @Test
    fun coreType_ofAnnotated_shouldReturnWildcard() {
        assertEquals(
            _t("@A ? extends X").type,
            Mirror.reflect(_t("@A ? extends X")).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedBound_shouldReturnWithBound() {
        assertEquals(
            _t("? extends @A X").type,
            Mirror.reflect(_t("? extends @A X")).coreType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotated_shouldReturnWildcard() {
        assertEquals(
            Mirror.toCanonical(_t("? extends X")),
            Mirror.reflect(_t("? extends X")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotated_shouldReturnAnnotatedWildcard() {
        assertEquals(
            Mirror.toCanonical(_t("@A ? extends X")),
            Mirror.reflect(_t("@A ? extends X")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedBound_shouldReturnWithBound() {
        val mirror = Mirror.reflect(_t("? extends @A X"))
        val canon = Mirror.toCanonical(_t("? extends @A X"))
        assertEquals(
            canon,
            mirror.coreAnnotatedType
        )
    }
}