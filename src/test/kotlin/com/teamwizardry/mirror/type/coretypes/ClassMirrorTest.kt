package com.teamwizardry.mirror.type.coretypes

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.canonical
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.typeholders.TypeMirror_CoreTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClassMirrorTest: MirrorTestBase() {
    private val holder = TypeMirror_CoreTypes()

    @Test
    fun coreType_ofNotAnnotatedNoOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["GenericObject1"].type,
            Mirror.reflect(holder["GenericObject1"]).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedNoOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["GenericObject1<Object1>"].type,
            Mirror.reflect(holder["GenericObject1<Object1>"]).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedRawOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["OuterGenericClass1.InnerGenericClass"].type,
            Mirror.reflect(holder["OuterGenericClass1.InnerGenericClass"]).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedGenericOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["OuterGenericClass1<Object1>.InnerClass"].type,
            Mirror.reflect(holder["OuterGenericClass1<Object1>.InnerClass"]).coreType
        )
    }

    @Test
    fun coreType_ofNotAnnotatedGenericOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["OuterGenericClass1<Object1>.InnerGenericClass<Object2>"].type,
            Mirror.reflect(holder["OuterGenericClass1<Object1>.InnerGenericClass<Object2>"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedNoOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["@TypeAnnotation1 GenericObject1"].type,
            Mirror.reflect(holder["@TypeAnnotation1 GenericObject1"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedNoOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["@TypeAnnotation1 GenericObject1<@TypeAnnotation1 Object1>"].type,
            Mirror.reflect(holder["@TypeAnnotation1 GenericObject1<@TypeAnnotation1 Object1>"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedRawOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["@TypeAnnotation1 OuterGenericClass1.InnerGenericClass"].type,
            Mirror.reflect(holder["@TypeAnnotation1 OuterGenericClass1.InnerGenericClass"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedGenericOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["OuterGenericClass1<@TypeAnnotation1 Object1>.@TypeAnnotation1 InnerClass"].type,
            Mirror.reflect(holder["OuterGenericClass1<@TypeAnnotation1 Object1>.@TypeAnnotation1 InnerClass"]).coreType
        )
    }

    @Test
    fun coreType_ofAnnotatedGenericOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["OuterGenericClass1<@TypeAnnotation1 Object1>.@TypeAnnotation1 InnerGenericClass<Object2>"].type,
            Mirror.reflect(holder["OuterGenericClass1<@TypeAnnotation1 Object1>.@TypeAnnotation1 InnerGenericClass<Object2>"]).coreType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedNoOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["GenericObject1"].canonical,
            Mirror.reflect(holder["GenericObject1"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedNoOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["GenericObject1<Object1>"].canonical,
            Mirror.reflect(holder["GenericObject1<Object1>"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedRawOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["OuterGenericClass1.InnerGenericClass"].canonical,
            Mirror.reflect(holder["OuterGenericClass1.InnerGenericClass"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedGenericOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["OuterGenericClass1<Object1>.InnerClass"].canonical,
            Mirror.reflect(holder["OuterGenericClass1<Object1>.InnerClass"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofNotAnnotatedGenericOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["OuterGenericClass1<Object1>.InnerGenericClass<Object2>"].canonical,
            Mirror.reflect(holder["OuterGenericClass1<Object1>.InnerGenericClass<Object2>"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedNoOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["@TypeAnnotation1 GenericObject1"].canonical,
            Mirror.reflect(holder["@TypeAnnotation1 GenericObject1"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedNoOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["@TypeAnnotation1 GenericObject1<@TypeAnnotation1 Object1>"].canonical,
            Mirror.reflect(holder["@TypeAnnotation1 GenericObject1<@TypeAnnotation1 Object1>"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedRawOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["@TypeAnnotation1 OuterGenericClass1.InnerGenericClass"].canonical,
            Mirror.reflect(holder["@TypeAnnotation1 OuterGenericClass1.InnerGenericClass"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedGenericOwnerNoParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["OuterGenericClass1<@TypeAnnotation1 Object1>.@TypeAnnotation1 InnerClass"].canonical,
            Mirror.reflect(holder["OuterGenericClass1<@TypeAnnotation1 Object1>.@TypeAnnotation1 InnerClass"]).coreAnnotatedType
        )
    }

    @Test
    fun coreAnnotatedType_ofAnnotatedGenericOwnerWithParams_shouldReturnAnnotationsStripped() {
        assertEquals(
            holder["OuterGenericClass1<@TypeAnnotation1 Object1>.@TypeAnnotation1 InnerGenericClass<Object2>"].canonical,
            Mirror.reflect(holder["OuterGenericClass1<@TypeAnnotation1 Object1>.@TypeAnnotation1 InnerGenericClass<Object2>"]).coreAnnotatedType
        )
    }
}