package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.annotations.TypeAnnotation1
import dev.thecodewarrior.mirror.annotations.TypeAnnotation2
import dev.thecodewarrior.mirror.annotations.TypeAnnotationArg1
import dev.thecodewarrior.mirror.testsupport.AnnotatedTypeHolder
import dev.thecodewarrior.mirror.testsupport.GenericObject1
import dev.thecodewarrior.mirror.testsupport.GenericObject1Sub
import dev.thecodewarrior.mirror.testsupport.KotlinTypeAnnotation1
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.Object1Sub
import dev.thecodewarrior.mirror.testsupport.Object1Sub2
import dev.thecodewarrior.mirror.testsupport.Object2
import dev.thecodewarrior.mirror.testsupport.assertInstanceOf
import dev.thecodewarrior.mirror.typeToken
import dev.thecodewarrior.mirror.typeholders.TypeMirrorHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

internal class TypeMirrorTest: MirrorTestBase(TypeMirrorHolder()) {

    @Test
    fun reflect_withClass_shouldReturnClassMirror() {
        assertInstanceOf<ClassMirror>(Mirror.reflect(Any::class.java))
    }

    @Test
    fun reflect_withArray_shouldReturnArrayMirror() {
        assertInstanceOf<ArrayMirror>(Mirror.reflect(typeToken<Array<Any>>()))
    }

    @Test
    fun reflect_withGenericArray_shouldReturnArrayMirror() {
        assertInstanceOf<ArrayMirror>(Mirror.reflect(holder["T[]; T"]))
    }

    @Test
    fun reflect_withVariable_shouldReturnVariableMirror() {
        assertInstanceOf<VariableMirror>(Mirror.reflect(holder["T"]))
    }

    @Test
    fun reflect_withWildcard_shouldReturnWildcardMirror() {
        assertInstanceOf<WildcardMirror>(Mirror.reflect(holder["? extends Object1Sub"]))
    }

    @Test
    fun reflect_withVoid_shouldReturnVoidMirror() {
        assertInstanceOf<VoidMirror>(Mirror.reflect(Void.TYPE))
    }

    @Test
    fun reflectClass_withClass_shouldReturnClassMirror() {
        assertInstanceOf<ClassMirror>(Mirror.reflectClass(Any::class.java))
    }

    @Test
    fun reflectClass_withArray_shouldThrow() {
        assertThrows<IllegalArgumentException> {
            Mirror.reflectClass(typeToken<Array<Any>>())
        }
    }

    @Test
    fun reflectClass_withGenericArray_shouldThrow() {
        assertThrows<IllegalArgumentException> {
            Mirror.reflectClass(holder["T[]; T"])
        }
    }

    @Test
    fun reflectClass_withVariable_shouldThrow() {
        assertThrows<IllegalArgumentException> {
            Mirror.reflectClass(holder["T"])
        }
    }

    @Test
    fun reflectClass_withWildcard_shouldThrow() {
        assertThrows<IllegalArgumentException> {
            Mirror.reflectClass(holder["? extends Object1Sub"])
        }
    }

    @Test
    fun reflectClass_withVoid_shouldThrow() {
        assertThrows<IllegalArgumentException> {
            Mirror.reflectClass(Void.TYPE)
        }
    }

    @Test
    fun reflect_withCoreMethod_shouldReturnSameObjectAsClassMirror() {
        val method = holder.m("void method()")
        val fromClassMirror = Mirror.reflectClass(holder.c("ReflectAndClassMirrorGetSame"))
            .declaredMethods.first { it.name == "method" }
        assertSame(fromClassMirror, Mirror.reflect(method))
    }

    @Test
    fun reflect_withCoreField_shouldReturnSameObjectAsClassMirror() {
        val field = holder.f("int field")
        val fromClassMirror = Mirror.reflectClass(holder.c("ReflectAndClassMirrorGetSame")).getDeclaredField("field")
        assertSame(fromClassMirror, Mirror.reflect(field))
    }

