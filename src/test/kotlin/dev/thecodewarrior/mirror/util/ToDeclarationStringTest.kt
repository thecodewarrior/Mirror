package dev.thecodewarrior.mirror.util

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import dev.thecodewarrior.mirror.typeToken
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ToDeclarationStringTest: MTest() {

    //@Test
    fun `'toString' for non-generic constructor should be correct`() {
        val X by sources.add("X", "public class X { public X(Y arg) {} }")
        val Y by sources.add("Y", "public class Y { }")
        sources.compile()
        assertEquals("public gen.X(gen.Y arg)", Mirror.reflect(X._constructor()).toString())
    }

    //@Test
    fun `'toString' for generic constructor should place type parameters before the name`() {
        val X by sources.add("X", "public class X { public <T> X(T arg) {} }")
        val Y by sources.add("Y", "public class Y { }")
        sources.compile()
        assertEquals("public <T> gen.X(T arg)", Mirror.reflect(X._constructor()).toString())
    }

    //@Test
    fun `'toString' for specialized generic constructor should place specialization after the name`() {
        val X by sources.add("X", "public class X { public <T> X(T arg) {} }")
        val Y by sources.add("Y", "public class Y { }")
        sources.compile()
        assertEquals("public gen.X<gen.Y>(gen.Y arg)", Mirror.reflect(X._constructor()).withTypeParameters(Mirror.reflect(Y)).toString())
    }

    //@Test
    fun `'toString' for non-generic method should be correct`() {
        val X by sources.add("X", "public class X { public void method(Y arg) {} }")
        val Y by sources.add("Y", "public class Y { }")
        sources.compile()
        assertEquals("public void gen.X.method(gen.Y arg)", Mirror.reflect(X._m("method")).toString())
    }

    //@Test
    fun `'toString' for generic method should place type parameters before the name`() {
        val X by sources.add("X", "public class X { public <T> void method(T arg) {} }")
        val Y by sources.add("Y", "public class Y { }")
        sources.compile()
        assertEquals("public <T> void gen.X.method(T arg)", Mirror.reflect(X._m("method")).toString())
    }

    //@Test
    fun `'toString' for specialized generic method should place specialization after the name`() {
        val X by sources.add("X", "public class X { public <T> void method(T arg) {} }")
        val Y by sources.add("Y", "public class Y { }")
        sources.compile()
        assertEquals("public void gen.X.method<gen.Y>(gen.Y arg)", Mirror.reflect(X._m("method")).withTypeParameters(Mirror.reflect(Y)).toString())
    }

    //@Test
    fun `'toString' for a method in an anonymous class should use the dot-separated binary class name`() {
        val X by sources.add("X", "class X { static Class type = new Y() { private void method() {} }.getClass(); }")
        val Y by sources.add("Y", "interface Y {}")
        sources.compile()
        val method = X._f("type")._get<Class<*>>(null)._m("method")
        assertEquals("private void gen.X$1.method()", Mirror.reflect(method).toString())
    }

    //@Test
    fun `'toString' of a plain parameter should be correct`() {
        val X by sources.add("X", "class X { public void method(int param) {} }")
        sources.compile()
        val param = Mirror.reflect(X._m("method")).parameters.single()
        assertEquals("int param", param.java.toString())
        assertEquals("int param", param.toString())
    }

    //@Test
    fun `'toString' of a final parameter should be correct`() {
        val X by sources.add("X", "class X { public void method(final int param) {} }")
        sources.compile()
        val param = Mirror.reflect(X._m("method")).parameters.single()
        assertEquals("final int param", param.java.toString())
        assertEquals("final int param", param.toString())
    }

    //@Test
    fun `'toString' of a vararg parameter should be correct`() {
        val X by sources.add("X", "class X { public void method(int... param) {} }")
        sources.compile()
        val param = Mirror.reflect(X._m("method")).parameters.single()
        assertEquals("int... param", param.java.toString())
        assertEquals("int... param", param.toString())
    }

    //@Test
    fun `'toString' of a parameter without a name should be correct`() {
        val X by sources.add("X", "class X { public void method(int param) {} }")
        sources.options.remove("-parameters")
        sources.compile()
        val param = Mirror.reflect(X._m("method")).parameters.single()
        assertEquals("int arg0", param.java.toString())
        assertEquals("int arg0", param.toString())
    }


    //@Test
    fun `'toString' for a field should have modifiers and qualified type and field names`() {
        val X by sources.add("X", "class X { private Y field; }")
        val Y by sources.add("Y", "class Y {}")
        sources.compile()
        assertEquals("private gen.Y gen.X.field", Mirror.reflect(X._f("field")).toString())
    }

    //@Test
    fun `'toString' for a field in an anonymous class should use the dot-separated binary class name`() {
        val X by sources.add("X", "class X { static Class type = new Y() { private Y field; }.getClass(); }")
        val Y by sources.add("Y", "interface Y {}")
        sources.compile()
        val field = X._f("type")._get<Class<*>>(null)._f("field")
        assertEquals("private gen.Y gen.X$1.field", Mirror.reflect(field).toString())
    }

    //@Test
    fun `'toString' on a self-referential class should not infinitely recurse`() {
        val sources = TestSources()
        val G by sources.add("G", "class G<T> {}")
        val X by sources.add("X", "class X extends G<X> {}")
        sources.compile()
        assertDoesNotThrow {
            X.toString()
        }
    }
}