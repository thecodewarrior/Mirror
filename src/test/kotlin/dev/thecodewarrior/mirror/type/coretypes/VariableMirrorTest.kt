package dev.thecodewarrior.mirror.type.coretypes

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.typeholders.type.coretypes.VariableMirrorTypeHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class VariableMirrorTest: MirrorTestBase(VariableMirrorTypeHolder()) {

    @Test
    fun coreType_ofNotAnnotated_shouldReturnTypeParameter() {
        assertEquals(
            _t("T").type,
            Mirror.reflect(_t("T")).coreType
        )
    }

    @Test
    fun coreType_ofAnnotated_shouldReturnTypeParameter() {
        assertEquals(
            _t("T").type,
            Mirror.reflect(_t("@A T")).coreType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotated_shouldReturnTypeParameter() {
        assertEquals(
            Mirror.toCanonical(_t("T")),
            Mirror.reflect(_t("T")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotated_shouldReturnAnnotatedTypeParameter() {
        assertEquals(
            Mirror.toCanonical(_t("T")),
            Mirror.reflect(_t("T")).coreAnnotatedType
        )
    }

}