    @Test
    @DisplayName("Getting the annotations of an unannotated type should return an empty list")
    fun getAnnotation_ofUnannotatedType_shouldReturnEmptyList() {
        val type = Mirror.reflect(holder["Object1"])
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of type with one annotation should return that annotation")
    fun getAnnotation_ofAnnotatedType_shouldReturnAnnotation() {
        val type = Mirror.reflect(holder["@TypeAnnotation1 Object1"])
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>()
        ), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of a type with multiple annotations should return the correct annotations")
    fun getAnnotation_ofMultiAnnotatedType_shouldReturnAnnotations() {
        val type = Mirror.reflect(holder["@TypeAnnotation1 @TypeAnnotationArg1(arg = 1) Object1"])
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>(),
            Mirror.newAnnotation<TypeAnnotationArg1>(mapOf("arg" to 1))
        ), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of an annotated type parameter should return the correct annotations")
    fun getAnnotation_ofAnnotatedTypeParameter_shouldReturnAnnotations() {
        val outer = Mirror.reflect(holder["GenericObject1<@TypeAnnotation1 Object1>"]) as ClassMirror
        val type = outer.typeParameters[0]
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>()
        ), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of an annotated array component should return the correct annotations")
    fun getAnnotation_ofAnnotatedArrayComponent_shouldReturnAnnotations() {
        val array = Mirror.reflect(holder["@TypeAnnotation1 Object[]"]) as ArrayMirror
        val type = array.component
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>()
        ), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of an array with an annotated component should return an empty list")
    fun getAnnotation_ofArrayWithAnnotatedComponent_shouldReturnEmptyList() {
        val type = Mirror.reflect(holder["@TypeAnnotation1 Object[]"]) as ArrayMirror
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of an annotated array with an unannotated component should return the correct annotations")
    fun getAnnotation_ofAnnotatedArrayWithUnannotatedComponent_shouldReturnAnnotations() {
        val type = Mirror.reflect(holder["Object @TypeAnnotation1[]"]) as ArrayMirror
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>()
        ), type.typeAnnotations)
    }

    @Test
    fun typeAnnotations_ofJavaTypeWithJavaAnnotation_shouldReturnAnnotation() {
        val type = Mirror.reflectClass(holder["@TypeAnnotation1 Object1"])
        assertEquals(listOf(
            Mirror.newAnnotation<TypeAnnotation1>()
        ), type.typeAnnotations)
    }

    @Test
    fun typeAnnotations_ofJavaTypeWithKotlinAnnotation_shouldReturnAnnotation() {
//        Kotlin type annotations don't work in Java
//        val type = Mirror.reflectClass(holder["@KotlinTypeAnnotation1 Object"])
//        assertEquals(listOf(
//            Mirror.newAnnotation<KotlinTypeAnnotation1>()
//        ), type.typeAnnotations)
    }

    @Test
    fun typeAnnotations_ofKotlinTypeWithJavaAnnotation_shouldReturnNone() {
        val localHolder = object: AnnotatedTypeHolder() {
            @TypeHolder("@TypeAnnotation1 Object1")
            fun someFun(arg: @TypeAnnotation1 Object1) {}
        }
        val type = Mirror.reflectClass(localHolder["@TypeAnnotation1 Object1"])
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }

