package com.teamwizardry.mirror.specialization

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.GenericObject1
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.Object1
import com.teamwizardry.mirror.testsupport.Object2
import com.teamwizardry.mirror.testsupport.OuterClass1
import com.teamwizardry.mirror.testsupport.OuterGenericClass1
import com.teamwizardry.mirror.testsupport.assertSameSet
import com.teamwizardry.mirror.type.ClassMirror
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
        val inner = Mirror.reflectClass(OuterClass1.OuterClass1_InnerClass::class.java)
        assertSame(outer, inner.enclosingClass)
    }

    @Test
    @DisplayName("Getting the enclosing class of an inner class with a raw generic outer class should return the outer class")
    fun enclosingClass_ofInnerClassWithRawGenericOuterClass_shouldReturnRawGenericOuterClass() {
        val outer = Mirror.reflectClass(OuterGenericClass1::class.java)
        val inner = Mirror.reflectClass(OuterGenericClass1.OuterGenericClass1_InnerClass::class.java)
        assertSame(outer, inner.enclosingClass)
    }

    @Test
    @DisplayName("Getting the enclosing class of a class with a specialized outer class should return the outer class")
    fun enclosingClass_ofInnerClassWithSpecializedOuterClass_shouldReturnSpecializedOuterClass() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>>()
        val inner = Mirror.reflectClass(OuterGenericClass1.OuterGenericClass1_InnerClass::class.java)
        val specialized = inner.enclose(outer)
        assertSame(outer, specialized.enclosingClass)
    }

    @Test
    @DisplayName("Getting the enclosing type of a class with a directly specified specialized outer class should return the specialized outer class")
    fun enclosingClass_ofInnerClassWithDirectlySpecifiedOuterClass_shouldReturnSpecializedOuterClass() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>>()
        val inner = Mirror.reflectClass<OuterGenericClass1<String>.OuterGenericClass1_InnerClass>()
        assertSame(outer, inner.enclosingClass)
    }

    @Test
    @DisplayName("Getting the mirror of a class with a directly specified specialized outer class should be identical to the mirror with the outer class specified directly")
    fun mirror_ofInnerClassWithDirectlySpecializedOuterClass_shouldBeIdenticalToInnerClassWithOuterClassSpecializedManually() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>>()
        val inner = Mirror.reflectClass(OuterGenericClass1.OuterGenericClass1_InnerClass::class.java)
        val specialized = inner.enclose(outer)
        val innerDirect = Mirror.reflectClass<OuterGenericClass1<String>.OuterGenericClass1_InnerClass>()
        assertSame(specialized, innerDirect)
    }

    @Test
    @DisplayName("Getting the enclosing type of a doubly-nested class with a directly specified specialized outer class should return the singly-nested specialized outer class")
    fun enclosingClass_ofDoublyInnerClassWithDirectlySpecifiedOuterClass_shouldReturnSinglyInnerClassWithSpecializedOuterClass() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>.OuterGenericClass1_InnerClass>()
        val inner = Mirror.reflectClass<OuterGenericClass1<String>.OuterGenericClass1_InnerClass.OuterGenericClass1_InnerClass_InnerClass>()
        assertSame(outer, inner.enclosingClass)
    }

    @Test
    @DisplayName("Getting the mirror of a class with a directly specified specialized outer class should be identical to the mirror with the outer class specified directly")
    fun mirror_ofDoublyInnerClassWithDirectlySpecializedOuterClass_shouldBeIdenticalToDoublyInnerClassWithOuterClassesSpecializedManually() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>>()
        val middle = Mirror.reflectClass(OuterGenericClass1.OuterGenericClass1_InnerClass::class.java).enclose(outer)
        val inner = Mirror.reflectClass(OuterGenericClass1.OuterGenericClass1_InnerClass.OuterGenericClass1_InnerClass_InnerClass::class.java).enclose(middle)
        val innerDirect = Mirror.reflectClass<OuterGenericClass1<String>.OuterGenericClass1_InnerClass.OuterGenericClass1_InnerClass_InnerClass>()
        assertSame(inner, innerDirect)
    }

    @Test
    @DisplayName("Getting the field type of a field in a class with a specialized outer class should return the specialized field type")
    fun fieldType_ofFieldInClassWithGenericOuterClass_shouldReturnOuterClassTypeParameter() {
        val outer = Mirror.reflectClass(OuterGenericClass1::class.java)
        val inner = Mirror.reflectClass(OuterGenericClass1.OuterGenericClass1_InnerClass::class.java)
        val specialized = inner.field("innerField")?.type
        assertSame(outer.typeParameters[0], specialized)
    }

    @Test
    @DisplayName("Getting the field type of a field in a class with a specialized outer class should return the specialized field type")
    fun fieldType_ofFieldInInnerClassWithSpecializedOuterClass_shouldReturnSpecializedFieldType() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>>()
        val inner = Mirror.reflectClass(OuterGenericClass1.OuterGenericClass1_InnerClass::class.java)
        val specialized = inner.enclose(outer).field("innerField")?.type
        assertSame(outer.typeParameters[0], specialized)
    }

    @Test
    @DisplayName("Getting the return type of a method in a class with a specialized outer class should return the specialized return type")
    fun returnType_ofMethodInInnerClassWithSpecializedOuterClass_shouldReturnSpecializedReturnType() {
        val outer = Mirror.reflectClass<OuterGenericClass1<String>>()
        val inner = Mirror.reflectClass(OuterGenericClass1.OuterGenericClass1_InnerClass::class.java)
        val specialized = inner.enclose(outer).methods("innerMethod")[0].returnType
        assertSame(outer.typeParameters[0], specialized)
    }

    @Test
    @DisplayName("Calling enclose on a root class and passing a non-null outer class should throw")
    fun enclose_ofRootClass_whenPassedNonnull_shouldThrow() {
        val root = Mirror.reflectClass(Object1::class.java)
        val outer = Mirror.reflectClass(Object2::class.java)
        assertThrows<InvalidSpecializationException> {
            root.enclose(outer)
        }
    }

    @Test
    @DisplayName("Calling enclose on a root class and passing a null outer class should return itself")
    fun enclose_ofSpecializedRootClass_whenPassedNull_shouldReturnSelf() {
        val root = Mirror.reflectClass<GenericObject1<String>>()
        assertSame(root, root.enclose(null))
    }

    @Test
    @DisplayName("Calling enclose on a specialized inner class and passing a null outer class should return itself without a specialized enclosing class")
    fun enclose_ofSpecializedInnerClass_whenPassedNull_shouldReturnSpecializedSelfWithoutSpecializedEnclosingClass() {
        val inner = Mirror.reflectClass<OuterGenericClass1<String>.OuterGenericClass1_InnerGenericClass<Object1>>()
        val withRawOuter = Mirror.reflectClass(OuterGenericClass1.OuterGenericClass1_InnerGenericClass::class.java).specialize(Mirror.reflectClass<Object1>())
        assertSame(withRawOuter, inner.enclose(null))
    }

    @Test
    @DisplayName("Getting the declared classes of a leaf class should return an empty list")
    fun declaredClasses_ofLeafClass_shouldBeEmpty() {
        val clazz = Mirror.reflectClass(Object1::class.java)
        assertEquals(emptyList<ClassMirror>(), clazz.declaredClasses)
    }

    @Test
    @DisplayName("Getting the declared classes of a parent class should return a list of the inner classes")
    fun declaredClasses_ofParentClass_shouldReturnInnerClasses() {
        val outer = Mirror.reflectClass(OuterClass1::class.java)
        val innerStatic = Mirror.reflectClass(OuterClass1.OuterClass1_InnerStaticClass::class.java)
        val inner = Mirror.reflectClass(OuterClass1.OuterClass1_InnerClass::class.java)
        val inner2 = Mirror.reflectClass(OuterClass1.OuterClass1_InnerClass2::class.java)
        assertSameSet(listOf(innerStatic, inner, inner2), outer.declaredClasses)
    }
}