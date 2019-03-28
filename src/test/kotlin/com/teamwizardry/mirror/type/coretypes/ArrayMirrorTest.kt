package com.teamwizardry.mirror.type.coretypes

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.canonical
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.typeholders.TypeMirror_CoreTypesHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ArrayMirrorTest: MirrorTestBase() {
    private val holder = TypeMirror_CoreTypesHolder()

    @Test
    fun coreType_ofRawNotAnnotated_shouldReturnRaw() {
        assertEquals(
            holder["Object1[]"].type,
            Mirror.reflect(holder["Object1[]"]).coreType
        )
    }

    @Test
    fun coreType_ofGenericNotAnnotated_shouldReturnRaw() {
        assertEquals(
            holder["GenericObject1<Object1>[]"].type,
            Mirror.reflect(holder["GenericObject1<Object1>[]"]).coreType
        )
    }

    @Test
    fun coreType_ofRawAnnotated_shouldReturnRaw() {
        assertEquals(
            holder["@TypeAnnotation1 Object1 @TypeAnnotation1 []"].type,
            Mirror.reflect(holder["@TypeAnnotation1 Object1 @TypeAnnotation1 []"]).coreType
        )
    }

    @Test
    fun coreType_ofGenericAnnotated_shouldReturnRaw() {
        assertEquals(
            holder["GenericObject1<@TypeAnnotation1 Object1>[]"].type,
            Mirror.reflect(holder["GenericObject1<@TypeAnnotation1 Object1>[]"]).coreType
        )
    }

    @Test
    fun coreAnnotatedType_ofRawNotAnnotated_shouldReturnAnnotated() {
        assertEquals(
            holder["Object1[]"].canonical,
            Mirror.reflect(holder["Object1[]"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofGenericNotAnnotated_shouldReturnAnnotated() {
        assertEquals(
            holder["GenericObject1<Object1>[]"].canonical,
            Mirror.reflect(holder["GenericObject1<Object1>[]"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofRawAnnotated_shouldReturnAnnotated() {
        assertEquals(
            holder["@TypeAnnotation1 Object1 @TypeAnnotation1 []"].canonical,
            Mirror.reflect(holder["@TypeAnnotation1 Object1 @TypeAnnotation1 []"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofGenericAnnotated_shouldReturnAnnotated() {
        assertEquals(
            holder["GenericObject1<@TypeAnnotation1 Object1>[]"].canonical,
            Mirror.reflect(holder["GenericObject1<@TypeAnnotation1 Object1>[]"]).coreAnnotatedType
        )
    }
}