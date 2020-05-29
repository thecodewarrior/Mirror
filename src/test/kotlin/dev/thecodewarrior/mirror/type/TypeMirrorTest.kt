package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.TypeToken
import dev.thecodewarrior.mirror.annotations.TypeAnnotation1
import dev.thecodewarrior.mirror.testsupport.KotlinTypeAnnotation1
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.assertInstanceOf
import dev.thecodewarrior.mirror.typeToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
internal class TypeMirrorTest: MTest() {
    val A by sources.add("A", "@rt(TYPE_USE) @interface A {}").typed<Annotation>()
    val B by sources.add("B", "@rt(TYPE_USE) @interface B { int a(); int b(); }").typed<Annotation>()
    val C by sources.add("C", "@rt(TYPE_USE) @interface C { int value(); }").typed<Annotation>()
    val D by sources.add("D", "@rt(TYPE_USE) @interface D { Class<?> value(); }").typed<Annotation>()
    val X by sources.add("X", """
        import dev.thecodewarrior.mirror.TypeToken;
        public class X {
            public static TypeToken token = new TypeToken<X>() {};
        }
    """)
    val Generic by sources.add("Generic", "class Generic<T> {}")

    val types = sources.types {
        typeVariables("T") {
            +"T"
            +"T[]"
        }
        +"? extends X"
        +"X"
        +"@A X"
        +"@B(a=1, b=2) X"
        +"@C(1) X"
        +"@A @B(a=1, b=2) @C(1) X"
        +"@D(X.class) X"
        +"Generic<@A X>"
        +"@A X[]"
        +"X @A[]"
        +"@A Generic<X>"
        +"@A Generic<X>[]"
        +"@A ? extends X"
    }

    @Test
    fun reflect_withClass_shouldReturnClassMirror() {
        assertInstanceOf<ClassMirror>(Mirror.reflect(Any::class.java))
    }

    @Test
    fun reflect_withClassTypeToken_shouldReturnClassMirror() {
        assertInstanceOf<ClassMirror>(Mirror.reflect(X.getField("token").get(null) as TypeToken<*>))
    }

    @Test
    fun reflect_withArray_shouldReturnArrayMirror() {
        assertInstanceOf<ArrayMirror>(Mirror.reflect(typeToken<Array<Any>>()))
    }

    @Test
    fun reflect_withGenericArray_shouldReturnArrayMirror() {
        assertInstanceOf<ArrayMirror>(Mirror.reflect(types["T[]"]))
    }

    @Test
    fun reflect_withVariable_shouldReturnVariableMirror() {
        assertInstanceOf<VariableMirror>(Mirror.reflect(types["T"]))
    }

