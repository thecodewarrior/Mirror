package dev.thecodewarrior.mirror.specialization

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.GenericCheckedExceptionMethodHolder
import dev.thecodewarrior.mirror.testsupport.GenericInterface1
import dev.thecodewarrior.mirror.testsupport.GenericPairInterface1
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.assertSameList
import dev.thecodewarrior.mirror.testsupport.nothing
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class ExecutableSpecializationTest: MirrorTestBase() {
    //region Class generic
    @Test
    @DisplayName("Specializing a class should specialize the return types of each of its declared methods")
    fun specializeMethodReturnsWithClassGeneric() {
        class MethodHolder<T> {
            fun directGeneric(): T { null!! }
            fun indirectGeneric(): GenericInterface1<T> { null!! }
        }

        val baseType = Mirror.reflectClass<MethodHolder<*>>()
        val specializedType = baseType.withTypeArguments(Mirror.reflect<String>())
        val specializedDirectMethod = specializedType.declaredMethods.find { it.name == "directGeneric" }!!
        val specializedIndirectMethod = specializedType.declaredMethods.find { it.name == "indirectGeneric" }!!

        assertSame(Mirror.reflect<String>(), specializedDirectMethod.returnType)
        assertSame(Mirror.reflect<GenericInterface1<String>>(), specializedIndirectMethod.returnType)
    }

    @Test
    @DisplayName("Specializing a class should specialize the parameter types of each of its declared methods")
    fun specializeMethodParamsWithClassGeneric() {
        class MethodHolder<T> {
            fun directGeneric(param1: T) { null!! }
            fun indirectGeneric(param1: GenericInterface1<T>) { null!! }
        }

        val baseType = Mirror.reflectClass<MethodHolder<*>>()
        val specializedType = baseType.withTypeArguments(Mirror.reflect<String>())
        val specializedDirectMethod = specializedType.declaredMethods.find { it.name == "directGeneric" }!!
        val specializedIndirectMethod = specializedType.declaredMethods.find { it.name == "indirectGeneric" }!!

        assertSame(Mirror.reflect<String>(), specializedDirectMethod.parameterTypes[0])
        assertSame(Mirror.reflect<GenericInterface1<String>>(), specializedIndirectMethod.parameterTypes[0])
    }

    @Test
    @DisplayName("Specializing a class should specialize the checked exception types of each of its declared methods")
    fun specializeMethodExceptionsWithClassGeneric() {
        val baseType = Mirror.reflectClass<GenericCheckedExceptionMethodHolder<*>>()
        val specializedType = baseType.withTypeArguments(Mirror.reflect<RuntimeException>())
        val specializedMethod = specializedType.declaredMethods.find { it.name == "generic" }!!

        assertSame(Mirror.reflect<RuntimeException>(), specializedMethod.exceptionTypes[0])
    }

    @Test
    fun enclose_withSpecializedType_shouldReturnSpecializedMethod() {
        class MethodHolder<T> {
            fun method(): T { nothing() }
        }

        val baseType = Mirror.reflectClass(MethodHolder::class.java)
        val method = baseType.declaredMethods.find { it.name == "method" }!!
        val enclosedMethod = method.withDeclaringClass(Mirror.reflectClass<MethodHolder<Object1>>())

        assertSame(baseType, method.declaringClass)
        assertSame(Mirror.reflect<MethodHolder<Object1>>(), enclosedMethod.declaringClass)
        assertSame(Mirror.reflect<Object1>(), enclosedMethod.returnType)
    }

    @Test
    fun enclose_withRawType_shouldReturnUnspecializedMethod() {
        class MethodHolder<T> {
            fun method(): T { nothing() }
        }

        val baseType = Mirror.reflectClass(MethodHolder::class.java)
        val method = Mirror.reflectClass<MethodHolder<Object1>>().declaredMethods.find { it.name == "method" }!!
        val enclosedMethod = method.withDeclaringClass(baseType)

        assertSame(method.raw, enclosedMethod)
    }
    //endregion

    //region Method generic
    @Test
    @DisplayName("Specializing a method should specialize its return type")
    fun specializeMethodReturnsWithMethodGeneric() {
        class MethodHolder {
            fun <T> directGeneric(): T { null!! }
            fun <T> indirectGeneric(): GenericInterface1<T> { null!! }
        }

        val baseType = Mirror.reflectClass<MethodHolder>()
        val directMethod = baseType.declaredMethods.find { it.name == "directGeneric" }!!
        val indirectMethod = baseType.declaredMethods.find { it.name == "indirectGeneric" }!!
        val specializedDirectMethod = directMethod.withTypeParameters(Mirror.reflect<String>())
        val specializedIndirectMethod = indirectMethod.withTypeParameters(Mirror.reflect<String>())

        assertSame(Mirror.reflect<String>(), specializedDirectMethod.returnType)
        assertSame(Mirror.reflect<GenericInterface1<String>>(), specializedIndirectMethod.returnType)
    }

    @Test
    @DisplayName("Specializing a method should specialize its parameter types")
    fun specializeMethodParamsWithMethodGeneric() {
        class MethodHolder {
            fun <T> directGeneric(param1: T) { null!! }
            fun <T> indirectGeneric(param1: GenericInterface1<T>) { null!! }
        }

        val baseType = Mirror.reflectClass<MethodHolder>()
        val directMethod = baseType.declaredMethods.find { it.name == "directGeneric" }!!
        val indirectMethod = baseType.declaredMethods.find { it.name == "indirectGeneric" }!!
        val specializedDirectMethod = directMethod.withTypeParameters(Mirror.reflect<String>())
        val specializedIndirectMethod = indirectMethod.withTypeParameters(Mirror.reflect<String>())

        assertSame(Mirror.reflect<String>(), specializedDirectMethod.parameterTypes[0])
        assertSame(Mirror.reflect<GenericInterface1<String>>(), specializedIndirectMethod.parameterTypes[0])
    }

    @Test
    @DisplayName("Specializing a method should specialize its checked exception types")
    fun specializeMethodExceptionsWithMethodGeneric() {
        val baseType = Mirror.reflectClass<GenericCheckedExceptionMethodHolder<*>>()
        val method = baseType.declaredMethods.find { it.name == "genericMethodParameters" }!!
        val specializedMethod = method.withTypeParameters(Mirror.reflect<RuntimeException>())

        assertSame(Mirror.reflect<RuntimeException>(), specializedMethod.exceptionTypes[0])
    }

    @Test
    fun specialized_withRawTypes_shouldReturnUnspecializedMethod() {
        class MethodHolder {
            fun <T> method(): T { nothing() }
        }

        val method = Mirror.reflectClass<MethodHolder>().declaredMethods.find { it.name == "method" }!!
        val specialized = method.withTypeParameters(Mirror.reflect<String>())
        val despecialized = specialized.withTypeParameters(*method.typeParameters.toTypedArray())

        assertSame(method.raw, despecialized)
    }

    @Test
    fun specialized_withZeroTypes_shouldReturnUnspecializedMethod() {
        class MethodHolder {
            fun <T> method(): T { nothing() }
        }

        val method = Mirror.reflectClass<MethodHolder>().declaredMethods.find { it.name == "method" }!!
        val specialized = method.withTypeParameters(Mirror.reflect<String>())
        val despecialized = specialized.withTypeParameters()

        assertSame(method.raw, despecialized)
    }
    //endregion

    //region Class & Method generics
    @Test
    @DisplayName("Specializing a class and its method should compound the specialization of the method's return types")
    fun specializeMethodReturnsWithClassAndMethodGeneric() {
        class MethodHolder<T> {
            fun <A> indirectGeneric(): GenericPairInterface1<T, A> { null!! }
        }

        val baseType = Mirror.reflectClass<MethodHolder<*>>()
        val specializedType = baseType.withTypeArguments(Mirror.reflect<Object1>())
        val indirectMethod = specializedType.declaredMethods.find { it.name == "indirectGeneric" }!!
        val specializedIndirectMethod = indirectMethod.withTypeParameters(Mirror.reflect<String>())

        assertSame(Mirror.reflect<GenericPairInterface1<Object1, String>>(), specializedIndirectMethod.returnType)
    }

    @Test
    @DisplayName("Specializing a class and its method should compound the specialization of the method's parameter types")
    fun specializeMethodParamsWithClassAndMethodGeneric() {
        class MethodHolder<T> {
            fun <A> directGeneric(param1: T, param2: A) { null!! }
            fun <A> indirectGeneric(param1: GenericPairInterface1<T, A>) { null!! }
        }

        val baseType = Mirror.reflectClass<MethodHolder<*>>()
        val specializedType = baseType.withTypeArguments(Mirror.reflect<Object1>())
        val directMethod = specializedType.declaredMethods.find { it.name == "directGeneric" }!!
        val indirectMethod = specializedType.declaredMethods.find { it.name == "indirectGeneric" }!!
        val specializedDirectMethod = directMethod.withTypeParameters(Mirror.reflect<String>())
        val specializedIndirectMethod = indirectMethod.withTypeParameters(Mirror.reflect<String>())

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
        val specializedType = baseType.withTypeArguments(Mirror.reflect<NullPointerException>())
        val method = specializedType.declaredMethods.find { it.name == "genericClassAndMethodParameters" }!!
        val specializedMethod = method.withTypeParameters(Mirror.reflect<RuntimeException>())

        assertSameList(listOf(
            Mirror.reflect<NullPointerException>(),
            Mirror.reflect<RuntimeException>()
        ), specializedMethod.exceptionTypes)
    }
    //endregion
}