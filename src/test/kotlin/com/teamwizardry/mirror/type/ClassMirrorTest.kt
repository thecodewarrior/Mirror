package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.GenericPairObject1
import com.teamwizardry.mirror.testsupport.Interface1
import com.teamwizardry.mirror.testsupport.Interface2
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.Object1
import com.teamwizardry.mirror.testsupport.OuterClass1
import com.teamwizardry.mirror.testsupport.OuterGenericClass1
import com.teamwizardry.mirror.testsupport.assertSameList
import com.teamwizardry.mirror.testsupport.assertSameSet
import com.teamwizardry.mirror.typeParameter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class ClassMirrorTest: MirrorTestBase() {

    @Test
    @DisplayName("Getting the raw class of a ClassMirror should return the original class")
    fun getRawClass_ofType_shouldReturnOriginalClass() {
        val type = Mirror.reflectClass<Object1>()
        assertEquals(Object1::class.java, type.java)
    }

    @Test
    @DisplayName("Getting the superclass of a ClassMirror should return the correct supertype")
    fun getSuperclass_ofType_shouldReturnSupertype() {
        class Object1Subclass: Object1()
        val type = Mirror.reflectClass<Object1Subclass>().superclass
        assertEquals(Mirror.reflect<Object1>(), type)
    }

    @Test
    @DisplayName("Getting the superclass of Object should return null")
    fun getSuperclass_ofObject_shouldReturnNull() {
        val type = Mirror.reflectClass<Any>().superclass
        assertEquals(null, type)
    }

    @Test
    @DisplayName("Getting the superclass an interface should return null")
    fun getSuperclass_ofInterface_shouldReturnNull() {
        val type = Mirror.reflectClass<Interface1>().superclass
        assertEquals(null, type)
    }

    @Test
    @DisplayName("Getting the interfaces of a type without interfaces should return an empty list")
    fun getInterfaces_ofTypeWithoutInterfaces_shouldReturnEmptyList() {
        val type = Mirror.reflectClass<Object1>().interfaces
        assertEquals(emptyList<TypeMirror>(), type)
    }

    @Test
    @DisplayName("Getting the interfaces of a type with interfaces should return its interfaces in declaration order")
    fun getInterfaces_ofTypeWithInterfaces_shouldReturnInterfacesInOrder() {
        class TestType: Interface2, Interface1
        val type = Mirror.reflectClass<TestType>().interfaces
        assertSameList(listOf(
                Mirror.reflect<Interface2>(),
                Mirror.reflect<Interface1>()
        ), type)
    }

    @Test
    @DisplayName("Getting the type parameters of a nongeneric type should return an empty list")
    fun getTypeParameters_ofNonGenericType_shouldReturnEmptyList() {
        val type = Mirror.reflectClass<Object1>().typeParameters
        assertEquals(emptyList<TypeMirror>(), type)
    }

    @Test
    @DisplayName("Getting the type parameters of a generic type should return the correct types")
    fun getTypeParameters_ofGenericType_shouldReturnTypes() {
        val type = Mirror.reflectClass(GenericPairObject1::class.java).typeParameters
        assertSameList(listOf(
                Mirror.reflect(GenericPairObject1::class.java.typeParameter(0)!!),
                Mirror.reflect(GenericPairObject1::class.java.typeParameter(1)!!)
        ), type)
    }

    @Test
    @DisplayName("Getting the raw type of a raw ClassMirror should return itself")
    fun getRaw_ofType_returnsItself() {
        val type = Mirror.reflectClass<Object1>()
        assertSame(type, type.raw)
    }

    @Test
    @DisplayName("Getting the enclosing class of a ClassMirror not nested within another should return null")
    fun enclosingClass_ofNonNestedClass_shouldReturnNull() {
        val type = Mirror.reflectClass<Object1>()
        assertNull(type.enclosingClass)
    }

    @Test
    @DisplayName("Getting the enclosing class of a ClassMirror statically nested within another should return the outer class")
    fun enclosingClass_ofStaticNestedClass_shouldReturnClassMirror() {
        val type = Mirror.reflectClass<OuterClass1.OuterClass1_InnerStaticClass>()
        assertSame(Mirror.reflectClass<OuterClass1>(), type.enclosingClass)
    }

    @Test
    @DisplayName("Getting the enclosing class of a ClassMirror nested within another should return the outer class")
    fun enclosingClass_ofNestedClass_shouldReturnClassMirror() {
        val type = Mirror.reflectClass<OuterClass1.OuterClass1_InnerClass>()
        assertSame(Mirror.reflectClass<OuterClass1>(), type.enclosingClass)
    }

    @Test
    @DisplayName("Field types of inner classes that use outer generics should have outer generic types")
    fun fieldType_ofInnerClassFieldWithOuterGenericType_shouldReturnOuterGenericType() {
        val innerClass = Mirror.reflectClass(OuterGenericClass1.OuterGenericClass1_InnerClass::class.java)
        val fieldType = innerClass.field("innerField")?.type
        assertSame(Mirror.reflectClass(OuterGenericClass1::class.java).typeParameters[0], fieldType)
    }

    @Test
    @DisplayName("Method types of inner classes that use outer generics should have outer generic types")
    fun returnType_ofMethodInNestedClassWithOuterGenericType_shouldReturnOuterGenericType() {
        val innerClass = Mirror.reflectClass(OuterGenericClass1.OuterGenericClass1_InnerClass::class.java)
        val methodType = innerClass.methods("innerMethod")[0].returnType
        assertSame(Mirror.reflectClass(OuterGenericClass1::class.java).typeParameters[0], methodType)
    }

    @Test
    @DisplayName("Getting declared fields of a class should return the correct mirrors in order")
    fun getFields() {
        class FieldHolder(
            @JvmField var foo: String,
            @JvmField var bar: String
        )
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