    @Test
    fun reflect_withWildcard_shouldReturnWildcardMirror() {
        assertInstanceOf<WildcardMirror>(Mirror.reflect(types["? extends X"]))
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
            Mirror.reflectClass(types["T[]"])
        }
    }

    @Test
    fun reflectClass_withVariable_shouldThrow() {
        assertThrows<IllegalArgumentException> {
            Mirror.reflectClass(types["T"])
        }
    }

    @Test
    fun reflectClass_withWildcard_shouldThrow() {
        assertThrows<IllegalArgumentException> {
            Mirror.reflectClass(types["? extends X"])
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
        val X by sources.add("X", "class X {}")
        val C by sources.add("C", """
            class C {
                void method() {}
            }
        """)
        sources.compile()

        val method = C._m("method")
        val fromClassMirror = Mirror.reflectClass(C)
            .declaredMethods.first { it.name == "method" }
        assertSame(fromClassMirror, Mirror.reflect(method))
    }

    @Test
    fun reflect_withCoreField_shouldReturnSameObjectAsClassMirror() {
        val X by sources.add("X", "class X {}")
        val C by sources.add("C", """
            class C {
                int field;
            }
        """)
        sources.compile()
        val field = C._f("field")
        val fromClassMirror = Mirror.reflectClass(C).declaredFields.first { it.name == "field" }
        assertSame(fromClassMirror, Mirror.reflect(field))
    }

    @Test
    fun getAnnotation_ofUnannotatedType_shouldReturnEmptyList() {
        val type = Mirror.reflect(types["X"])
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }

    @Test
    fun getAnnotation_ofAnnotatedType_shouldReturnAnnotation() {
        val type = Mirror.reflect(types["@A X"])
        assertEquals(listOf(
            Mirror.newAnnotation(A)
        ), type.typeAnnotations)
    }

    @Test
    fun getAnnotation_ofMultiAnnotatedType_shouldReturnAnnotations() {
        val type = Mirror.reflect(types["@A @B(a=1, b=2) @C(1) X"])
        assertEquals(listOf(
            Mirror.newAnnotation(A),
            Mirror.newAnnotation(B, mapOf("a" to 1, "b" to 2)),
            Mirror.newAnnotation(C, mapOf("value" to 1))
        ), type.typeAnnotations)
    }

    @Test
    fun getAnnotation_ofAnnotatedTypeParameter_shouldReturnAnnotations() {
        val outer = Mirror.reflectClass(types["Generic<@A X>"])
        val type = outer.typeParameters[0]
        assertEquals(listOf(
            Mirror.newAnnotation(A)
        ), type.typeAnnotations)
    }

    @Test
    fun getAnnotation_ofAnnotatedArrayComponent_shouldReturnAnnotations() {
        val array = Mirror.reflect(types["@A X[]"]) as ArrayMirror
        val type = array.component
        assertEquals(listOf(
            Mirror.newAnnotation(A)
        ), type.typeAnnotations)
    }

    @Test
    fun getAnnotation_ofArrayWithAnnotatedComponent_shouldReturnEmptyList() {
        val type = Mirror.reflect(types["@A X[]"]) as ArrayMirror
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }

    @Test
    fun getAnnotation_ofAnnotatedArrayWithUnannotatedComponent_shouldReturnAnnotations() {
        val type = Mirror.reflect(types["X @A[]"]) as ArrayMirror
        assertEquals(listOf(
            Mirror.newAnnotation(A)
        ), type.typeAnnotations)
    }

    @Test
    fun typeAnnotations_ofJavaTypeWithJavaAnnotation_shouldReturnAnnotation() {
        val type = Mirror.reflectClass(types["@A X"])
        assertEquals(listOf(
            Mirror.newAnnotation(A)
        ), type.typeAnnotations)
    }

    @Test
    fun typeAnnotations_ofJavaTypeWithKotlinAnnotation_shouldReturnAnnotation() {
//        Kotlin type annotations don't work in Java
//        val type = Mirror.reflectClass(types["@KotlinTypeAnnotation1 Object"])
//        assertEquals(listOf(
//            Mirror.newAnnotation<KotlinTypeAnnotation1>()
//        ), type.typeAnnotations)
    }

    @Test
    fun typeAnnotations_ofKotlinTypeWithJavaAnnotation_shouldReturnNone() {
        val localHolder = object {
            fun method(): @TypeAnnotation1 Object1 { null!! }
        }
        val type = Mirror.reflectClass(localHolder::class.java._m("method").annotatedReturnType)
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }

    @Test
    fun typeAnnotations_ofKotlinTypeWithKotlinAnnotation_shouldReturnNone() {
        val localHolder = object {
            fun method(): @KotlinTypeAnnotation1 Object1 { null!! }
        }
        val type = Mirror.reflectClass(localHolder::class.java._m("method").annotatedReturnType)
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }

    @Test
    fun raw_ofAnnotatedArrayWithGenericComponent_shouldReturnErasure() {
        val type = Mirror.reflect(types["@A Generic<X>[]"]) as ArrayMirror
        assertEquals(
            Mirror.reflect(Generic)
        , type.raw.component)
    }

    @Test
    fun getAnnotation_ofUnannotatedComponentOfAnnotatedArray_shouldReturnEmptyList() {
        val array = Mirror.reflect(types["X @A[]"]) as ArrayMirror
        val type = array.component
        assertEquals(emptyList<Annotation>(), type.typeAnnotations)
    }

    @Test
    fun getAnnotation_ofAnnotatedWildcard_shouldReturnAnnotations() {
        val wildcard = Mirror.reflect(types["@A ? extends X"])
        assertEquals(listOf(Mirror.newAnnotation(A)), wildcard.typeAnnotations)
    }

    @Test
    fun `'typeAnnotationString' of a type with no annotations should return an empty string`() {
        assertEquals("", Mirror.reflectClass(types["X"]).typeAnnotationString())
    }

    @Test
    fun `'typeAnnotationString' of a type with a simple annotation should return the qualified annotation name followed by a space`() {
        assertEquals("@gen.A() ", Mirror.reflectClass(types["@A X"]).typeAnnotationString())
    }

    @Test
    fun `'typeAnnotationString' of a type with a parameterized annotation should include the parameters with the name`() {
        assertEquals("@gen.B(a=1, b=2) ", Mirror.reflectClass(types["@B(a=1, b=2) X"]).typeAnnotationString())
    }

    @Test
    fun `'typeAnnotationString' of a type with a value= annotation should include the value= parameter name`() {
        assertEquals("@gen.C(value=1) ", Mirror.reflectClass(types["@C(1) X"]).typeAnnotationString())
    }

    @Test
    fun `'typeAnnotationString' of a type with multiple annotations should return the annotation strings`() {
        assertEquals("@gen.A() @gen.B(a=1, b=2) @gen.C(value=1) ", Mirror.reflectClass(types["@A @B(a=1, b=2) @C(1) X"]).typeAnnotationString())
    }

    @Test
    fun `'typeAnnotationString' of a type with an annotation with a class parameter should use the fully qualified class name`() {
        assertEquals("@gen.D(value=class gen.X) ", Mirror.reflectClass(types["@D(X.class) X"]).typeAnnotationString())
    }
}