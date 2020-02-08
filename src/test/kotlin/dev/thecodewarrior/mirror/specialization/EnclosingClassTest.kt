package dev.thecodewarrior.mirror.specialization

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.GenericObject1
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.Object2
import dev.thecodewarrior.mirror.testsupport.OuterClass1
import dev.thecodewarrior.mirror.testsupport.OuterGenericClass1
import dev.thecodewarrior.mirror.testsupport.assertSameSet
import dev.thecodewarrior.mirror.type.ClassMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EnclosingClassTest: MirrorTestBase() {
    @Test
    @DisplayName("Getting the enclosing class of a root class should return null")
    fun enclosingClass_ofRootClass_shouldReturnNull() {
        val clazz = Mirror.reflectClass(Object1::class.java)
        assertNull(clazz.enclosingClass)
    }

    @Test
    @DisplayName("Getting the enclosing class of an inner class should return the outer class")
    fun enclosingClass_ofInnerClass_shouldReturnOuterClass() {
        val outer = Mirror.reflectClass(OuterClass1::class.java)
        val inner = Mirror.reflectClass(OuterClass1.InnerClass::class.java)
        assertSame(outer, inner.enclosingClass)
    }

    @Test
    @DisplayName("Getting the enclosing class of an inner class with a raw generic outer class should return the outer class")
    fun enclosingClass_ofInnerClassWithRawGenericOuterClass_shouldReturnRawGenericOuterClass() {
        val outer = Mirror.reflectClass(OuterGenericClass1::class.java)
        val inner = Mirror.reflectClass(OuterGenericClass1.InnerClass::class.java)
        assertSame(outer, inner.enclosingClass)
    }

    @Test
    @DisplayName("Getting the enclosing class of a class with a specialized outer class should return the outer class")
    fun enclosingClass_ofInnerClassWithSpecializedOuterClass_shouldReturnSpecializedOuterClass() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>>()
        val inner = Mirror.reflectClass(OuterGenericClass1.InnerClass::class.java)
        val specialized = inner.withEnclosingClass(outer)
        assertSame(outer, specialized.enclosingClass)
    }

    @Test
    @DisplayName("Getting the enclosing type of a class with a directly specified specialized outer class should return the specialized outer class")
    fun enclosingClass_ofInnerClassWithDirectlySpecifiedOuterClass_shouldReturnSpecializedOuterClass() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>>()
        val inner = Mirror.reflectClass<OuterGenericClass1<String>.InnerClass>()
        assertSame(outer, inner.enclosingClass)
    }

    @Test
    @DisplayName("Getting the mirror of a class with a directly specified specialized outer class should be identical to the mirror with the outer class specified directly")
    fun mirror_ofInnerClassWithDirectlySpecializedOuterClass_shouldBeIdenticalToInnerClassWithOuterClassSpecializedManually() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>>()
        val inner = Mirror.reflectClass(OuterGenericClass1.InnerClass::class.java)
        val specialized = inner.withEnclosingClass(outer)
        val innerDirect = Mirror.reflectClass<OuterGenericClass1<String>.InnerClass>()
        assertSame(specialized, innerDirect)
    }

    @Test
    @DisplayName("Getting the enclosing type of a doubly-nested class with a directly specified specialized outer class should return the singly-nested specialized outer class")
    fun enclosingClass_ofDoublyInnerClassWithDirectlySpecifiedOuterClass_shouldReturnSinglyInnerClassWithSpecializedOuterClass() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>.InnerClass>()
        val inner = Mirror.reflectClass<OuterGenericClass1<String>.InnerClass.InnerClass>()
        assertSame(outer, inner.enclosingClass)
    }

    @Test
    @DisplayName("Getting the mirror of a class with a directly specified specialized outer class should be identical to the mirror with the outer class specified directly")
    fun mirror_ofDoublyInnerClassWithDirectlySpecializedOuterClass_shouldBeIdenticalToDoublyInnerClassWithOuterClassesSpecializedManually() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>>()
        val middle = Mirror.reflectClass(OuterGenericClass1.InnerClass::class.java).withEnclosingClass(outer)
        val inner = Mirror.reflectClass(OuterGenericClass1.InnerClass.InnerClass::class.java).withEnclosingClass(middle)
        val innerDirect = Mirror.reflectClass<OuterGenericClass1<String>.InnerClass.InnerClass>()
        assertSame(inner, innerDirect)
    }

    @Test
    @DisplayName("Getting the field type of a field in a class with a specialized outer class should return the specialized field type")
    fun fieldType_ofFieldInClassWithGenericOuterClass_shouldReturnOuterClassTypeParameter() {
        val outer = Mirror.reflectClass(OuterGenericClass1::class.java)
        val inner = Mirror.reflectClass(OuterGenericClass1.InnerClass::class.java)
        val specialized = inner.findPublicField("innerField")?.type
        assertSame(outer.typeParameters[0], specialized)
    }

    @Test
    @DisplayName("Getting the field type of a field in a class with a specialized outer class should return the specialized field type")
    fun fieldType_ofFieldInInnerClassWithSpecializedOuterClass_shouldReturnSpecializedFieldType() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>>()
        val inner = Mirror.reflectClass(OuterGenericClass1.InnerClass::class.java)
        val specialized = inner.withEnclosingClass(outer).findPublicField("innerField")?.type
        assertSame(outer.typeParameters[0], specialized)
    }

    @Test
    @DisplayName("Getting the return type of a method in a class with a specialized outer class should return the specialized return type")
    fun returnType_ofMethodInInnerClassWithSpecializedOuterClass_shouldReturnSpecializedReturnType() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>>()
        val inner = Mirror.reflectClass(OuterGenericClass1.InnerClass::class.java)
        val specialized = inner.withEnclosingClass(outer).findPublicMethods("innerMethod")[0].returnType
        assertSame(outer.typeParameters[0], specialized)
    }

    @Test
    @DisplayName("Calling enclose on a root class and passing a non-null outer class should throw")
    fun enclose_ofRootClass_whenPassedNonnull_shouldThrow() {
        val root = Mirror.reflectClass(Object1::class.java)
        val outer = Mirror.reflectClass(Object2::class.java)
        assertThrows<InvalidSpecializationException> {
            root.withEnclosingClass(outer)
        }
    }

    @Test
    @DisplayName("Calling enclose on a root class and passing a null outer class should return itself")
    fun enclose_ofSpecializedRootClass_whenPassedNull_shouldReturnSelf() {
        val root = Mirror.reflectClass<GenericObject1<String>>()
        assertSame(root, root.withEnclosingClass(null))
    }

    @Test
    @DisplayName("Calling enclose on a specialized inner class and passing a null outer class should return itself without a specialized enclosing class")
    fun enclose_ofSpecializedInnerClass_whenPassedNull_shouldReturnSpecializedSelfWithoutSpecializedEnclosingClass() {
        val inner = Mirror.reflectClass<OuterGenericClass1<String>.InnerGenericClass<Object1>>()
        val withRawOuter = Mirror.reflectClass(OuterGenericClass1.InnerGenericClass::class.java).withTypeArguments(Mirror.reflectClass<Object1>())
        assertSame(withRawOuter, inner.withEnclosingClass(null))
    }

    @Test
    @DisplayName("Getting the declared classes of a leaf class should return an empty list")
    fun declaredClasses_ofLeafClass_shouldBeEmpty() {
        val clazz = Mirror.reflectClass(Object1::class.java)
        assertEquals(emptyList<ClassMirror>(), clazz.declaredMemberClasses)
    }

    @Test
    @DisplayName("Getting the declared classes of a parent class should return a list of the inner classes")
    fun declaredClasses_ofParentClass_shouldReturnInnerClasses() {
        val outer = Mirror.reflectClass(OuterClass1::class.java)
        val innerStatic = Mirror.reflectClass(OuterClass1.InnerStaticClass::class.java)
        val inner = Mirror.reflectClass(OuterClass1.InnerClass::class.java)
        val inner2 = Mirror.reflectClass(OuterClass1.InnerClass2::class.java)
        assertSameSet(listOf(innerStatic, inner, inner2), outer.declaredMemberClasses)
    }
}