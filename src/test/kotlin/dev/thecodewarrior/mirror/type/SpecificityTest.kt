package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.GenericObject1
import dev.thecodewarrior.mirror.testsupport.GenericObject1Sub
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.Object2
import dev.thecodewarrior.mirror.testsupport.TestSources
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("PropertyName")
internal class SpecificityTest: MTest() {
    val A by sources.add("A", "@rt(TYPE_USE) @interface A {}").typed<Annotation>()
    val A2 by sources.add("A2", "@rt(TYPE_USE) @interface A2 {}").typed<Annotation>()
    val X by sources.add("X", "class X {}")
    val Y by sources.add("Y", "class Y {}")
    val X_X by sources.add("X_X", "class X_X extends X {}")
    val X_Y by sources.add("X_Y", "class X_Y extends X {}")
    val Generic by sources.add("Generic", "class Generic<T> {}")
    val Generic_Subclass by sources.add("Generic_Subclass", "class Generic_Subclass<T> extends Generic<T> {}")

    val types = sources.types {
        +"Generic<X>"
        +"Generic_Subclass<Y>"
    }

    @Test
    fun specificity_ofMutuallyAssignable_shouldBeEqual() {
        val type = Mirror.reflect<Object1>()
        val type1 = type.withTypeAnnotations(listOf(Mirror.newAnnotation(A)))
        val type2 = type.withTypeAnnotations(listOf(Mirror.newAnnotation(A2)))
        Assertions.assertEquals(0, type1.specificity.compareTo(type2.specificity))
    }

    @Test
    fun specificity_ofSelf_shouldBeEqual() {
        val type = Mirror.reflect(X).specificity
        Assertions.assertEquals(0, type.compareTo(type))
    }

    @Test
    fun specificity_ofSeparateTypes_shouldBeEqual() {
        val type1 = Mirror.reflect(X).specificity
        val type2 = Mirror.reflect(Y).specificity
        Assertions.assertEquals(0, type1.compareTo(type2))
        Assertions.assertEquals(0, type2.compareTo(type1))
    }

    @Test
    fun specificity_ofSeparateSubTypes_shouldBeEqual() {
        val type1 = Mirror.reflect(X_X).specificity
        val type2 = Mirror.reflect(X_Y).specificity
        Assertions.assertEquals(0, type1.compareTo(type2))
        Assertions.assertEquals(0, type2.compareTo(type1))
    }

    @Test
    fun specificity_ofSubclass_shouldBeGreater() {
        val superclass = Mirror.reflect(X).specificity
        val subclass = Mirror.reflect(X_X).specificity
        Assertions.assertEquals(-1, superclass.compareTo(subclass))
        Assertions.assertEquals(1, subclass.compareTo(superclass))
    }

    @Test
    fun specificity_ofSpecifiedGeneric_shouldBeGreater() {
        val unspecified = Mirror.reflect(Generic).specificity
        val specified = Mirror.reflect(types["Generic<X>"]).specificity
        Assertions.assertEquals(-1, unspecified.compareTo(specified))
        Assertions.assertEquals(1, specified.compareTo(unspecified))
    }

    @Test
    fun specificity_ofSubclass_shouldBeEqualTo_specifiedGeneric() {
        val subclass = Mirror.reflect(Generic_Subclass).specificity
        val specified = Mirror.reflect(types["Generic<X>"]).specificity
        Assertions.assertEquals(0, specified.compareTo(subclass))
        Assertions.assertEquals(0, subclass.compareTo(specified))
    }

    @Test
    fun specificity_ofIncompatibleGenerics_shouldBeEqual() {
        val list1 = Mirror.reflect(types["Generic<X>"]).specificity
        val list2 = Mirror.reflect(types["Generic_Subclass<Y>"]).specificity
        Assertions.assertEquals(0, list1.compareTo(list2))
        Assertions.assertEquals(0, list2.compareTo(list1))
    }
}