package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.*
import dev.thecodewarrior.mirror.type.TypeMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class ClassMirrorTest: MTest() {

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
                Mirror.reflect(GenericPairObject1::class.java.typeParameters[0]),
                Mirror.reflect(GenericPairObject1::class.java.typeParameters[1])
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
        val type = Mirror.reflectClass<OuterClass1.InnerStaticClass>()
        assertSame(Mirror.reflectClass<OuterClass1>(), type.enclosingClass)
    }

    @Test
    @DisplayName("Getting the enclosing class of a ClassMirror nested within another should return the outer class")
    fun enclosingClass_ofNestedClass_shouldReturnClassMirror() {
        val type = Mirror.reflectClass<OuterClass1.InnerClass>()
        assertSame(Mirror.reflectClass<OuterClass1>(), type.enclosingClass)
    }

    @Test
    @DisplayName("Field types of inner classes that use outer generics should have outer generic types")
    fun fieldType_ofInnerClassFieldWithOuterGenericType_shouldReturnOuterGenericType() {
        val innerClass = Mirror.reflectClass(OuterGenericClass1.InnerClass::class.java)
        val fieldType = innerClass.findPublicField("innerField")?.type
        assertSame(Mirror.reflectClass(OuterGenericClass1::class.java).typeParameters[0], fieldType)
    }

    @Test
    @DisplayName("Method types of inner classes that use outer generics should have outer generic types")
    fun returnType_ofMethodInNestedClassWithOuterGenericType_shouldReturnOuterGenericType() {
        val innerClass = Mirror.reflectClass(OuterGenericClass1.InnerClass::class.java)
        val methodType = innerClass.getMethod("innerMethod").returnType
        assertSame(Mirror.reflectClass(OuterGenericClass1::class.java).typeParameters[0], methodType)
    }

    @Test
    @DisplayName("Getting declared fields of a class should return the correct mirrors")
    fun getDeclaredFields() {
        class FieldHolder(
            @JvmField var foo: String,
            @JvmField private var bar: String
        )
        val fooJvmField = FieldHolder::class.java.getDeclaredField("foo")
        val barJvmField = FieldHolder::class.java.getDeclaredField("bar")
        val holderMirror = Mirror.reflectClass<FieldHolder>()
        val fields = holderMirror.declaredFields
        assertSameSet(listOf(
            Mirror.reflect(fooJvmField),
            Mirror.reflect(barJvmField)
        ), fields)
    }

    @Test
    @DisplayName("Getting declared methods of a class should return the correct mirrors")
    fun getDeclaredMethods() {
        class MethodHolder {
            fun foo() {}
            private fun bar() {}
        }
        val fooJvmMethod = MethodHolder::class.java.getDeclaredMethod("foo")
        val barJvmMethod = MethodHolder::class.java.getDeclaredMethod("bar")
        val holderMirror = Mirror.reflectClass<MethodHolder>()
        val methods = holderMirror.declaredMethods
        assertSameSet(listOf(
            Mirror.reflect(fooJvmMethod),
            Mirror.reflect(barJvmMethod)
        ), methods)
    }

    @Test
    @DisplayName("Getting declared constructors of a class should return the correct mirrors")
    fun getDeclaredConstructors() {
        class ConstructorHolder {
            constructor(foo: String)
            private constructor(bar: Int)
        }
        val fooJvmConstructor = ConstructorHolder::class.java.getDeclaredConstructor(String::class.java)
        val barJvmConstructor = ConstructorHolder::class.java.getDeclaredConstructor(Int::class.javaPrimitiveType)
        val holderMirror = Mirror.reflectClass<ConstructorHolder>()
        val constructors = holderMirror.declaredConstructors
        assertSameSet(listOf(
            Mirror.reflect(fooJvmConstructor),
            Mirror.reflect(barJvmConstructor)
        ), constructors)
    }

    @Test
    fun kClass_ofRawClass_shouldReturnKClass() {
        assertEquals(Object1::class, Mirror.reflectClass<Object1>().kClass)
    }

    @Test
    fun kClass_ofSpecializedClass_shouldReturnKClass() {
        assertEquals(GenericObject1::class, Mirror.reflectClass<GenericObject1<Object1>>().kClass)
    }
}
