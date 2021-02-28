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
        Assertions.assertEquals(0, TypeSpecificityComparator.compare(type1, type2))
    }

    @Test
    fun specificity_ofSelf_shouldBeEqual() {
        val type = Mirror.reflect(X)
        Assertions.assertEquals(0, TypeSpecificityComparator.compare(type, type))
    }

    @Test
    fun specificity_ofSeparateTypes_shouldBeEqual() {
        val type1 = Mirror.reflect(X)
        val type2 = Mirror.reflect(Y)
        Assertions.assertEquals(0,TypeSpecificityComparator.compare(type1, type2))
        Assertions.assertEquals(0,TypeSpecificityComparator.compare(type2, type1))
    }

    @Test
    fun specificity_ofSeparateSubTypes_shouldBeEqual() {
        val type1 = Mirror.reflect(X_X)
        val type2 = Mirror.reflect(X_Y)
        Assertions.assertEquals(0,TypeSpecificityComparator.compare(type1, type2))
        Assertions.assertEquals(0,TypeSpecificityComparator.compare(type2, type1))
    }

    @Test
    fun specificity_ofSubclass_shouldBeGreater() {
        val superclass = Mirror.reflect(X)
        val subclass = Mirror.reflect(X_X)
        Assertions.assertEquals(-1,TypeSpecificityComparator.compare(superclass, subclass))
        Assertions.assertEquals(1,TypeSpecificityComparator.compare(subclass, superclass))
    }

    @Test
    fun specificity_ofSpecifiedGeneric_shouldBeGreater() {
        val unspecified = Mirror.reflect(Generic)
        val specified = Mirror.reflect(types["Generic<X>"])
        Assertions.assertEquals(-1,TypeSpecificityComparator.compare(unspecified, specified))
        Assertions.assertEquals(1,TypeSpecificityComparator.compare(specified, unspecified))
    }

    @Test
    fun specificity_ofSubclass_shouldBeEqualTo_specifiedGeneric() {
        val subclass = Mirror.reflect(Generic_Subclass)
        val specified = Mirror.reflect(types["Generic<X>"])
        Assertions.assertEquals(0,TypeSpecificityComparator.compare(specified, subclass))
        Assertions.assertEquals(0,TypeSpecificityComparator.compare(subclass, specified))
    }

    @Test
    fun specificity_ofIncompatibleGenerics_shouldBeEqual() {
        val list1 = Mirror.reflect(types["Generic<X>"])
        val list2 = Mirror.reflect(types["Generic_Subclass<Y>"])
        Assertions.assertEquals(0,TypeSpecificityComparator.compare(list1, list2))
        Assertions.assertEquals(0,TypeSpecificityComparator.compare(list2, list1))
    }
}