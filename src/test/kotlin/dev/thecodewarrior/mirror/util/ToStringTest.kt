package dev.thecodewarrior.mirror.util

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import dev.thecodewarrior.mirror.typeToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

@Suppress("LocalVariableName")
internal class ToStringTest: MTest() {
    @Test
    fun `'toJavaString' of plain classes should be correct`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { int value() default 10; }")
        val I by sources.add("I", "interface I {}")
        val X by sources.add("X", "class X {}")
        val Outer by sources.add("Outer", "class Outer { static class Static {} class Inner {} }")
        val types = sources.types {
            +"@A X"
            +"@A(11) X"
        }
        sources.compile()

        assertAll(
            { assertEquals("gen.A", Mirror.reflect(A).toJavaString()) },
            { assertEquals("gen.I", Mirror.reflect(I).toJavaString()) },
            { assertEquals("gen.X", Mirror.reflect(X).toJavaString()) },
            { assertEquals("gen.Outer.Static", Mirror.reflect(Outer._class("Static")).toJavaString()) },
            { assertEquals("gen.Outer.Inner", Mirror.reflect(Outer._class("Inner")).toJavaString()) },
            { assertEquals("@gen.A(10) gen.X", Mirror.reflect(types["@A X"]).toJavaString()) },
            { assertEquals("@gen.A(11) gen.X", Mirror.reflect(types["@A(11) X"]).toJavaString()) },
        )
    }

    @Test
    fun `'toJavaString' of generic types should be correct`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A {}").typed<Annotation>()
        val X by sources.add("X", "class X {}")
        val G by sources.add("G", "class G<T> {}")
        val Outer by sources.add("Outer", "class Outer<A> { static class Static<B> {} class Inner<B> {} }")
        val types = sources.types {
            +"G<?>"
            +"G<X>"
            +"G<G<X>>"
            +"Outer.Static<X>"
            +"Outer.Inner"
            +"Outer<X>.Inner<X>"
            +"@A Outer"
        }
        sources.compile()

        assertAll(
            { assertEquals("gen.G<?>", Mirror.reflect(types["G<?>"]).toJavaString()) },
            { assertEquals("gen.G<gen.X>", Mirror.reflect(types["G<X>"]).toJavaString()) },
            { assertEquals("gen.G<gen.G<gen.X>>", Mirror.reflect(types["G<G<X>>"]).toJavaString()) },
            { assertEquals("gen.Outer.Static<gen.X>", Mirror.reflect(types["Outer.Static<X>"]).toJavaString()) },
            { assertEquals("gen.Outer.Inner", Mirror.reflect(types["Outer.Inner"]).toJavaString()) },
            { assertEquals("gen.Outer<gen.X>.Inner<gen.X>", Mirror.reflect(types["Outer<X>.Inner<X>"]).toJavaString()) },
            {
                // partial specialization. This isn't valid java, but mirror can model it.
                val owner = Mirror.reflectClass(Outer).withTypeArguments(Mirror.reflect(X))
                val nested = owner.getDeclaredMemberClass("Inner")
                assertEquals("gen.Outer<gen.X>.Inner", nested.toJavaString())
            },
            {
                // partial specialization. This isn't valid java, but mirror can model it.
                val owner = Mirror.reflectClass(Outer)
                val nested = owner.getDeclaredMemberClass("Inner").withTypeArguments(Mirror.reflect(X))
                assertEquals("gen.Outer.Inner<gen.X>", nested.toJavaString())
            },
            {
                // weird partial annotation
                val owner = Mirror.reflectClass(types["@A Outer"])
                val nested = owner.getDeclaredMemberClass("Inner")
                assertEquals("gen.Outer.Inner", nested.toJavaString())
            },
        )
    }

    @Test
    fun `'toJavaString' of array types should be correct`() {
        val X by sources.add("X", "class X {}")
        val G by sources.add("G", "class G<T> {}")
        val types = sources.types {
            +"X[]"
            +"X[][]"
            +"G[]"
            +"G<X>[]"
            +"G<X[]>"
        }
        sources.compile()

        assertAll(
            { assertEquals("gen.X[]", Mirror.reflect(types["X[]"]).toJavaString()) },
            { assertEquals("gen.X[][]", Mirror.reflect(types["X[][]"]).toJavaString()) },
            { assertEquals("gen.G[]", Mirror.reflect(types["G[]"]).toJavaString()) },
            { assertEquals("gen.G<gen.X>[]", Mirror.reflect(types["G<X>[]"]).toJavaString()) },
            { assertEquals("gen.G<gen.X[]>", Mirror.reflect(types["G<X[]>"]).toJavaString()) },
        )
    }

    @Test
    fun `toJavaString of wildcard types should be correct`() {
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"? extends X"
            +"? super X"
            +"?"
        }
        sources.compile()

        assertAll(
            { assertEquals("? extends gen.X", Mirror.reflect(types["? extends X"]).toJavaString()) },
            { assertEquals("? super gen.X", Mirror.reflect(types["? super X"]).toJavaString()) },
            { assertEquals("?", Mirror.reflect(types["?"]).toJavaString()) },
        )
    }

    @Test
    fun `toJavaString of type variables should be correct`() {
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            block("T") {}
            block("T extends X") {}
        }
        sources.compile()

        assertAll(
            { assertEquals("T", Mirror.reflect(types["T"]).toJavaString()) },
            // the extends clause is part of the declaration, not type use
            { assertEquals("T", Mirror.reflect(types["T extends X"]).toJavaString()) },
        )
    }


}