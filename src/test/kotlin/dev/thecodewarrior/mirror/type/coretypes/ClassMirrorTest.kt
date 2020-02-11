package dev.thecodewarrior.mirror.type.coretypes

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.typeholders.type.coretypes.ClassMirrorTypeHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ClassMirrorTest: MirrorTestBase(ClassMirrorTypeHolder()) {

    @Test
    fun coreType_ofNotAnnotatedNoOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            _t("Generic").type,
            Mirror.reflect(_t("Generic")).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedNoOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            _t("Generic<X>").type,
            Mirror.reflect(_t("Generic<X>")).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedRawOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            _t("Generic.InnerGeneric").type,
            Mirror.reflect(_t("Generic.InnerGeneric")).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedGenericOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            _t("Generic<X>.Inner").type,
            Mirror.reflect(_t("Generic<X>.Inner")).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedGenericOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            _t("Generic<X>.InnerGeneric<Y>").type,
            Mirror.reflect(_t("Generic<X>.InnerGeneric<Y>")).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedNoOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            _t("@A Generic").type,
            Mirror.reflect(_t("@A Generic")).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedNoOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            _t("@A Generic<@A X>").type,
            Mirror.reflect(_t("@A Generic<@A X>")).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedRawOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            _t("@A Generic.InnerGeneric").type,
            Mirror.reflect(_t("@A Generic.InnerGeneric")).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedGenericOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            _t("Generic<@A X>.@A Inner").type,
            Mirror.reflect(_t("Generic<@A X>.@A Inner")).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedGenericOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            _t("Generic<@A X>.@A InnerGeneric<Y>").type,
            Mirror.reflect(_t("Generic<@A X>.@A InnerGeneric<Y>")).coreType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedNoOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(_t("Generic")),
            Mirror.reflect(_t("Generic")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedNoOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(_t("Generic<X>")),
            Mirror.reflect(_t("Generic<X>")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedRawOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(_t("Generic.InnerGeneric")),
            Mirror.reflect(_t("Generic.InnerGeneric")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedGenericOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(_t("Generic<X>.Inner")),
            Mirror.reflect(_t("Generic<X>.Inner")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedGenericOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(_t("Generic<X>.InnerGeneric<Y>")),
            Mirror.reflect(_t("Generic<X>.InnerGeneric<Y>")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedNoOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(_t("@A Generic")),
            Mirror.reflect(_t("@A Generic")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedNoOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(_t("@A Generic<@A X>")),
            Mirror.reflect(_t("@A Generic<@A X>")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedRawOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(_t("@A Generic.InnerGeneric")),
            Mirror.reflect(_t("@A Generic.InnerGeneric")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedGenericOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(_t("Generic<@A X>.@A Inner")),
            Mirror.reflect(_t("Generic<@A X>.@A Inner")).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedGenericOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(_t("Generic<@A X>.@A InnerGeneric<Y>")),
            Mirror.reflect(_t("Generic<@A X>.@A InnerGeneric<Y>")).coreAnnotatedType
        )
    }
}