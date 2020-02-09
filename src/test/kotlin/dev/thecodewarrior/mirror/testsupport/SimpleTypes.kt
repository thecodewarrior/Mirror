@file:Suppress("ClassName", "unused")

package dev.thecodewarrior.mirror.testsupport

import dev.thecodewarrior.mirror.typeToken
import java.lang.reflect.Type

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
annotation class KotlinTypeAnnotation1

@Target(AnnotationTarget.TYPE)
annotation class AnnotationWithParameter(val foo: Int)

interface Interface1
interface Interface1Sub1: Interface1
interface Interface1Sub2: Interface1Sub1
interface GenericInterface1<T>
interface GenericPairInterface1<A, B>

interface Interface2
interface Interface2Sub1: Interface2
interface Interface2Sub2: Interface2Sub1
interface GenericInterface2<T>
interface GenericPairInterface2<A, B>

interface Interface12: Interface1, Interface2
interface Interface12Sub1: Interface1Sub1, Interface2Sub1
interface Interface12Sub2: Interface1Sub2, Interface2Sub2

interface NonEmptyInterface1 {
    fun interface1(): Object1
}
interface NonEmptyInterface1Override: NonEmptyInterface1 {
    override fun interface1(): Object1
}
interface NonEmptyInterface1Shadow: NonEmptyInterface1 {
    override fun interface1(): Object1Sub
}

internal class KotlinInternalClass
class KotlinInternalConstructor internal constructor() {
    internal fun internalMethod() {}
}

open class Object1
open class GenericObject1<T>
open class GenericPairObject1<A, B>

open class Object1Sub: Object1()
open class Object1SubSub: Object1Sub()
open class Object1Sub2: Object1()
open class GenericObject1Sub<T>: GenericObject1<T>()

open class Object2
open class GenericObject2<T>
open class GenericPairObject2<A, B>

open class Object2Sub: Object2()
open class Object2SubSub: Object2Sub()
open class Object2Sub2: Object2()
open class GenericObject2Sub<T>: GenericObject2<T>()

open class UpperBounded<out T>
open class LowerBounded<in T>

open class Exception1: Exception()
open class Exception2: Exception()

class ClosedObject1
data class DataObject1(val field: Boolean)

sealed class SealedClass {
    class SealedSub1: SealedClass()
    class SealedSub2: SealedClass()
    class SealedSub3: SealedClass()
}

class CompanionHolder {
    companion object
}

open class OuterClass1 {
    class InnerStaticClass
    inner class InnerClass
    inner class InnerClass2

    fun getAnonymousClass(): Type {
        class Method_AnonymousClass
        return Method_AnonymousClass::class.java
    }

    fun getGenericAnonymousClass(): Type {
        class Method_GenericAnonymousClass<I: Any> {
            lateinit var innerField: I
        }
        return Method_GenericAnonymousClass::class.java
    }

    fun <T: Any> getSpecializedAnonymousClass(): Type {
        class GenericMethod_SpecializedAnonymousClass<I: Any> {
            lateinit var innerField: I
            lateinit var outerField: T
        }
        return typeToken<GenericMethod_SpecializedAnonymousClass<T>>()
    }
}

open class OuterGenericClass1<T: Any> {
    class InnerStaticClass
    inner class InnerClass {
        lateinit var innerField: T
        fun innerMethod(): T { nothing() }
        inner class InnerClass
    }
    inner class InnerGenericClass<I>

    fun getAnonymousClass(): Type {
        class Method_AnonymousClass {
            lateinit var innerField: T
        }
        return typeToken<Method_AnonymousClass>()
    }

    fun getGenericAnonymousClass(): Type {
        class Method_GenericAnonymousClass<I: Any> {
            lateinit var innerField: I
            lateinit var outerField: T
        }

        return typeToken<Method_GenericAnonymousClass<T>>()
    }

    fun <T: Any> getSpecializedAnonymousClass(): Type {
        class GenericMethod_SpecializedAnonymousClass<I: Any> {
            lateinit var innerField: I
            lateinit var outerField: T
        }
        return typeToken<GenericMethod_SpecializedAnonymousClass<T>>()
    }
}

enum class EnumClass1 {
    NORMAL,
    ANONYMOUS {
        fun someFun() {}
    }
}
