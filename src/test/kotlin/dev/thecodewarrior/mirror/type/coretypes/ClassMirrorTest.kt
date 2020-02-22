package dev.thecodewarrior.mirror.type.coretypes

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ClassMirrorTest: MTest() {
    val A by sources.add("A", """
        @Target(ElementType.TYPE_USE)
        @interface A {}
    """.trimIndent())
    val X by sources.add("X", "class X {}")
    val Y by sources.add("Y", "class Y {}")
    val Generic by sources.add("Generic", """
        class Generic<Z> {
            class Inner {}
            class InnerGeneric<W> {}
        }
    """.trimIndent())
    val types = sources.types {
        +"Generic"
        +"Generic<X>"
        +"Generic.InnerGeneric"
        +"Generic<X>.Inner"
        +"Generic<X>.InnerGeneric<Y>"
        +"@A Generic"
        +"@A Generic<@A X>"
        +"@A Generic.InnerGeneric"
        +"Generic<@A X>.@A Inner"
        +"Generic<@A X>.@A InnerGeneric<Y>"
    }

    @Test
    fun coreType_ofNotAnnotatedNoOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            types["Generic"].type,
            Mirror.reflect(types["Generic"]).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedNoOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            types["Generic<X>"].type,
            Mirror.reflect(types["Generic<X>"]).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedRawOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            types["Generic.InnerGeneric"].type,
            Mirror.reflect(types["Generic.InnerGeneric"]).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedGenericOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            types["Generic<X>.Inner"].type,
            Mirror.reflect(types["Generic<X>.Inner"]).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedGenericOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            types["Generic<X>.InnerGeneric<Y>"].type,
            Mirror.reflect(types["Generic<X>.InnerGeneric<Y>"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedNoOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            types["@A Generic"].type,
            Mirror.reflect(types["@A Generic"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedNoOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            types["@A Generic<@A X>"].type,
            Mirror.reflect(types["@A Generic<@A X>"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedRawOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            types["@A Generic.InnerGeneric"].type,
            Mirror.reflect(types["@A Generic.InnerGeneric"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedGenericOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            types["Generic<@A X>.@A Inner"].type,
            Mirror.reflect(types["Generic<@A X>.@A Inner"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedGenericOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            types["Generic<@A X>.@A InnerGeneric<Y>"].type,
            Mirror.reflect(types["Generic<@A X>.@A InnerGeneric<Y>"]).coreType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedNoOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(types["Generic"]),
            Mirror.reflect(types["Generic"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedNoOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(types["Generic<X>"]),
            Mirror.reflect(types["Generic<X>"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedRawOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(types["Generic.InnerGeneric"]),
            Mirror.reflect(types["Generic.InnerGeneric"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedGenericOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(types["Generic<X>.Inner"]),
            Mirror.reflect(types["Generic<X>.Inner"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedGenericOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(types["Generic<X>.InnerGeneric<Y>"]),
            Mirror.reflect(types["Generic<X>.InnerGeneric<Y>"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedNoOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(types["@A Generic"]),
            Mirror.reflect(types["@A Generic"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedNoOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(types["@A Generic<@A X>"]),
            Mirror.reflect(types["@A Generic<@A X>"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedRawOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(types["@A Generic.InnerGeneric"]),
            Mirror.reflect(types["@A Generic.InnerGeneric"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedGenericOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(types["Generic<@A X>.@A Inner"]),
            Mirror.reflect(types["Generic<@A X>.@A Inner"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedGenericOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            Mirror.toCanonical(types["Generic<@A X>.@A InnerGeneric<Y>"]),
            Mirror.reflect(types["Generic<@A X>.@A InnerGeneric<Y>"]).coreAnnotatedType
        )
    }
}