    @Test
    fun typeAnnotations_ofKotlinTypeWithKotlinAnnotation_shouldReturnNone() {
        val localHolder = object: AnnotatedTypeHolder() {
            @TypeHolder("@KotlinTypeAnnotation1 Object1")
            fun someFun(arg: @KotlinTypeAnnotation1 Object1) {}
        }
        val type = Mirror.reflectClass(localHolder["@KotlinTypeAnnotation1 Object1"])
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the raw type of an annotated array with an generic component should should return the correct erasure")
    fun raw_ofAnnotatedArrayWithGenericComponent_shouldReturnErasure() {
        val type = Mirror.reflect(holder["@TypeAnnotation1 GenericObject1<Object1>[]"]) as ArrayMirror
        assertEquals(
            Mirror.reflect(GenericObject1::class.java)
        , type.raw.component)
    }

    @Test
    @DisplayName("Getting the annotations of the unannotated component of an annotated array should return an empty list")
    fun getAnnotation_ofUnannotatedComponentOfAnnotatedArray_shouldReturnEmptyList() {
        val array = Mirror.reflect(holder["Object @TypeAnnotation1[]"]) as ArrayMirror
        val type = array.component
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }

    @Test
    @DisplayName("Getting the annotations of the unannotated component of an annotated array should return an empty list")
    fun getAnnotation_ofAnnotatedWildcard_shouldReturnAnnotations() {
        val wildcard = Mirror.reflectClass(holder["List<@TypeAnnotation1 ? extends Object1>"]).typeParameters[0]
        assertEquals(listOf(Mirror.newAnnotation<TypeAnnotation1>()), wildcard.typeAnnotations)
    }

    @Test
    @DisplayName("Reflecting a self-referential type should not infinitely recurse")
    fun reflect_onSelfReferentialType_shouldNotRecurse() {
        class TestType<T: TestType<T>>: GenericObject1<TestType<T>>()

        Mirror.reflect(typeToken<TestType<*>>())
    }

    @Test
    @DisplayName("Reflecting a class with looping generic inheritance should not infinitely recurse")
    fun reflect_withLoopingGenericInheritance_shouldNotRecurse() {
        open class ParentType<T>
        class ChildClass: ParentType<ChildClass>()

        Mirror.reflect(typeToken<ChildClass>())
    }

    @Test
    fun specificity_ofMutuallyAssignable_shouldBeEqual() {
        val type = Mirror.reflect<Object1>()
        val type1 = type.withTypeAnnotations(listOf(Mirror.newAnnotation<TypeAnnotation1>()))
        val type2 = type.withTypeAnnotations(listOf(Mirror.newAnnotation<TypeAnnotation2>()))
        assertEquals(0, type1.specificity.compareTo(type2.specificity))
    }

    @Test
    fun specificity_ofSelf_shouldBeEqual() {
        val type = Mirror.reflect<Object1>().specificity
        assertEquals(0, type.compareTo(type))
    }

    @Test
    fun specificity_ofSeparateTypes_shouldBeEqual() {
        val type1 = Mirror.reflect<Object1>().specificity
        val type2 = Mirror.reflect<Object2>().specificity
        assertEquals(0, type1.compareTo(type2))
        assertEquals(0, type2.compareTo(type1))
    }

    @Test
    fun specificity_ofSeparateSubTypes_shouldBeEqual() {
        val type1 = Mirror.reflect<Object1Sub>().specificity
        val type2 = Mirror.reflect<Object1Sub2>().specificity
        assertEquals(0, type1.compareTo(type2))
        assertEquals(0, type2.compareTo(type1))
    }

    @Test
    fun specificity_ofSubclass_shouldBeGreater() {
        val superClass = Mirror.reflect<Object1>().specificity
        val subClass = Mirror.reflect<Object1Sub>().specificity
        assertEquals(-1, superClass.compareTo(subClass))
        assertEquals(1, subClass.compareTo(superClass))
    }

    @Test
    fun specificity_ofSpecifiedGeneric_shouldBeGreater() {
        val unspecified = Mirror.reflect(GenericObject1::class.java).specificity
        val specified = Mirror.reflect<GenericObject1<String>>().specificity
        assertEquals(-1, unspecified.compareTo(specified))
        assertEquals(1, specified.compareTo(unspecified))
    }

    @Test
    fun specificity_ofSubclass_shouldBeEqualTo_specifiedGeneric() {
        val subclass = Mirror.reflect(GenericObject1Sub::class.java).specificity
        val specified = Mirror.reflect<GenericObject1<String>>().specificity
        assertEquals(0, specified.compareTo(subclass))
        assertEquals(0, subclass.compareTo(specified))
    }

    @Test
    fun specificity_ofIncompatibleGenerics_shouldBeEqual() {
        val list1 = Mirror.reflect<List<Object1>>().specificity
        val list2 = Mirror.reflect<ArrayList<Object2>>().specificity
        assertEquals(0, list1.compareTo(list2))
        assertEquals(0, list2.compareTo(list1))
    }
}