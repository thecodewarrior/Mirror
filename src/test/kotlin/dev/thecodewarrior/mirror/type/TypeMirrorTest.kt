package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.TypeToken
import dev.thecodewarrior.mirror.annotations.TypeAnnotation1
import dev.thecodewarrior.mirror.testsupport.GenericObject1
import dev.thecodewarrior.mirror.testsupport.GenericObject1Sub
import dev.thecodewarrior.mirror.testsupport.KotlinTypeAnnotation1
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.Object1Sub
import dev.thecodewarrior.mirror.testsupport.Object1Sub2
import dev.thecodewarrior.mirror.testsupport.Object2
import dev.thecodewarrior.mirror.testsupport.TestSources
import dev.thecodewarrior.mirror.testsupport.assertInstanceOf
import dev.thecodewarrior.mirror.typeToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
internal class TypeMirrorTest: MTest() {
    val sources = TestSources()

    val A by sources.add("A", "@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.RUNTIME) @interface A {}").typed<Annotation>()
    val AArg by sources.add("AArg", "@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.RUNTIME) @interface AArg { int v(); }").typed<Annotation>()
    val X by sources.add("X", """
        import dev.thecodewarrior.mirror.TypeToken;
        public class X {
            public static TypeToken token = new TypeToken<X>() {};
        }
    """.trimIndent())
    val Generic by sources.add("Generic", "class Generic<T> {}")

    val types = sources.types {
        typeVariables("T") {
            +"T"
            +"T[]"
        }
        +"? extends X"
        +"X"
        +"@A X"
        +"@A @AArg(v=1) X"
        +"Generic<@A X>"
        +"@A X[]"
        +"X @A[]"
        +"@A Generic<X>"
        +"@A Generic<X>[]"
        +"Generic<@A ? extends X>"
    }

    init {
        sources.compile()
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
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val C by sources.add("C", """
            class C {
                void method() {}
            }
        """.trimIndent())
        sources.compile()

        val method = C._m("method")
        val fromClassMirror = Mirror.reflectClass(C)
            .declaredMethods.first { it.name == "method" }
        assertSame(fromClassMirror, Mirror.reflect(method))
    }

    @Test
    fun reflect_withCoreField_shouldReturnSameObjectAsClassMirror() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val C by sources.add("C", """
            class C {
                int field;
            }
        """.trimIndent())
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
        val type = Mirror.reflect(types["@A @AArg(v=1) X"])
        assertEquals(listOf(
            Mirror.newAnnotation(A),
            Mirror.newAnnotation(AArg, mapOf("v" to 1))
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
        val wildcard = Mirror.reflectClass(types["Generic<@A ? extends X>"]).typeParameters[0]
        assertEquals(listOf(Mirror.newAnnotation(A)), wildcard.typeAnnotations)
    }
}