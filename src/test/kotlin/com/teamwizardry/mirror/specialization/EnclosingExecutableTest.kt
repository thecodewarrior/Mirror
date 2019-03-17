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
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

class EnclosingExecutableTest: MirrorTestBase() {
    @Test
    @DisplayName("Getting the enclosing method of a root class should return null")
    fun enclosingExecutable_ofRootClass_shouldReturnNull() {
        val clazz = Mirror.reflectClass(OuterClass1::class.java)
        assertNull(clazz.enclosingExecutable)
    }

    @Test
    @DisplayName("Getting the enclosing executable of an inner class should return null")
    fun enclosingExecutable_ofInnerClass_shouldReturnNull() {
        val clazz = Mirror.reflectClass(OuterClass1.OuterClass1_InnerClass::class.java)
        assertNull(clazz.enclosingExecutable)
    }

    @Test
    @DisplayName("Getting the enclosing executable of a local class should return the enclosing method")
    fun enclosingExecutable_ofLocalClass_shouldReturnEnclosingMethod() {
        class LocalClass
        val clazz = Mirror.reflectClass(LocalClass::class.java)
        val thisMethod = ::enclosingExecutable_ofLocalClass_shouldReturnEnclosingMethod.javaMethod!!
        assertSame(Mirror.reflect(thisMethod), clazz.enclosingExecutable)
    }

    @Test
    @DisplayName("Getting the enclosing method of a local class in a constructor should return the enclosing constructor")
    fun enclosingExecutable_ofConstructorLocalClass_shouldReturnEnclosingConstructor() {
        class ConstructorHolder {
            init {
                class LocalClass
                val clazz = Mirror.reflectClass(LocalClass::class.java)
                val thisConstructor = ::ConstructorHolder.javaConstructor!!
                assertSame(Mirror.reflect(thisConstructor), clazz.enclosingExecutable)
            }
        }
        ConstructorHolder()
    }

    @Test
    @DisplayName("The type of local class members should correctly reference the enclosing method's type parameters by identity")
    fun <T: Any> localClassUsingMethodTypeParameter() {
        class LocalClass {
            lateinit var foo: T
        }
        val clazz = Mirror.reflectClass<LocalClass>()
        val thisMethod = Mirror.reflectClass<EnclosingExecutableTest>().methods("localClassUsingMethodTypeParameter")[0]
        assertSame(thisMethod.typeParameters[0], clazz.field("foo")?.type)
    }

    @Test
    @DisplayName("The type of local class members should be specialized when specializing a local class in a generic method")
    fun <T: Any> localClassUsingSpecializedMethodTypeParameter() {
        class LocalClass {
            lateinit var foo: T
        }
        val clazz = Mirror.reflectClass<LocalClass>()
        val thisMethod = Mirror.reflectClass<EnclosingExecutableTest>().methods("localClassUsingSpecializedMethodTypeParameter")[0]
        assertSame(Mirror.reflect<String>(), clazz.specializeEnclosingExecutable(thisMethod.specialize(Mirror.reflect<String>())).field("foo")?.type)
    }

    @Test
    @DisplayName("Specializing a class with the wrong enclosing executable should throw")
    fun specializingWithWrongExecutable() {
        class LocalClass
        val clazz = Mirror.reflectClass<LocalClass>()
        val thisMethod = Mirror.reflectClass<EnclosingExecutableTest>().methods("toString")[0]
        assertThrows<InvalidSpecializationException> {
            clazz.specializeEnclosingExecutable(thisMethod)
        }
    }

    @Test
    @DisplayName("Specializing a root class for an enclosing executable should throw")
    fun specializingRootClass() {
        val clazz = Mirror.reflectClass<Object1>()
        val thisMethod = Mirror.reflectClass<EnclosingExecutableTest>().methods("toString")[0]
        assertThrows<InvalidSpecializationException> {
            clazz.specializeEnclosingExecutable(thisMethod)
        }
    }

    @Test
    @DisplayName("Specializing a root class for an enclosing executable should throw")
    fun specializingRootClassForNull() {
        val clazz = Mirror.reflectClass<Object1>()
        assertSame(clazz, clazz.specializeEnclosingExecutable(null))
    }

    @Test
    @DisplayName("Specializing a local class for a executable then for null should retain the enclosing class specialization")
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
        val innerClazz = innerJavaClazz.specializeEnclosingExecutable(middle)
        val innerDespecialized = innerClazz.specializeEnclosingExecutable(null)
        assertSame(outer, innerDespecialized.enclosingClass)
    }
}