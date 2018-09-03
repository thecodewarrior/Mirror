package com.teamwizardry.mirror.specialization

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.GenericCheckedExceptionMethodHolder
import com.teamwizardry.mirror.testsupport.GenericInterface1
import com.teamwizardry.mirror.testsupport.GenericPairInterface1
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.Object1
import com.teamwizardry.mirror.testsupport.assertSameList
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class MethodSpecializationTest: MirrorTestBase() {
    //region Class generic
    @Test
    @DisplayName("Specializing a class should specialize the return types of each of its declared methods")
    fun specializeMethodReturnsWithClassGeneric() {
        class MethodHolder<T> {
            fun directGeneric(): T { TODO() }
            fun indirectGeneric(): GenericInterface1<T> { TODO() }
        }

        val baseType = Mirror.reflectClass<MethodHolder<*>>()
        val specializedType = baseType.specialize(Mirror.reflect<String>())
        val specializedDirectMethod = specializedType.declaredMethods.find { it.name == "directGeneric" }!!
        val specializedIndirectMethod = specializedType.declaredMethods.find { it.name == "indirectGeneric" }!!

        assertSame(Mirror.reflect<String>(), specializedDirectMethod.returnType)
        assertSame(Mirror.reflect<GenericInterface1<String>>(), specializedIndirectMethod.returnType)
    }

    @Test
    @DisplayName("Specializing a class should specialize the parameter types of each of its declared methods")
    fun specializeMethodParamsWithClassGeneric() {
        class MethodHolder<T> {
            fun directGeneric(param1: T) { TODO() }
            fun indirectGeneric(param1: GenericInterface1<T>) { TODO() }
        }

        val baseType = Mirror.reflectClass<MethodHolder<*>>()
        val specializedType = baseType.specialize(Mirror.reflect<String>())
        val specializedDirectMethod = specializedType.declaredMethods.find { it.name == "directGeneric" }!!
        val specializedIndirectMethod = specializedType.declaredMethods.find { it.name == "indirectGeneric" }!!

        assertSame(Mirror.reflect<String>(), specializedDirectMethod.parameterTypes[0])
        assertSame(Mirror.reflect<GenericInterface1<String>>(), specializedIndirectMethod.parameterTypes[0])
    }

    @Test
    @DisplayName("Specializing a class should specialize the checked exception types of each of its declared methods")
    fun specializeMethodExceptionsWithClassGeneric() {
        val baseType = Mirror.reflectClass<GenericCheckedExceptionMethodHolder<*>>()
        val specializedType = baseType.specialize(Mirror.reflect<RuntimeException>())
        val specializedMethod = specializedType.declaredMethods.find { it.name == "generic" }!!

        assertSame(Mirror.reflect<RuntimeException>(), specializedMethod.exceptionTypes[0])
    }
    //endregion

    //region Method generic
    @Test
    @DisplayName("Specializing a method should specialize its return type")
    fun specializeMethodReturnsWithMethodGeneric() {
        class MethodHolder {
            fun <T> directGeneric(): T { TODO() }
            fun <T> indirectGeneric(): GenericInterface1<T> { TODO() }
        }

        val baseType = Mirror.reflectClass<MethodHolder>()
        val directMethod = baseType.declaredMethods.find { it.name == "directGeneric" }!!
        val indirectMethod = baseType.declaredMethods.find { it.name == "indirectGeneric" }!!
        val specializedDirectMethod = directMethod.specialize(Mirror.reflect<String>())
        val specializedIndirectMethod = indirectMethod.specialize(Mirror.reflect<String>())

        assertSame(Mirror.reflect<String>(), specializedDirectMethod.returnType)
        assertSame(Mirror.reflect<GenericInterface1<String>>(), specializedIndirectMethod.returnType)
    }

    @Test
    @DisplayName("Specializing a method should specialize its parameter types")
    fun specializeMethodParamsWithMethodGeneric() {
        class MethodHolder {
            fun <T> directGeneric(param1: T) { TODO() }
            fun <T> indirectGeneric(param1: GenericInterface1<T>) { TODO() }
        }

        val baseType = Mirror.reflectClass<MethodHolder>()
        val directMethod = baseType.declaredMethods.find { it.name == "directGeneric" }!!
        val indirectMethod = baseType.declaredMethods.find { it.name == "indirectGeneric" }!!
        val specializedDirectMethod = directMethod.specialize(Mirror.reflect<String>())
        val specializedIndirectMethod = indirectMethod.specialize(Mirror.reflect<String>())

        assertSame(Mirror.reflect<String>(), specializedDirectMethod.parameterTypes[0])
        assertSame(Mirror.reflect<GenericInterface1<String>>(), specializedIndirectMethod.parameterTypes[0])
    }

    @Test
    @DisplayName("Specializing a method should specialize its checked exception types")
    fun specializeMethodExceptionsWithMethodGeneric() {
        val baseType = Mirror.reflectClass<GenericCheckedExceptionMethodHolder<*>>()
        val method = baseType.declaredMethods.find { it.name == "genericMethodParameters" }!!
        val specializedMethod = method.specialize(Mirror.reflect<RuntimeException>())

        assertSame(Mirror.reflect<RuntimeException>(), specializedMethod.exceptionTypes[0])
    }
    //endregion

    //region Class & Method generics
    @Test
    @DisplayName("Specializing a class and its method should compound the specialization of the method's return types")
    fun specializeMethodReturnsWithClassAndMethodGeneric() {
        class MethodHolder<T> {
            fun <A> indirectGeneric(): GenericPairInterface1<T, A> { TODO() }
        }

        val baseType = Mirror.reflectClass<MethodHolder<*>>()
        val specializedType = baseType.specialize(Mirror.reflect<Object1>())
        val indirectMethod = specializedType.declaredMethods.find { it.name == "indirectGeneric" }!!
        val specializedIndirectMethod = indirectMethod.specialize(Mirror.reflect<String>())

        assertSame(Mirror.reflect<GenericPairInterface1<Object1, String>>(), specializedIndirectMethod.returnType)
    }

    @Test
    @DisplayName("Specializing a class and its method should compound the specialization of the method's parameter types")
    fun specializeMethodParamsWithClassAndMethodGeneric() {
        class MethodHolder<T> {
            fun <A> directGeneric(param1: T, param2: A) { TODO() }
            fun <A> indirectGeneric(param1: GenericPairInterface1<T, A>) { TODO() }
        }

        val baseType = Mirror.reflectClass<MethodHolder<*>>()
        val specializedType = baseType.specialize(Mirror.reflect<Object1>())
        val directMethod = specializedType.declaredMethods.find { it.name == "directGeneric" }!!
        val indirectMethod = specializedType.declaredMethods.find { it.name == "indirectGeneric" }!!
        val specializedDirectMethod = directMethod.specialize(Mirror.reflect<String>())
        val specializedIndirectMethod = indirectMethod.specialize(Mirror.reflect<String>())

        assertSameList(listOf(
            Mirror.reflect<Object1>(),
            Mirror.reflect<String>()
        ), specializedDirectMethod.parameterTypes)
        assertSame(Mirror.reflect<GenericPairInterface1<Object1, String>>(), specializedIndirectMethod.parameterTypes[0])
    }

    @Test
    @DisplayName("Specializing a class and its method should compound the specialization of the method's checked exception types")
    fun specializeMethodExceptionsWithClassAndMethodGeneric() {
        val baseType = Mirror.reflectClass<GenericCheckedExceptionMethodHolder<*>>()
        val specializedType = baseType.specialize(Mirror.reflect<NullPointerException>())
        val method = specializedType.declaredMethods.find { it.name == "genericClassAndMethodParameters" }!!
        val specializedMethod = method.specialize(Mirror.reflect<RuntimeException>())

        assertSameList(listOf(
            Mirror.reflect<NullPointerException>(),
            Mirror.reflect<RuntimeException>()
        ), specializedMethod.exceptionTypes)
    }
    //endregion
}