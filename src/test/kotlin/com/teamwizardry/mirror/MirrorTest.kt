package com.teamwizardry.mirror

import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.Object1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.reflect.AnnotatedArrayType

internal class MirrorTest: MirrorTestBase() {

    @Test
    @DisplayName("newAnnotation delegates to an external tested library")
    fun newAnnotation_needsNoTests() {
    }

    @Test
    fun createArrayType_withClass_shouldReturnTypedArray() {
        assertEquals(
            Mirror.reflect<Array<Object1>>(),
            Mirror.createArrayType(Mirror.reflect<Object1>())
        )
    }

    @Test
    fun createArrayType_withArray_shouldReturn2dArray() {
        assertEquals(
            Mirror.reflect<Array<Array<Any>>>(),
            Mirror.createArrayType(Mirror.reflect<Array<Any>>())
        )
    }

    @Test
    fun createArrayType_withGeneric_shouldReturnGeneric() {
        class FieldHolder<T>(
            @JvmField
            val field: Array<T>
        )
        val genericArray = FieldHolder::class.java.getField("field").annotatedType as AnnotatedArrayType
        val typeVariable = FieldHolder::class.java.typeParameters[0]
        assertEquals(
            Mirror.reflect(genericArray),
            Mirror.createArrayType(Mirror.reflect(typeVariable))
        )
    }
}
