package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.Interface1
import com.teamwizardry.mirror.testsupport.Interface2
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.Object1
import com.teamwizardry.mirror.testsupport.assertSameList
import com.teamwizardry.mirror.testsupport.assertSameSet
import com.teamwizardry.mirror.typeParameter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class ClassMirrorTest: MirrorTestBase() {
    @Test
    fun getRawClass_ofType_shouldReturnOriginalClass() {
        val type = Mirror.reflectClass<Object1>()
        assertEquals(Object1::class.java, type.java)
    }

    @Test
    fun getSuperclass_ofType_shouldReturnSupertype() {
        val type = Mirror.reflectClass<Object1>().superclass
        assertEquals(Mirror.reflect<Any>(), type)
    }

    @Test
    fun getSuperclass_ofObject_shouldReturnNull() {
        val type = Mirror.reflectClass<Any>().superclass
        assertEquals(null, type)
    }

    @Test
    fun getSuperclass_ofInterface_shouldReturnNull() {
        val type = Mirror.reflectClass<Interface1>().superclass
        assertEquals(null, type)
    }

    @Test
    fun getInterfaces_ofTypeWithoutInterfaces_shouldReturnEmptyList() {
        val type = Mirror.reflectClass<Object1>().interfaces
        assertEquals(emptyList<TypeMirror>(), type)
    }

    @Test
    fun getInterfaces_ofTypeWithInterfaces_shouldReturnInterfacesInOrder() {
        class TestType: Interface2, Interface1
        val type = Mirror.reflectClass<TestType>().interfaces
        assertSameList(listOf(
                Mirror.reflect<Interface2>(),
                Mirror.reflect<Interface1>()
        ), type)
    }

    @Test
    fun getTypeParameters_ofNonGenericType_shouldReturnEmptyList() {
        val type = Mirror.reflectClass<Object1>().typeParameters
        assertEquals(emptyList<TypeMirror>(), type)
    }

    @Test
    fun getTypeParameters_ofGenericType_shouldReturnTypes() {
        class GenericType<A, B>
        val type = Mirror.reflectClass(GenericType::class.java).typeParameters
        assertSameList(listOf(
                Mirror.reflect(GenericType::class.java.typeParameter(0)!!),
                Mirror.reflect(GenericType::class.java.typeParameter(1)!!)
        ), type)
    }

    @Test
    fun getRaw_ofType_returnsItself() {
        val type = Mirror.reflectClass<Object1>()
        assertSame(type, type.raw)
    }

    @Test
    @DisplayName("Getting declared fields of a class should return the correct mirrors in order")
    fun getFields() {
        class FieldHolder {
            @JvmField var foo: String? = null
            @JvmField var bar: String? = null
        }
        val fooJvmField = FieldHolder::class.java.getField("foo")
        val barJvmField = FieldHolder::class.java.getField("bar")
        val holderMirror = Mirror.reflectClass<FieldHolder>()
        val fields = holderMirror.declaredFields
        assertSameList(listOf(
            Mirror.reflect(fooJvmField),
            Mirror.reflect(barJvmField)
        ), fields)
    }

    @Test
    @DisplayName("Getting declared methods of a class should return the correct mirrors in order")
    fun getMethods() {
        class MethodHolder {
            fun foo() {}
            fun bar() {}
        }
        val fooJvmMethod = MethodHolder::class.java.getMethod("foo")
        val barJvmMethod = MethodHolder::class.java.getMethod("bar")
        val holderMirror = Mirror.reflectClass<MethodHolder>()
        val fields = holderMirror.declaredMethods
        assertSameSet(listOf(
            Mirror.reflect(fooJvmMethod),
            Mirror.reflect(barJvmMethod)
        ), fields)
    }
}