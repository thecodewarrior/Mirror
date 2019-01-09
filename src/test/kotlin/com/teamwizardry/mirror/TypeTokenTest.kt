package com.teamwizardry.mirror

import com.teamwizardry.mirror.testsupport.GenericObject1
import com.teamwizardry.mirror.testsupport.Object1
import com.teamwizardry.mirror.testsupport.TypeAnnotation1
import com.teamwizardry.mirror.testsupport.assertInstanceOf
import com.teamwizardry.mirror.testsupport.assertSameList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType

class TypeTokenTest {

    @Test
    @DisplayName("Getting the simple type from a plain class TypeToken should return the Class object")
    fun getSimpleType_withClass_shouldReturnClass() {
        val token = (object : TypeToken<Object1>() {})
        val type = token.get()
        assertSame(Object1::class.java, type)
    }

    @Test
    @DisplayName("Getting the simple type from a generic type TypeToken should return the correct ParameterizedType object")
    fun getSimpleType_withParameterized_shouldReturnParameterizedType() {
        val token = (object : TypeToken<GenericObject1<Object1>>() {})
        val type = token.get()
        assertInstanceOf<ParameterizedType>(type)
        type as ParameterizedType
        assertSameList(listOf(Object1::class.java), type.actualTypeArguments.toList())
    }

    @Test
    @DisplayName("Getting the annotated type from an annotated type TypeToken should return a type with the correct annotations")
    fun getAnnotatedType_withAnnotated_shouldHaveAnnotations() {
        val token = (object : TypeToken<@TypeAnnotation1 Object1>() {})
        val type = token.getAnnotated()
        assertEquals(listOf(Mirror.newAnnotation<TypeAnnotation1>()), type.declaredAnnotations.toList())
    }
}