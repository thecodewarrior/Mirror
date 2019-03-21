@file:Suppress("ClassName", "unused")

package com.teamwizardry.mirror.testsupport

import com.teamwizardry.mirror.typeToken
import java.lang.reflect.Type

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


open class Object1
open class GenericObject1<T>
open class GenericPairObject1<A, B>

open class Object1Sub: Object1()
open class Object1Sub2: Object1()
open class GenericObject1Sub<T>: GenericObject1<T>()

open class Object2
open class GenericObject2<T>
open class GenericPairObject2<A, B>

open class Object2Sub: Object2()
open class Object2Sub2: Object2()
open class GenericObject2Sub<T>: GenericObject2<T>()

open class UpperBounded<out T>
open class LowerBounded<in T>

open class Exception1: Exception()
open class Exception2: Exception()

open class OuterClass1 {
    class OuterClass1_InnerStaticClass
    inner class OuterClass1_InnerClass
    inner class OuterClass1_InnerClass2

    fun getAnonymousClass(): Type {
        class OuterClass1_Method_AnonymousClass
        return OuterClass1_Method_AnonymousClass::class.java
    }

    fun getGenericAnonymousClass(): Type {
        class OuterClass1_Method_GenericAnonymousClass<I: Any> {
            lateinit var innerField: I
        }
        return OuterClass1_Method_GenericAnonymousClass::class.java
    }

    fun <T: Any> getSpecializedAnonymousClass(): Type {
        class OuterClass1_GenericMethod_SpecializedAnonymousClass<I: Any> {
            lateinit var innerField: I
            lateinit var outerField: T
        }
        return typeToken<OuterClass1_GenericMethod_SpecializedAnonymousClass<T>>()
    }
}

open class OuterGenericClass1<T: Any> {
    class OuterGenericClass1_InnerStaticClass
    inner class OuterGenericClass1_InnerClass {
        lateinit var innerField: T
        fun innerMethod(): T { nothing() }
        inner class OuterGenericClass1_InnerClass_InnerClass
    }
    inner class OuterGenericClass1_InnerGenericClass<I>

    fun getAnonymousClass(): Type {
        class OuterGenericClass1_Method_AnonymousClass {
            lateinit var innerField: T
        }
        return typeToken<OuterGenericClass1_Method_AnonymousClass>()
    }

    fun getGenericAnonymousClass(): Type {
        class OuterGenericClass1_Method_GenericAnonymousClass<I: Any> {
            lateinit var innerField: I
            lateinit var outerField: T
        }

        return typeToken<OuterGenericClass1_Method_GenericAnonymousClass<T>>()
    }

    fun <T: Any> getSpecializedAnonymousClass(): Type {
        class OuterGenericClass1_GenericMethod_SpecializedAnonymousClass<I: Any> {
            lateinit var innerField: I
            lateinit var outerField: T
        }
        return typeToken<OuterGenericClass1_GenericMethod_SpecializedAnonymousClass<T>>()
    }
}
