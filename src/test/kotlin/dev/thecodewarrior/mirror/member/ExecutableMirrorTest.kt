package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.NoParamNames
import dev.thecodewarrior.mirror.annotations.Annotation1
import dev.thecodewarrior.mirror.annotations.AnnotationArg1
import dev.thecodewarrior.mirror.testsupport.Exception1
import dev.thecodewarrior.mirror.testsupport.Exception2
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.assertSameList
import dev.thecodewarrior.mirror.testsupport.assertSetEquals
import dev.thecodewarrior.mirror.typeholders.member.ExecutableMirrorHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.javaMethod

internal class ExecutableMirrorTest: MirrorTestBase() {
    private val holder = ExecutableMirrorHolder()

    @Test
    fun name_ofConstructor_shouldBeClassName() {
        val constructor = Mirror.reflect(holder.getConstructor("<init>()"))
        assertEquals(ExecutableMirrorHolder::class.qualifiedName!!, constructor.name)
    }

    @Test
    fun name_withName_shouldReturnCorrectName() {
        val method = Mirror.reflect(holder.m("void name()"))
        assertEquals("name", method.name)
    }

    @Test
    fun parameters_withNoParameters_shouldReturnEmptyList() {
        val method = Mirror.reflect(holder.m("void ()"))
        assertEquals(emptyList<Any>(), method.parameters)
    }

    @Test
    fun returnType_withVoidReturn_shouldReturnVoid() {
        val method = Mirror.reflect(holder.m("void ()"))
        assertSame(Mirror.types.void, method.returnType)
    }

    @Test
    fun returnType_withReturnType_shouldReturnCorrectType() {
        val method = Mirror.reflect(holder.m("String ()"))
        assertSame(Mirror.reflect<String>(), method.returnType)
    }

    @Test
    fun parameters_withParameters_shouldHaveCorrectNameTypeAndJava() {
        val method = Mirror.reflect(holder.m("void (String, int)"))
        assertSameList(listOf(
            Mirror.reflect<String>(),
            Mirror.types.int
        ), method.parameters.map { it.type })
        assertEquals(listOf(
            "s",
            "i"
        ), method.parameters.map { it.name })
        assertEquals(listOf(
            holder.p("void (String, int) > s"),
            holder.p("void (String, int) > i")
        ), method.parameters.map { it.java })
    }

    @Test
    fun parameterTypes_withNoParameters_shouldBeEmpty() {
        val method = Mirror.reflect(holder.m("void ()"))
        assertSameList(emptyList(), method.parameterTypes)
    }

    @Test
    fun parameterTypes_withParameters_shouldHaveCorrectTypes() {
        val method = Mirror.reflect(holder.m("void (String, int)"))
        assertSameList(listOf(
            Mirror.reflect<String>(),
            Mirror.types.int
        ), method.parameterTypes)
    }

    @Test
    @DisplayName("A void method with unnamed parameters should have corresponding parameter mirrors with null names")
    fun parameters_withNoBytecodeNames_shouldHaveNullNames() {
        val method = Mirror.reflect(NoParamNames::noNames.javaMethod!!)
        assertEquals(listOf(
            null
        ), method.parameters.map { it.name })
    }

    @Test
    fun exceptionTypes_withNoExceptions_shouldReturnExceptionTypes() {
        val method = Mirror.reflect(holder.m("void ()"))
        assertSameList(emptyList(), method.exceptionTypes)
    }

    @Test
    fun exceptionTypes_withCheckedExceptions_shouldReturnExceptionTypes() {
        val method = Mirror.reflect(holder.m("void () throws"))
        assertSameList(listOf(
            Mirror.reflect<Exception1>(),
            Mirror.reflect<Exception2>()
        ), method.exceptionTypes)
    }

    @Test
    fun typeParameters_withNoTypeParameters_shouldBeEmpty() {
        val method = Mirror.reflect(holder.m("void ()"))
        assertSameList(emptyList(), method.typeParameters)
    }

    @Test
    fun typeParameters_withTypeParameters_shouldHaveCorrectTypeParameters() {
        val method = Mirror.reflect(holder.m("<T> void (T)"))
        assertSameList(listOf(
            Mirror.reflect(holder.t("<T> void (T) > T"))
        ), method.typeParameters)
    }

    @Test
    fun annotations_withNoAnnotations_shouldReturnEmptyList() {
        val method = Mirror.reflect(ExecutableMirrorHolder::noMethodAnnotations.javaMethod!!)
        assertEquals(emptyList<Annotation>(), method.annotations)
    }

    @Test
    fun annotations_withAnnotatedMethod_shouldReturnAnnotations() {
        val method = Mirror.reflect(ExecutableMirrorHolder::methodAnnotations.javaMethod!!)
        assertSetEquals(listOf(
            Mirror.newAnnotation<Annotation1>(),
            Mirror.newAnnotation<AnnotationArg1>("arg" to 1)
        ), method.annotations)
    }

    @Test
    fun parameterAnnotations_withNoAnnotations_shouldReturnEmptyList() {
        val method = Mirror.reflect(holder.m("void (_)"))
        assertEquals(emptyList<Annotation>(), method.parameters[0].annotations)
    }

    @Test
    @DisplayName("Parameters that have annotations should have annotation lists containing those annotations")
    fun parameterAnnotations_withAnnotations_shouldReturnAnnotations() {
        val method = Mirror.reflect(holder.m("void (@- @- String)"))
        assertSetEquals(listOf(
            Mirror.newAnnotation<Annotation1>(),
            Mirror.newAnnotation<AnnotationArg1>("arg" to 1)
        ), method.parameters[0].annotations)
    }
}