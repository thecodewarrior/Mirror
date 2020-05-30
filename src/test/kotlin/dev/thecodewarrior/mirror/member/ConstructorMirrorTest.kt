package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.type.ClassMirror
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

@Suppress("LocalVariableName")
internal class ConstructorMirrorTest: MTest() {

    @Test
    fun `'name' of a class's constructor should be the class's binary name`() {
        val X by sources.add("X", "class X { public X() {} }")
        sources.compile()
        val constructor = Mirror.reflect(X.getConstructor())
        assertEquals("gen.X", constructor.name)
    }

    @Test
    fun `'name' of an inner class's constructor should be the class's binary name`() {
        val X by sources.add("X", "class X { class XX { public XX() {} } }")
        sources.compile()
        val constructor = Mirror.reflect(X._class("XX")._constructor())
        assertEquals("gen.X\$XX", constructor.name)
    }

    @Test
    fun `'isVarArgs' for a non-variadic constructor should return false`() {
        val X by sources.add("X", "class X { public X(int x) {} }")
        sources.compile()
        assertFalse(Mirror.reflect(X._constructor()).isVarArgs)
    }

    @Test
    fun `'isVarArgs' for a variadic constructor should return true`() {
        val X by sources.add("X", "class X { public X(int... x) {} }")
        sources.compile()
        assertTrue(Mirror.reflect(X._constructor()).isVarArgs)
    }

    @Test
    fun `'isSynthetic' for a non-synthetic constructor should return false`() {
        val X by sources.add("X", "class X { public X() {} }")
        sources.compile()
        assertFalse(Mirror.reflect(X._constructor()).isSynthetic)
    }

    @Test
    fun `'isSynthetic' for a synthetic constructor should return false`() {
        val X by sources.add("X", """
            class X {
                private Nested outsideAccess = new Nested();
                
                class Nested {
                    private Nested() {}
                }
            }
        """)
        sources.compile()
        val constructor = X._class("Nested").declaredConstructors
            .first { !JvmModifier.isPrivate(it.modifiers) }
        assertTrue(Mirror.reflect(constructor).isSynthetic)
    }

    @Test
    fun `'access' for constructors should be correct`() {
        val X by sources.add("X", "public class X { public X() {} }")
        val Y by sources.add("Y", "public class Y { protected Y() {} }")
        val Z by sources.add("Z", "public class Z { Z() {} }")
        val W by sources.add("W", "public class W { private W() {} }")
        sources.compile()
        assertAll(
            { assertEquals(Modifier.Access.PUBLIC, Mirror.reflect(X._constructor()).access) },
            { assertEquals(Modifier.Access.PROTECTED, Mirror.reflect(Y._constructor()).access) },
            { assertEquals(Modifier.Access.DEFAULT, Mirror.reflect(Z._constructor()).access) },
            { assertEquals(Modifier.Access.PRIVATE, Mirror.reflect(W._constructor()).access) }
        )
    }

    class `internal access test case` internal constructor()

    @Test
    fun `'access' for a Kotlin 'internal' constructor should be public and 'isInternalAccess'`() {
        assertEquals(Modifier.Access.PUBLIC, Mirror.reflect(::`internal access test case`.c).access)
        assertTrue(Mirror.reflect(::`internal access test case`.c).isInternalAccess)
    }

    @Test
    fun `'KCallable' for a Kotlin constructor should be correct`() {
        class X { }
        val constructor = X::class.java.declaredConstructors.single()
        assertEquals(::X, Mirror.reflect(constructor).kCallable)
    }

    @Test
    fun `'KCallable' for a Java constructor should be correct`() {
        val X by sources.add("X", "class X {}").typed<Any>()
        sources.compile()
        val constructor = X.declaredConstructors.single()
        assertEquals(X.kotlin.constructors.single(), Mirror.reflect(constructor).kCallable)
    }

    @Test
    fun `'KCallable' for a synthetic constructor should not exist`() {
        val X by sources.add("X", """
            class X {
                private Nested outsideAccess = new Nested();
                
                class Nested {
                    private Nested() {}
                }
            }
        """)
        sources.compile()
        val constructor = X._class("Nested").declaredConstructors
            .first { !JvmModifier.isPrivate(it.modifiers) }
        assertNull(Mirror.reflect(constructor).kCallable)
    }

    @Test
    fun `'modifiers' for constructors should be correct`() {
        val X by sources.add("X", "public class X { public X() {} }")
        val Y by sources.add("Y", "public class Y { protected Y() {} }")
        val Z by sources.add("Z", "public class Z { Z() {} }")
        val W by sources.add("W", "public class W { private W() {} }")
        sources.compile()
        assertAll(
            { assertEquals(setOf(Modifier.PUBLIC), Mirror.reflect(X._constructor()).modifiers) },
            { assertEquals(setOf(Modifier.PROTECTED), Mirror.reflect(Y._constructor()).modifiers) },
            { assertEquals(setOf<Any>(), Mirror.reflect(Z._constructor()).modifiers) },
            { assertEquals(setOf(Modifier.PRIVATE), Mirror.reflect(W._constructor()).modifiers) }
        )
    }

    @Test
    fun `'modifiers' for Kotlin constructors should be correct`() {
        open class K {
            constructor(uniqueSignature: Byte) {}
            internal constructor(uniqueSignature: Short) {}
            protected constructor(uniqueSignature: Int) {}
            private constructor(uniqueSignature: Long) {}
        }

        fun getConstructor(type: ClassMirror) = K::class.java.getDeclaredConstructor(type.java)

        fun test(type: ClassMirror, vararg mods: Modifier) = assertEquals(
            setOf(*mods),
            Mirror.reflect(getConstructor(type)).modifiers
        )
        assertAll(
            { test(Mirror.types.byte, Modifier.PUBLIC)
                assertFalse(Mirror.reflect(getConstructor(Mirror.types.byte)).isInternalAccess) },
            { test(Mirror.types.short, Modifier.PUBLIC)
                assertTrue(Mirror.reflect(getConstructor(Mirror.types.short)).isInternalAccess) },
            { test(Mirror.types.int, Modifier.PROTECTED)
                assertFalse(Mirror.reflect(getConstructor(Mirror.types.int)).isInternalAccess) },
            { test(Mirror.types.long, Modifier.PRIVATE)
                assertFalse(Mirror.reflect(getConstructor(Mirror.types.long)).isInternalAccess) }
        )
    }

    @Test
    fun `'toString' for non-generic constructor should be correct`() {
        val X by sources.add("X", "public class X { public X(Y arg) {} }")
        val Y by sources.add("Y", "public class Y { }")
        sources.compile()
        assertEquals("public gen.X(gen.Y arg)", Mirror.reflect(X._constructor()).toString())
    }

    @Test
    fun `'toString' for generic constructor should place type parameters before the name`() {
        val X by sources.add("X", "public class X { public <T> X(T arg) {} }")
        val Y by sources.add("Y", "public class Y { }")
        sources.compile()
        assertEquals("public <T> gen.X(T arg)", Mirror.reflect(X._constructor()).toString())
    }

    @Test
    fun `'toString' for specialized generic constructor should place specialization after the name`() {
        val X by sources.add("X", "public class X { public <T> X(T arg) {} }")
        val Y by sources.add("Y", "public class Y { }")
        sources.compile()
        assertEquals("public gen.X<gen.Y>(gen.Y arg)", Mirror.reflect(X._constructor()).withTypeParameters(Mirror.reflect(Y)).toString())
    }
}