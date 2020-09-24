package dev.thecodewarrior.mirror.utils

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.assertSameList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Suppress("LocalVariableName")
internal class ReflectionUtilsTest: MTest() {
    @Test
    fun `'annotationString' of a type with no annotations should return an empty string`() {
        assertEquals("", emptyList<Annotation>().annotationString())
    }

    @Test
    fun `'annotationString' of a type with a simple annotation should return the qualified annotation name followed by a space`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A {}")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"@A X"
        }
        sources.compile()
        assertEquals("@gen.A() ", types["@A X"].annotations.annotationString())
    }

    @Test
    fun `'annotationString' of a type with a parameterized annotation should include the parameters with the name`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { int a(); int b(); }")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"@A(a=1, b=2) X"
        }
        sources.compile()
        assertEquals("@gen.A(a=1, b=2) ", types["@A(a=1, b=2) X"].annotations.annotationString())
    }

    @Test
    fun `'annotationString' of a type with a value= annotation should include the value= parameter name`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { int value(); }")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"@A(1) X"
        }
        sources.compile()
        assertEquals("@gen.A(value=1) ", types["@A(1) X"].annotations.annotationString())
    }

    @Test
    fun `'annotationString' of a type with multiple annotations should return the annotation strings`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A {}")
        val B by sources.add("B", "@rt(TYPE_USE) @interface B { int value(); }")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"@A @B(1) X"
        }
        sources.compile()
        assertEquals("@gen.A() @gen.B(value=1) ", types["@A @B(1) X"].annotations.annotationString())
    }

    @Test
    fun `'annotationString' of a type with an annotation with a class parameter should use the fully qualified class name`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { Class value(); }")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"@A(X.class) X"
        }
        sources.compile()
        assertEquals("@gen.A(value=class gen.X) ", types["@A(X.class) X"].annotations.annotationString())
    }
}
