package dev.thecodewarrior.mirror.specialization

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.*
import dev.thecodewarrior.mirror.type.ClassMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@Suppress("LocalVariableName")
internal class EnclosingClassTest: MTest() {
    @Test
    fun `'enclosingClass' of a root class should return null`() {
        val X by sources.add("X", "class X {}")
        sources.compile()

        assertNull(Mirror.reflectClass(X).enclosingClass)
    }

    @Test
    fun `'enclosingClass' of an inner class should return the outer class`() {
        val Outer by sources.add("Outer", "class Outer { class Inner {} }")
        sources.compile()

        assertSame(Mirror.reflect(Outer), Mirror.reflectClass(Outer._class("Inner")).enclosingClass)
    }

    @Test
    fun `'enclosingClass' of a nested class should return the outer class`() {
        val Outer by sources.add("Outer", "class Outer { static class Nested {} interface INested {} }")
        sources.compile()

        assertAll(
            { assertSame(Mirror.reflect(Outer), Mirror.reflectClass(Outer._class("Nested")).enclosingClass) },
            { assertSame(Mirror.reflect(Outer), Mirror.reflectClass(Outer._class("INested")).enclosingClass) },
        )
    }

    @Test
    fun `'enclosingClass' of an inner class with a raw generic enclosing class should return the raw owner class`() {
        val Outer by sources.add("Outer", "class Outer<T> { class Inner {} }")
        sources.compile()

        assertSame(Mirror.reflect(Outer), Mirror.reflectClass(Outer._class("Inner")).enclosingClass)
    }

    @Test
    fun `'enclosingClass' of an inner class explicitly specialized with a generic enclosing class should return the generic enclosing class`() {
        val X by sources.add("X", "class X {}")
        val Outer by sources.add("Outer", "class Outer<T> { class Inner {} }")
        val types = sources.types {
            +"Outer<X>"
        }
        sources.compile()

        val outer = Mirror.reflectClass(types["Outer<X>"])
        val inner = Mirror.reflectClass(Outer._class("Inner"))

        assertSame(outer, inner.withEnclosingClass(outer).enclosingClass)
    }

    @Test
    fun `'enclosingClass' of a reflected type with a generic enclosing class should return the generic enclosing class`() {
        val X by sources.add("X", "class X {}")
        val Outer by sources.add("Outer", "class Outer<T> { class Inner {} }")
        val types = sources.types {
            +"Outer<X>"
            +"Outer<X>.Inner"
        }
        sources.compile()

        assertSame(Mirror.reflect(types["Outer<X>"]), Mirror.reflectClass(types["Outer<X>.Inner"]).enclosingClass)
    }

    @Test
    fun `'type' of a field in an inner class with a generic enclosing class should use the outer class's type variable`() {
        val Outer by sources.add("Outer", "class Outer<T> { class Inner { T field; } }")
        sources.compile()

        val outer = Mirror.reflectClass(Outer)
        val inner = Mirror.reflectClass(Outer._class("Inner"))
        val specialized = inner.getField("field")
        assertSame(outer.typeParameters[0], specialized.type)
    }

    @Test
    fun `'type' of a field in an inner class with a specialized generic enclosing class should use the specialized type`() {
        val X by sources.add("X", "class X {}")
        val Outer by sources.add("Outer", "class Outer<T> { class Inner { T field; } }")
        val types = sources.types {
            +"Outer<X>.Inner"
        }
        sources.compile()

        assertSame(Mirror.reflect(X), Mirror.reflectClass(types["Outer<X>.Inner"]).getField("field").type)
    }

    @Test
    fun `'returnType' of a method in an inner class with a generic enclosing class should use the outer class's type variable`() {
        val Outer by sources.add("Outer", "class Outer<T> { class Inner { T method() { NOP; } } }")
        sources.compile()

        val outer = Mirror.reflectClass(Outer)
        val inner = Mirror.reflectClass(Outer._class("Inner"))
        val specialized = inner.getMethod("method")
        assertSame(outer.typeParameters[0], specialized.returnType)
    }

    @Test
    fun `'returnType' of a method in an inner class with a specialized generic enclosing class should use the specialized type`() {
        val X by sources.add("X", "class X {}")
        val Outer by sources.add("Outer", "class Outer<T> { class Inner { T method() { NOP; } } }")
        val types = sources.types {
            +"Outer<X>.Inner"
        }
        sources.compile()

        assertSame(Mirror.reflect(X), Mirror.reflectClass(types["Outer<X>.Inner"]).getMethod("method").returnType)
    }

