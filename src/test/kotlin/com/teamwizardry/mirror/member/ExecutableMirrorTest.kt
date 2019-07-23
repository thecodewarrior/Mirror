package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.annotations.Annotation1
import com.teamwizardry.mirror.annotations.AnnotationArg1
import com.teamwizardry.mirror.testsupport.CheckedExceptionMethodHolder
import com.teamwizardry.mirror.testsupport.Exception1
import com.teamwizardry.mirror.testsupport.Exception2
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.NoParamNames
import com.teamwizardry.mirror.testsupport.assertSameList
import com.teamwizardry.mirror.testsupport.assertSetEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class ExecutableMirrorTest: MirrorTestBase() {
    @Test
    @DisplayName("A void method should have no parameters and a VoidMirror return type")
    fun voidMethod() {
        class MethodHolder {
            fun method() {}
        }
        val method = Mirror.reflect(MethodHolder::class.java.getDeclaredMethod("method"))
        assertSame(Mirror.reflect(Void.TYPE), method.returnType)
        assertEquals(emptyList<Any>(), method.parameters)
    }

    @Test
    @DisplayName("A method that returns a value should have the corresponding return type")
    fun methodReturnType() {
        class MethodHolder {
            fun method(): String { null!! }
        }
        val method = Mirror.reflect(MethodHolder::class.java.getDeclaredMethod("method"))
        assertSame(Mirror.reflect<String>(), method.returnType)
    }

    @Test
    @DisplayName("A void method with parameters should have corresponding parameter mirrors")
    fun voidMethodWithParameters() {
        class MethodHolder {
            fun method(paramA: String, paramB: Int) {}
        }
        val method = Mirror.reflect(MethodHolder::class.java.getDeclaredMethod("method", String::class.java, Int::class.javaPrimitiveType))
        assertSameList(listOf(
            Mirror.reflect<String>(),
            Mirror.reflect(Int::class.javaPrimitiveType!!)
        ), method.parameters.map { it.type })
        assertEquals(listOf(
            "paramA",
            "paramB"
        ), method.parameters.map { it.name })
    }

    @Test
    @DisplayName("A void method with unnamed parameters should have corresponding parameter mirrors with null names")
    fun voidMethodWithUnnamedParameters() {
        val method = Mirror.reflect(NoParamNames::class.java.getDeclaredMethod("stringIntParams", String::class.java, Int::class.javaPrimitiveType))
        assertSameList(listOf(
            Mirror.reflect<String>(),
            Mirror.reflect(Int::class.javaPrimitiveType!!)
        ), method.parameters.map { it.type })
        assertEquals(listOf(
            null,
            null
        ), method.parameters.map { it.name })
    }

    @Test
    @DisplayName("A method with checked exceptions should have corresponding type mirrors")
    fun voidMethodWithCheckedExceptions() {
        val method = Mirror.reflect(CheckedExceptionMethodHolder::class.java.getDeclaredMethod("method"))
        assertSameList(listOf(
            Mirror.reflect<Exception1>(),
            Mirror.reflect<Exception2>()
        ), method.exceptionTypes)
    }

    @Test
    @DisplayName("A method with checked exceptions should have corresponding type mirrors")
    fun voidMethodWithTypeParameters() {
        class MethodHolder {
            fun <T> method() {}
        }
        val javaMethod = MethodHolder::class.java.getDeclaredMethod("method")
        val method = Mirror.reflect(javaMethod)
        assertSameList(listOf(
            Mirror.reflect(javaMethod.typeParameters[0])
        ), method.typeParameters)
    }

    @Test
    @DisplayName("A method that has no annotations should have an empty annotations list")
    fun nonAnnotatedMethod() {
        class MethodHolder {
            fun method() { }
        }
        val method = Mirror.reflect(MethodHolder::class.java.getDeclaredMethod("method"))
        assertEquals(emptyList<Annotation>(), method.annotations)
    }

    @Test
    @DisplayName("A method that has annotations should have an annotations list containing those annotations")
    fun annotatedMethod() {
        class MethodHolder {
            @Annotation1
            @AnnotationArg1(arg = 1)
            fun method() { }
        }
        val method = Mirror.reflect(MethodHolder::class.java.getDeclaredMethod("method"))
        assertSetEquals(listOf(
            Mirror.newAnnotation<Annotation1>(),
            Mirror.newAnnotation<AnnotationArg1>("arg" to 1)
        ), method.annotations)
    }

    @Test
    @DisplayName("Parameters that have no annotations should have empty annotation lists")
    fun nonAnnotatedParameter() {
        class MethodHolder {
            fun method(paramA: String) {}
        }
        val method = Mirror.reflect(MethodHolder::class.java.getDeclaredMethod("method", String::class.java))
        assertEquals(emptyList<Annotation>(), method.parameters[0].annotations)
    }

    @Test
    @DisplayName("Parameters that have annotations should have annotation lists containing those annotations")
    fun annotatedParameter() {
        class MethodHolder {
            fun method(@Annotation1 @AnnotationArg1(arg = 1) paramA: String) {}
        }
        val method = Mirror.reflect(MethodHolder::class.java.getDeclaredMethod("method", String::class.java))
        assertSetEquals(listOf(
            Mirror.newAnnotation<Annotation1>(),
            Mirror.newAnnotation<AnnotationArg1>("arg" to 1)
        ), method.parameters[0].annotations)
    }
}