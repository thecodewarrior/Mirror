package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.member.Modifier
import com.teamwizardry.mirror.testsupport.ClosedObject1
import com.teamwizardry.mirror.testsupport.CompanionHolder
import com.teamwizardry.mirror.testsupport.DataObject1
import com.teamwizardry.mirror.testsupport.EnumClass1
import com.teamwizardry.mirror.testsupport.GenericObject1
import com.teamwizardry.mirror.testsupport.GenericPairObject1
import com.teamwizardry.mirror.testsupport.Interface1
import com.teamwizardry.mirror.testsupport.Interface2
import com.teamwizardry.mirror.testsupport.KotlinInternalClass
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.Object1
import com.teamwizardry.mirror.testsupport.OuterClass1
import com.teamwizardry.mirror.testsupport.OuterGenericClass1
import com.teamwizardry.mirror.testsupport.SealedClass
import com.teamwizardry.mirror.testsupport.assertSameList
import com.teamwizardry.mirror.testsupport.assertSameSet
import com.teamwizardry.mirror.testsupport.simpletypes.JObject1
import com.teamwizardry.mirror.type.ClassMirror.Flag
import com.teamwizardry.mirror.typeholders.ClassMirrorHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class ClassMirrorTest: MirrorTestBase() {
    private val holder = ClassMirrorHolder()

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
        val fieldType = innerClass.field("innerField")?.type
        assertSame(Mirror.reflectClass(OuterGenericClass1::class.java).typeParameters[0], fieldType)
    }

    @Test
    @DisplayName("Method types of inner classes that use outer generics should have outer generic types")
    fun returnType_ofMethodInNestedClassWithOuterGenericType_shouldReturnOuterGenericType() {
        val innerClass = Mirror.reflectClass(OuterGenericClass1.InnerClass::class.java)
        val methodType = innerClass.methods("innerMethod")[0].returnType
        assertSame(Mirror.reflectClass(OuterGenericClass1::class.java).typeParameters[0], methodType)
    }

    @Test
    @DisplayName("Getting declared fields of a class should return the correct mirrors")
    fun getFields() {
        class FieldHolder(
            @JvmField var foo: String,
            @JvmField private var bar: String
        )
        val fooJvmField = FieldHolder::class.java.getDeclaredField("foo")
        val barJvmField = FieldHolder::class.java.getDeclaredField("bar")
        val holderMirror = Mirror.reflectClass<FieldHolder>()
        val fields = holderMirror.declaredFields
        assertSameList(listOf(
            Mirror.reflect(fooJvmField),
            Mirror.reflect(barJvmField)
        ), fields)
    }

    @Test
    @DisplayName("Getting declared methods of a class should return the correct mirrors")
    fun getMethods() {
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
    fun getConstructors() {
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

    @Test
    fun access_ofJavaClass_shouldBeCorrect() {
        assertEquals(Modifier.Access.PUBLIC, Mirror.reflectClass(holder.getClass("public class")).access)
        assertEquals(Modifier.Access.DEFAULT, Mirror.reflectClass(holder.getClass("default class")).access)
        assertEquals(Modifier.Access.PROTECTED, Mirror.reflectClass(holder.getClass("protected class")).access)
        assertEquals(Modifier.Access.PRIVATE, Mirror.reflectClass(holder.getClass("private class")).access)
    }

    @Test
    fun access_ofKotlinInternalClass_shouldBePublicAndInternal() {
        assertEquals(Modifier.Access.PUBLIC, Mirror.reflectClass<KotlinInternalClass>().access)
        assertTrue(Mirror.reflectClass<KotlinInternalClass>().isInternalAccess)
    }

    @Test
    fun modifiers_ofJavaClass_shouldBeCorrect() {
        fun test(name: String, vararg mods: Modifier) = assertEquals(setOf(*mods), Mirror.reflectClass(holder.getClass(name)).modifiers)
        test("public class", Modifier.PUBLIC)
        test("default class")
        test("protected class", Modifier.PROTECTED)
        test("private class", Modifier.PRIVATE)
        test("abstract class", Modifier.ABSTRACT)
        test("static class", Modifier.STATIC)
        test("final class", Modifier.FINAL)
        // test("strictfp class", Modifier.STRICT) // TODO Strictfp class fix
    }

    private inline fun <reified T> testFlags(vararg flags: Flag) {
        assertEquals(setOf(*flags), Mirror.reflectClass<T>().flags)
    }

    private fun testFlags(name: String, vararg flags: Flag) {
        assertEquals(setOf(*flags), Mirror.reflectClass(holder.getClass(name)).flags)
    }

    @Test
    fun kotlinFlags_ofKotlinClass_shouldBeCorrect() {
        assertAll(
            { testFlags<ClosedObject1>(Flag.FINAL) },
            { testFlags<Object1>() },
            { testFlags<CompanionHolder.Companion>(Flag.FINAL, Flag.STATIC, Flag.COMPANION, Flag.MEMBER) },
            { testFlags<DataObject1>(Flag.FINAL, Flag.DATA) },
            { testFlags<SealedClass>(Flag.ABSTRACT, Flag.SEALED) },
            { testFlags<Interface1>(Flag.INTERFACE, Flag.ABSTRACT) }
        )
    }

    @Test
    fun flags_ofClasses_shouldBeCorrect() {
        assertAll(
            { testFlags("public class", Flag.MEMBER) },
            { testFlags("default class", Flag.MEMBER) },
            { testFlags("protected class", Flag.MEMBER) },
            { testFlags("private class", Flag.MEMBER) },
            { testFlags("abstract class", Flag.MEMBER, Flag.ABSTRACT) },
            { testFlags("static class", Flag.MEMBER, Flag.STATIC) },
            { testFlags("final class", Flag.MEMBER, Flag.FINAL) },
            // TODO Strictfp flag missing from java modifiers
            // { testFlags("strictfp class", Flag.MEMBER, Flag.STRICT) },
            { testFlags("annotation class", Flag.MEMBER, Flag.INTERFACE, Flag.ABSTRACT, Flag.ANNOTATION, Flag.STATIC) },
            { testFlags("interface", Flag.MEMBER, Flag.STATIC, Flag.INTERFACE, Flag.ABSTRACT) },
            // TODO java anonymous KClass error
            // `kotlin.reflect.jvm.internal.KotlinReflectionInternalError: Unresolved class: class com.teamwizardry.mirror.typeholders.ClassMirrorHolder$1`
            // { assertEquals(setOf(Flag.ANONYMOUS), Mirror.reflectClass(holder.anonymous.javaClass).flags) },
            // TODO java local KClass error
            // `kotlin.reflect.jvm.internal.KotlinReflectionInternalError: Unresolved class: class com.teamwizardry.mirror.typeholders.ClassMirrorHolder$1LocalClass`
            // { assertEquals(setOf(Flag.LOCAL), Mirror.reflectClass(holder.local).flags) },
            // TODO java lambda KClass error
            // `kotlin.reflect.jvm.internal.KotlinReflectionInternalError: Unresolved class: class com.teamwizardry.mirror.typeholders.ClassMirrorHolder$$Lambda$246/50345623`
            // { assertEquals(setOf(Flag.SYNTHETIC), Mirror.reflectClass(holder.lambda.javaClass).flags) },
            { assertEquals(setOf(Flag.ABSTRACT, Flag.FINAL, Flag.PRIMITIVE), Mirror.Types.int.flags) },
            { testFlags<JObject1>() },
            { testFlags<EnumClass1>(Flag.ENUM) }
        )
    }

    // todo annotations, declaredAnnotations, simpleName, name, canonicalName

    @Test
    fun enumType_ofNonEnum_shouldReturnNull() {
        assertNull(Mirror.reflectClass<Object1>().enumType)
    }

    @Test
    fun enumType_ofEnumClass_shouldReturnSelf() {
        assertEquals(Mirror.reflectClass<EnumClass1>(), Mirror.reflectClass<EnumClass1>().enumType)
    }

    @Test
    fun enumType_ofAnonymousEnumSubclass_shouldReturnEnumClass() {
        assertEquals(Mirror.reflectClass<EnumClass1>(), Mirror.reflectClass(EnumClass1.ANONYMOUS.javaClass).enumType)
    }

    @Test
    fun enumConstants_ofNonEnum_shouldReturnNull() {
        assertNull(Mirror.reflectClass<Object1>().enumConstants)
    }

    @Test
    fun enumConstants_ofEnumClass_shouldReturnConstants() {
        assertEquals(listOf(*EnumClass1.values()), Mirror.reflectClass<EnumClass1>().enumConstants)
    }

    @Test
    fun enumConstants_ofAnonymousEnumSubclass_shouldReturnNull() {
        assertNull(Mirror.reflectClass(EnumClass1.ANONYMOUS.javaClass).enumConstants)
    }
}