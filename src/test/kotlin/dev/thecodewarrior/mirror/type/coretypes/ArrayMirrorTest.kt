package dev.thecodewarrior.mirror.type.coretypes

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ArrayMirrorTest: MTest() {
    val A by sources.add("A", "@rt(TYPE_USE) @interface A {}")
    val X by sources.add("X", "class X {}")
    val Generic by sources.add("Generic", "class Generic<T> {}")

    val types = sources.types {
        +"X[]"
        +"Generic<X>[]"
        +"@A X @A []"
        +"Generic<@A X>[]"
        typeVariables("K", "V") {
            +"K[]"
            +"@A Generic<V>"
        }
    }

    @Test
    fun coreType_ofRawNotAnnotated_shouldReturnRaw() {
        assertEquals(
            types["X[]"].type,
            Mirror.reflect(types["X[]"]).coreType
        )
    }

    @Test
    fun coreType_ofGenericNotAnnotated_shouldReturnRaw() {
        assertEquals(
            types["Generic<X>[]"].type,
            Mirror.reflect(types["Generic<X>[]"]).coreType
        )
    }

    @Test
    fun coreType_ofRawAnnotated_shouldReturnRaw() {
        assertEquals(
            types["@A X @A []"].type,
            Mirror.reflect(types["@A X @A []"]).coreType
        )
    }

    @Test
    fun coreType_ofGenericAnnotated_shouldReturnRaw() {
        assertEquals(
            types["Generic<@A X>[]"].type,
            Mirror.reflect(types["Generic<@A X>[]"]).coreType
        )
    }

    @Test
    fun coreAnnotatedType_ofRawNotAnnotated_shouldReturnAnnotated() {
        assertEquals(
            Mirror.toCanonical(types["X[]"]),
            Mirror.reflect(types["X[]"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofGenericNotAnnotated_shouldReturnAnnotated() {
        assertEquals(
            Mirror.toCanonical(types["Generic<X>[]"]),
            Mirror.reflect(types["Generic<X>[]"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofRawAnnotated_shouldReturnAnnotated() {
        assertEquals(
            Mirror.toCanonical(types["@A X @A []"]),
            Mirror.reflect(types["@A X @A []"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofGenericAnnotated_shouldReturnAnnotated() {
        assertEquals(
            Mirror.toCanonical(types["Generic<@A X>[]"]),
            Mirror.reflect(types["Generic<@A X>[]"]).coreAnnotatedType
        )
    }
}