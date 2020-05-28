package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.annotations.Annotation1
import dev.thecodewarrior.mirror.annotations.AnnotationArg1
import dev.thecodewarrior.mirror.testsupport.FieldFlagTestClass
import dev.thecodewarrior.mirror.testsupport.FieldVisibilityTestClass
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.assertSetEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.lang.reflect.Field

@Suppress("LocalVariableName")
internal class FieldMirrorTest: MTest() {
    @Test
    fun `a field should have the correct name`() {
        val X by sources.add("X", "class X { int field; }")
        sources.compile()
        assertEquals("field", Mirror.reflect(X._f("field")).name)
    }

    @Test
    fun `a non-generic field should have the correct type`() {
        val X by sources.add("X", "class X { Y field; }")
        val Y by sources.add("Y", "class Y {}")
        sources.compile()
        assertEquals(Mirror.reflect(Y), Mirror.reflect(X._f("field")).type)
    }

    @Test
    fun `a generic field should have the correct raw type`() {
        val X by sources.add("X", "class X<T> { T field; }")
        sources.compile()
        assertEquals(Mirror.reflect(X.typeParameters.single()), Mirror.reflect(X._f("field")).type)
    }

    @Test
    fun `a specialized generic field should have the correct specialized type`() {
        val X by sources.add("X", "class X<T> { T field; }")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"X<Y>"
        }
        sources.compile()
        assertEquals(
            Mirror.reflect(Y),
            Mirror.reflect(X._f("field"))
                .withDeclaringClass(Mirror.reflectClass(types["X<Y>"])).type
        )
    }

    @Test
    fun `'isEnumConstant' for a non-enum field should return false`() {
        val X by sources.add("X", "class X { int field; }")
        sources.compile()
        assertFalse(Mirror.reflect(X._f("field")).isEnumConstant)
    }

    @Test
    fun `'isEnumConstant' for an enum constant field should return true`() {
        val X by sources.add("X", "enum X { V; }")
        sources.compile()
        assertTrue(Mirror.reflect(X._f("V")).isEnumConstant)
    }

    @Test
    fun `'isEnumConstant' for a field in an enum which is not an enum constant should return false`() {
        val X by sources.add("X", "enum X { V; int field = 1; }")
        sources.compile()
        assertFalse(Mirror.reflect(X._f("field")).isEnumConstant)
    }

    @Test
    fun `'access' for fields should be correct`() {
        val X by sources.add("X", """
            public class X {
                public int _public;
                protected int _protected;
                int _default;
                private int _private;
            }
        """)
        sources.compile()
        assertAll(
            { assertEquals(Modifier.Access.PUBLIC, Mirror.reflect(X._f("_public")).access) },
            { assertEquals(Modifier.Access.PROTECTED, Mirror.reflect(X._f("_protected")).access) },
            { assertEquals(Modifier.Access.DEFAULT, Mirror.reflect(X._f("_default")).access) },
            { assertEquals(Modifier.Access.PRIVATE, Mirror.reflect(X._f("_private")).access) }
        )
    }

    @Test
    fun `'modifiers' for fields should be correct`() {
        val X by sources.add("X", """
            class X {
                public int _public;
                int _default;
                protected int _protected;
                private int _private;
                static int _static;
                final int _final = 1;
                transient int _transient;
                volatile int _volatile;
            }
        """)
        sources.compile()
        fun test(method: Field, vararg mods: Modifier) = assertEquals(setOf(*mods), Mirror.reflect(method).modifiers)
        assertAll(
            { test(X._f("_public"), Modifier.PUBLIC) },
            { test(X._f("_default")) },
            { test(X._f("_protected"), Modifier.PROTECTED) },
            { test(X._f("_private"), Modifier.PRIVATE) },
            { test(X._f("_static"), Modifier.STATIC) },
            { test(X._f("_final"), Modifier.FINAL) },
            { test(X._f("_transient"), Modifier.TRANSIENT) },
            { test(X._f("_volatile"), Modifier.VOLATILE) }
        )
    }

    @Test
    fun `modifier helpers for fields should be correct`() {
        val X by sources.add("X", """
            class X {
                public int _public;
                int _default;
                protected int _protected;
                private int _private;
                static int _static;
                final int _final = 1;
                transient int _transient;
                volatile int _volatile;
            }
        """)
        sources.compile()
        assertAll(
            { assertTrue(Mirror.reflect(X._f("_public")).isPublic) },
            { assertTrue(Mirror.reflect(X._f("_default")).isPackagePrivate) },
            { assertTrue(Mirror.reflect(X._f("_protected")).isProtected) },
            { assertTrue(Mirror.reflect(X._f("_private")).isPrivate) },
            { assertTrue(Mirror.reflect(X._f("_static")).isStatic) },
            { assertTrue(Mirror.reflect(X._f("_final")).isFinal) },
            { assertTrue(Mirror.reflect(X._f("_transient")).isTransient) },
            { assertTrue(Mirror.reflect(X._f("_volatile")).isVolatile) }
        )
    }

    @Test
    fun `'isSynthetic' for a non-synthetic field should return false`() {
        val X by sources.add("X", "class X { int field; }")
        sources.compile()
        assertFalse(Mirror.reflect(X._f("field")).isSynthetic)
    }

    @Test
    fun `'isSynthetic' for a synthetic field should return true`() {
        val X by sources.add("X", "class X { class Y { } }")
        sources.compile()
        assertTrue(Mirror.reflect(X._class("Y")._f("this$0")).isSynthetic)
    }

    @Test
    fun `'annotations' for a non-annotated field should return an empty list`() {
        val X by sources.add("X", "class X { int field; }")
        sources.compile()
        val field = Mirror.reflect(X._f("field"))
        assertEquals(emptyList(), field.annotations)
    }

    @Test
    fun `'annotations' for an annotated field should return a list of the annotations`() {
        val A by sources.add("A", " @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface A {}").typed<Annotation>()
        val X by sources.add("X", "class X { @A int field; }")
        sources.compile()
        val field = Mirror.reflect(X._f("field"))
        assertEquals(listOf(Mirror.newAnnotation(A)), field.annotations)
    }

    @Test
    fun `'annotations' for a field with a type annotation should return an empty list`() {
        val A by sources.add("A", " @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE_USE) @interface A {}").typed<Annotation>()
        val X by sources.add("X", "class X { @A int field; }")
        sources.compile()
        val field = Mirror.reflect(X._f("field"))
        assertEquals(emptyList(), field.annotations)
    }

    @Test
    fun `specializing a class should specialize its fields`() {
        val X by sources.add("X", "class X<T> { T field; }")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"X<Y>"
        }
        sources.compile()
        assertEquals(Mirror.reflect(Y), Mirror.reflectClass(types["X<Y>"]).getField("field").type)
    }

    @Test
    fun `specializing a field with its raw enclosing type should return the raw mirror`() {
        val X by sources.add("X", "class X<T> { T field; }")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"X<Y>"
        }
        sources.compile()
        val field = Mirror.reflectClass(types["X<Y>"]).getField("field")
        assertSame(field.raw, field.withDeclaringClass(Mirror.reflectClass(X)))
    }

    @Test
    fun `'toString' for a field should have modifiers and qualified type and field names`() {
        val X by sources.add("X", "class X { private Y field; }")
        val Y by sources.add("Y", "class Y {}")
        sources.compile()
        assertEquals("private gen.Y gen.X.field", Mirror.reflect(X._f("field")).toString())
    }

    @Test
    fun `'toString' for a field in an anonymous class should use the dot-separated binary class name`() {
        val X by sources.add("X", "class X { static Class type = new Y() { private Y field; }.getClass(); }")
        val Y by sources.add("Y", "interface Y {}")
        sources.compile()
        val field = X._f("type")._get<Class<*>>(null)._f("field")
        assertEquals("private gen.Y gen.X$1.field", Mirror.reflect(field).toString())
    }
}