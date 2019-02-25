package com.teamwizardry.mirror.specialization

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.Object1
import com.teamwizardry.mirror.testsupport.Object2
import com.teamwizardry.mirror.testsupport.OuterClass1
import com.teamwizardry.mirror.type.ClassMirror
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.jvm.javaMethod

class EnclosingMethodTest: MirrorTestBase() {
    @Test
    @DisplayName("Getting the enclosing method of a root class should return null")
    fun enclosingMethod_ofRootClass_shouldReturnNull() {
        val clazz = Mirror.reflectClass(OuterClass1::class.java)
        assertNull(clazz.enclosingMethod)
    }

    @Test
    @DisplayName("Getting the enclosing method of an inner class should return null")
    fun enclosingMethod_ofInnerClass_shouldReturnNull() {
        val clazz = Mirror.reflectClass(OuterClass1.OuterClass1_InnerClass::class.java)
        assertNull(clazz.enclosingMethod)
    }

    @Test
    @DisplayName("Getting the enclosing method of a local class should return the enclosing method")
    fun enclosingMethod_ofLocalClass_shouldReturnEnclosingMethod() {
        class LocalClass
        val clazz = Mirror.reflectClass(LocalClass::class.java)
        val thisMethod = ::enclosingMethod_ofLocalClass_shouldReturnEnclosingMethod.javaMethod!!
        assertSame(Mirror.reflect(thisMethod), clazz.enclosingMethod)
    }

    @Test
    @DisplayName("The type of local class members should correctly reference the enclosing method's type parameters by identity")
    fun <T: Any> localClassUsingMethodTypeParameter() {
        class LocalClass {
            lateinit var foo: T
        }
        val clazz = Mirror.reflectClass<LocalClass>()
        val thisMethod = Mirror.reflectClass<EnclosingMethodTest>().methods("localClassUsingMethodTypeParameter")[0]
        assertSame(thisMethod.typeParameters[0], clazz.field("foo")?.type)
    }

    @Test
    @DisplayName("The type of local class members should be specialized when specializing a local class in a generic method")
    fun <T: Any> localClassUsingSpecializedMethodTypeParameter() {
        class LocalClass {
            lateinit var foo: T
        }
        val clazz = Mirror.reflectClass<LocalClass>()
        val thisMethod = Mirror.reflectClass<EnclosingMethodTest>().methods("localClassUsingSpecializedMethodTypeParameter")[0]
        assertSame(Mirror.reflect<String>(), clazz.specializeEnclosingMethod(thisMethod.specialize(Mirror.reflect<String>())).field("foo")?.type)
    }

    @Test
    @DisplayName("Specializing a class with the wrong enclosing method should throw")
    fun specializingWithWrongMethod() {
        class LocalClass
        val clazz = Mirror.reflectClass<LocalClass>()
        val thisMethod = Mirror.reflectClass<EnclosingMethodTest>().methods("toString")[0]
        assertThrows<InvalidSpecializationException> {
            clazz.specializeEnclosingMethod(thisMethod)
        }
    }

    @Test
    @DisplayName("Specializing a root class for an enclosing method should throw")
    fun specializingRootClass() {
        val clazz = Mirror.reflectClass<Object1>()
        val thisMethod = Mirror.reflectClass<EnclosingMethodTest>().methods("toString")[0]
        assertThrows<InvalidSpecializationException> {
            clazz.specializeEnclosingMethod(thisMethod)
        }
    }

    @Test
    @DisplayName("Specializing a root class for an enclosing method should throw")
    fun specializingRootClassForNull() {
        val clazz = Mirror.reflectClass<Object1>()
        assertSame(clazz, clazz.specializeEnclosingMethod(null))
    }

    @Test
    @DisplayName("Specializing a local class for a method then for null should retain the enclosing class specialization")
    fun specializingLocalClassForMethodThenNull() {
        class OuterType<O> {
            fun <M> middleMethod(): ClassMirror {
                class InnerType
                return Mirror.reflectClass<InnerType>()
            }
        }
        val outer = Mirror.reflectClass<OuterType<Object1>>()
        val middle = outer.methods("middleMethod")[0].specialize(Mirror.reflect<Object2>())
        val innerJavaClazz = OuterType<Any>().middleMethod<Any>()
        val innerClazz = innerJavaClazz.specializeEnclosingMethod(middle)
        val innerDespecialized = innerClazz.specializeEnclosingMethod(null)
        assertSame(outer, innerDespecialized.enclosingClass)
    }
}