    @Test
    fun `'withEnclosingClass' on a root class should throw when the parameter is non-null`() {
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y {}")
        sources.compile()

        assertThrows<InvalidSpecializationException> {
            Mirror.reflectClass(X).withEnclosingClass(Mirror.reflectClass(Y))
        }
    }

    @Test
    fun `'withEnclosingClass' on a nested class should throw when the parameter not the raw enclosing class after stripping annotations`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A {}")
        val X by sources.add("X", "class X {}")
        val G by sources.add("G", "class G<T> { static class Nested {} }")
        val types = sources.types {
            +"G<X>"
            +"@A G"
            +"G.Nested"
        }
        sources.compile()

        assertAll(
            {
                // when it's the raw enclosing class, that's a no-op, so we shouldn't throw
                assertDoesNotThrow {
                    Mirror.reflectClass(types["G.Nested"]).withEnclosingClass(Mirror.reflectClass(G))
                }
            },
            {
                // if we try to use a specialized enclosing class, throw
                assertThrows<InvalidSpecializationException> {
                    Mirror.reflectClass(types["G.Nested"]).withEnclosingClass(Mirror.reflectClass(types["G<X>"]))
                }
            },
            {
                // if the raw enclosing class has annotations, that's fine, they get stripped anyway
                assertDoesNotThrow {
                    Mirror.reflectClass(types["G.Nested"]).withEnclosingClass(Mirror.reflectClass(types["@A G"]))
                }
            },
        )
    }


    @Test
    fun `'withEnclosingClass' on a generic class should not change the type specialization`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A {}")
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y {}")
        val G by sources.add("G", "class G<T> {}")
        val Outer by sources.add("Outer", "class Outer<T> { class G<V> {} }")
        val types = sources.types {
            +"G<X>"
            +"@A Outer<X>.G<X>"
            +"Outer<Y>"
            +"@A Outer<Y>.G<X>"
        }
        sources.compile()

        assertAll(
            {
                val root = Mirror.reflectClass(types["G<X>"])
                assertSame(root, root.withEnclosingClass(null))
            },
            {
                assertSame(
                    Mirror.reflectClass(types["@A Outer<Y>.G<X>"]),
                    Mirror.reflectClass(types["@A Outer<X>.G<X>"])
                        .withEnclosingClass(Mirror.reflectClass(types["Outer<Y>"]))
                )
            }
        )
    }

    @Test
    fun enclose_ofSpecializedInnerClass_whenPassedNull_shouldReturnSpecializedSelfWithoutSpecializedEnclosingClass() {
        val inner = Mirror.reflectClass<OuterGenericClass1<String>.InnerGenericClass<Object1>>()
        val withRawOuter = Mirror.reflectClass(OuterGenericClass1.InnerGenericClass::class.java).withTypeArguments(Mirror.reflectClass<Object1>())
        assertSame(withRawOuter, inner.withEnclosingClass(null))
    }

    @Test
    fun declaredClasses_ofLeafClass_shouldBeEmpty() {
        val clazz = Mirror.reflectClass(Object1::class.java)
        assertEquals(emptyList<ClassMirror>(), clazz.declaredMemberClasses)
    }

    @Test
    fun `'declaredMemberClasses' should return both inner and nested classes`() {
        val Outer by sources.add("Outer", "class Outer { class Inner {} static class Nested {} }")
        sources.compile()

        assertSameSet(
            listOf(
                Mirror.reflect(Outer._class("Inner")),
                Mirror.reflect(Outer._class("Nested")),
            ),
            Mirror.reflectClass(Outer).declaredMemberClasses
        )
    }

    @Test
    fun `specializing a class with an annotated enclosing type should strip the enclosing type annotations`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A {}")
        val X by sources.add("X", "class X {}")
        val Outer by sources.add("Outer", "class Outer<T> { class Inner {} }")
        val types = sources.types {
            +"Outer<X>"
            +"@A Outer<X>"
        }
        sources.compile()

        val inner = Mirror.reflectClass(Outer._class("Inner"))
        val specialized = inner.withEnclosingClass(Mirror.reflectClass(types["@A Outer<X>"]))
        assertSame(Mirror.reflect(types["Outer<X>"]), specialized.enclosingClass)
    }
}