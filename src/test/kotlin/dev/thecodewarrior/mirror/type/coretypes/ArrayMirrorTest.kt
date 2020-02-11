package dev.thecodewarrior.mirror.type.coretypes

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.typeholders.type.coretypes.ArrayMirrorTypeHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ArrayMirrorTest: MirrorTestBase(ArrayMirrorTypeHolder()) {

    @Test
    fun coreType_ofRawNotAnnotated_shouldReturnRaw() {
        assertEquals(
            _t("X[]").type,
            Mirror.reflect(_t("X[]")).coreType
        )
    }

    @Test
    fun coreType_ofGenericNotAnnotated_shouldReturnRaw() {
        assertEquals(
            _t("Generic<X>[]").type,
            Mirror.reflect(_t("Generic<X>[]")).coreType
        )
    }

    @Test
    fun coreType_ofRawAnnotated_shouldReturnRaw() {
        assertEquals(
            _t("@A X @A []").type,
            Mirror.reflect(_t("@A X @A []")).coreType
        )
    }

    @Test
    fun coreType_ofGenericAnnotated_shouldReturnRaw() {
        assertEquals(
            _t("Generic<@A X>[]").type,
            Mirror.reflect(_t("Generic<@A X>[]")).coreType
        )
    }

    @Test
    fun coreAnnotatedType_ofRawNotAnnotated_shouldReturnAnnotated() {
        assertEquals(
            Mirror.toCanonical(_t("X[]")),
            Mirror.reflect(_t("X[]")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofGenericNotAnnotated_shouldReturnAnnotated() {
        assertEquals(
            Mirror.toCanonical(_t("Generic<X>[]")),
            Mirror.reflect(_t("Generic<X>[]")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofRawAnnotated_shouldReturnAnnotated() {
        assertEquals(
            Mirror.toCanonical(_t("@A X @A []")),
            Mirror.reflect(_t("@A X @A []")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofGenericAnnotated_shouldReturnAnnotated() {
        assertEquals(
            Mirror.toCanonical(_t("Generic<@A X>[]")),
            Mirror.reflect(_t("Generic<@A X>[]")).coreAnnotatedType
        )
    }
}