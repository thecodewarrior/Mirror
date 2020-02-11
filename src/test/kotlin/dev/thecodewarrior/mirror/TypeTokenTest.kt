package dev.thecodewarrior.mirror

import dev.thecodewarrior.mirror.annotations.TypeAnnotation1
import dev.thecodewarrior.mirror.testsupport.GenericObject1
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.assertInstanceOf
import dev.thecodewarrior.mirror.testsupport.assertSameList
import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.TypeToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType

class TypeTokenTest {

    @Test
    fun getSimpleType_withClassToken_shouldReturnClass() {
        val token = (object : TypeToken<Object1>() {})
        val type = token.get()
        assertSame(Object1::class.java, type)
    }

    @Test
    fun getSimpleType_withParameterizedToken_shouldReturnParameterizedType() {
        val token = (object : TypeToken<GenericObject1<Object1>>() {})
        val type = token.get()
        assertInstanceOf<ParameterizedType>(type)
        type as ParameterizedType
        assertSameList(listOf(Object1::class.java), type.actualTypeArguments.toList())
    }

//    @Disabled("Only works when used in Java code compiled with Java 10+ https://github.com/raphw/byte-buddy/issues/583")
//    @Test
    fun getAnnotatedType_withAnnotatedToken_shouldHaveAnnotations() {
        val token = (object : TypeToken<@TypeAnnotation1 Object1>() {})
        val type = token.getAnnotated()
        assertEquals(listOf(Mirror.newAnnotation<TypeAnnotation1>()), type.declaredAnnotations.toList())
    }
}