package com.teamwizardry.mirror.reflection.type

import com.teamwizardry.librarianlib.commons.reflection.typeParameter
import com.teamwizardry.mirror.reflection.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ClassMirrorTest: MirrorTestBase() {

    @Test
    fun getRawClass_ofType_shouldReturnOriginalClass() {
        val type = Mirror.reflectClass<Object1>()
        assertEquals(Object1::class.java, type.rawType)
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
        assertEquals(listOf(
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
        assertEquals(listOf(
                Mirror.reflect(GenericType::class.java.typeParameter(0)!!),
                Mirror.reflect(GenericType::class.java.typeParameter(1)!!)
        ), type)
    }

    @Test
    fun getRaw_ofType_returnsItself() {
        val type = Mirror.reflectClass<Object1>()
        assertEquals(type, type.raw)
    }
}