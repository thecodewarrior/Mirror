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

    @Nested
    inner class Parameters: MTest() {
        @Test
        fun `'parameters' of a constructor with no parameters should return an empty list`() {
            val X by sources.add("X", "class X { public X() {} }")
            sources.compile()
            assertEquals(emptyList<Any>(), Mirror.reflect(X._constructor()).parameters)
        }

        @Test
        fun `'parameterTypes' of a constructor with no parameters should return an empty list`() {
            val X by sources.add("X", "class X { public X() {} }")
            sources.compile()
            assertEquals(emptyList<Any>(), Mirror.reflect(X._constructor()).parameterTypes)
        }

        @Test
        fun `'parameters' of a non-generic constructor should have the correct types`() {
            val X by sources.add("X", "class X { public X(String theString, int theInt) {} }")
            sources.compile()
            assertEquals(
                listOf(Mirror.reflect<String>(), Mirror.types.int),
                Mirror.reflect(X._constructor()).parameters.map { it.type }
            )
        }

        @Test
        fun `'parameterTypes' of a non-generic constructor should match the types of each element in 'parameters'`() {
            val X by sources.add("X", "class X { public X(String theString, int theInt) {} }")
            sources.compile()
            val constructor = Mirror.reflect(X._constructor())
            assertEquals(constructor.parameters.map { it.type }, constructor.parameterTypes)
        }

        @Test
        fun `'parameters' of a non-generic constructor in a generic specialized class should have the correct types`() {
            val X by sources.add("X", "class X<T> { public X(T theT, int theInt) {} }")
            val Y by sources.add("Y", "class Y {}")
            val types = sources.types {
                +"X<Y>"
            }
            sources.compile()
            assertEquals(
                listOf(Mirror.reflect(Y), Mirror.types.int),
                Mirror.reflectClass(types["X<Y>"]).declaredConstructors.single()
                    .parameters.map { it.type }
            )
        }

        @Test
        fun `'parameterTypes' of a non-generic constructor in a generic specialized class should match the types of each element in 'parameters'`() {
            val X by sources.add("X", "class X<T> { public X(T theT, int theInt) {} }")
            val Y by sources.add("Y", "class Y {}")
            val types = sources.types {
                +"X<Y>"
            }
            sources.compile()
            val constructor = Mirror.reflectClass(types["X<Y>"]).declaredConstructors.single()
            assertEquals(constructor.parameters.map { it.type }, constructor.parameterTypes)
        }

        @Test
        fun `'parameters' of a generic specialized constructor should have the correct types`() {
            val X by sources.add("X", "class X { public <T> X(T theT, int theInt) {} }")
            val Y by sources.add("Y", "class Y {}")
            sources.compile()
            val constructor = Mirror.reflectClass(X).declaredConstructors.single()
                .withTypeParameters(Mirror.reflect(Y))
            assertEquals(listOf(Mirror.reflect(Y), Mirror.types.int), constructor.parameters.map { it.type })
        }

        @Test
        fun `'parameterTypes' of a generic specialized constructor should match the types of each element in 'parameters'`() {
            val X by sources.add("X", "class X { public <T> X(T theT, int theInt) {} }")
            val Y by sources.add("Y", "class Y {}")
            sources.compile()
            val constructor = Mirror.reflectClass(X).declaredConstructors.single()
                .withTypeParameters(Mirror.reflect(Y))
            assertEquals(constructor.parameters.map { it.type }, constructor.parameterTypes)
        }


        @Test
        fun `'parameters' of a generic specialized constructor in a generic specialized class should have the correct types`() {
            val X by sources.add("X", "class X<A> { public <B> X(A theA, B theB, int theInt) {} }")
            val Y by sources.add("Y", "class Y {}")
            val Z by sources.add("Z", "class Z {}")
            val types = sources.types {
                +"X<Y>"
            }
            sources.compile()
            val constructor = Mirror.reflectClass(types["X<Y>"]).declaredConstructors.single()
                .withTypeParameters(Mirror.reflect(Z))
            assertEquals(listOf(Mirror.reflect(Y), Mirror.reflect(Z), Mirror.types.int), constructor.parameters.map { it.type })
        }

        @Test
        fun `'parameterTypes' of a generic specialized constructor in a generic specialized class should match the types of each element in 'parameters'`() {
            val X by sources.add("X", "class X<A> { public <B> X(A theA, B theB, int theInt) {} }")
            val Y by sources.add("Y", "class Y {}")
            val Z by sources.add("Z", "class Z {}")
            val types = sources.types {
                +"X<Y>"
            }
            sources.compile()
            val constructor = Mirror.reflectClass(types["X<Y>"]).declaredConstructors.single()
                .withTypeParameters(Mirror.reflect(Z))
            assertEquals(constructor.parameters.map { it.type }, constructor.parameterTypes)
        }

        @Test
        fun `'parameters' of a constructor compiled with parameter names should have the correct names`() {
            val X by sources.add("X", "class X { public X(String theString, int theInt) {} }")
            sources.compile()
            assertEquals(
                listOf("theString", "theInt"),
                Mirror.reflect(X._constructor()).parameters.map { it.name }
            )
        }

        @Test
        fun `'parameters' of a constructor compiled without parameter names should have null names`() {
            val X by sources.add("X", "class X { public X(String theString, int theInt) {} }")
            sources.options.remove("-parameters")
            sources.compile()
            assertEquals(
                listOf(null, null),
                Mirror.reflect(X._constructor()).parameters.map { it.name }
            )
        }

        @Test
        fun `'parameters' of a constructor should correspond to the correct 'java' parameter objects`() {
            val X by sources.add("X", "class X { public X(String theString, int theInt) {} }")
            sources.compile()
            assertEquals(
                X._constructor().parameters.toList(),
                Mirror.reflect(X._constructor()).parameters.map { it.java }
            )
        }

        @Test
        fun `'annotations' of an non-annotated parameter should return an empty list`() {
            val X by sources.add("X", "class X { public X(int param) {} }")
            sources.compile()
            assertArrayEquals(
                emptyArray<Any>(),
                Mirror.reflect(X._constructor()).parameters.single().annotations
            )
        }

        @Test
        fun `'annotations' of an annotated parameter should return the annotations in declaration order`() {
            val A by sources.add("A", "@Target(ElementType.PARAMETER) @Retention(RetentionPolicy.RUNTIME) @interface A {}").typed<Annotation>()
            val A2 by sources.add("A2", "@Target(ElementType.PARAMETER) @Retention(RetentionPolicy.RUNTIME) @interface A2 { String value(); }").typed<Annotation>()
            val X by sources.add("X", "class X { public X(@A @A2(\"annotation value\") int param) {} }")
            sources.compile()
            assertArrayEquals(
                arrayOf(Mirror.newAnnotation(A), Mirror.newAnnotation(A2, "value" to "annotation value")),
                Mirror.reflect(X._constructor()).parameters.single().annotations
            )
        }

        @Test
        fun `'annotations' of a parameter with an annotated type should return an empty list`() {
            val A by sources.add("A", "@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.RUNTIME) @interface A {}").typed<Annotation>()
            val A2 by sources.add("A2", "@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.RUNTIME) @interface A2 { String value(); }").typed<Annotation>()
            val X by sources.add("X", "class X { public X(@A @A2(\"annotation value\") int param) {} }")
            sources.compile()
            assertArrayEquals(
                arrayOf<Any>(),
                Mirror.reflect(X._constructor()).parameters.single().annotations
            )
        }

        @Test
        fun `'isFinal' of a final parameter should be true`() {
            val X by sources.add("X", "class X { public X(final int param) {} }")
            sources.compile()
            assertTrue(Mirror.reflect(X._constructor()).parameters.single().isFinal)
        }

        @Test
        fun `'isFinal' of a non-final parameter should be false`() {
            val X by sources.add("X", "class X { public X(int param) {} }")
            sources.compile()
            assertFalse(Mirror.reflect(X._constructor()).parameters.single().isFinal)
        }
    }

    @Test
    fun `'exceptionTypes' of a constructor with no thrown exceptions should return an empty list`() {
        val X by sources.add("X", "class X { public X() {} }")
        sources.compile()
        assertEquals(
            emptyList<Any>(),
            Mirror.reflect(X._constructor()).exceptionTypes
        )
    }

    @Test
    fun `'exceptionTypes' of a constructor with multiple thrown exceptions should return the thrown types in declaration order`() {
        val ExceptionA by sources.add("ExceptionA", "class ExceptionA extends Exception {}")
        val ExceptionB by sources.add("ExceptionB", "class ExceptionB extends Exception {}")
        val X by sources.add("X", "class X { public X() throws ExceptionB, ExceptionA {} }")
        sources.compile()
        assertEquals(
            listOf(Mirror.reflect(ExceptionB), Mirror.reflect(ExceptionA)),
            Mirror.reflect(X._constructor()).exceptionTypes
        )
    }

    @Test
    fun `'typeParameters' of a constructor with no type parameters should return an empty list`() {
        val X by sources.add("X", "class X { public X() {} }")
        sources.compile()
        assertEquals(
            emptyList<Any>(),
            Mirror.reflect(X._constructor()).typeParameters
        )
    }

    @Test
    fun `'typeParameters' of a constructor with type parameters should return a list of those type parameters`() {
        val X by sources.add("X", "class X { public <B, A> X() {} }")
        sources.compile()
        assertEquals(
            X._constructor().typeParameters.map { Mirror.reflect(it) },
            Mirror.reflect(X._constructor()).typeParameters
        )
    }

    @Test
    fun `'annotations' of a constructor with no annotations should return an empty list`() {
        val X by sources.add("X", "class X { public X() {} }")
        sources.compile()
        assertEquals(
            emptyList<Any>(),
            Mirror.reflect(X._constructor()).annotations
        )
    }

    @Test
    fun `'annotations' of a constructor with annotations should return an array of annotations`() {
        val A by sources.add("A", "@Target(ElementType.CONSTRUCTOR) @Retention(RetentionPolicy.RUNTIME) @interface A {}").typed<Annotation>()
        val A2 by sources.add("A2", "@Target(ElementType.CONSTRUCTOR) @Retention(RetentionPolicy.RUNTIME) @interface A2 { String value(); }").typed<Annotation>()
        val X by sources.add("X", "class X { @A @A2(\"annotation value\") public X() {} }")
        sources.compile()
        assertEquals(
            listOf(Mirror.newAnnotation(A), Mirror.newAnnotation(A2, "value" to "annotation value")),
            Mirror.reflect(X._constructor()).annotations
        )
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
        """.trimIndent())
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

    class Foo internal constructor()

    @Test
    fun `'access' for a Kotlin 'internal' constructor should be public and 'isInternalAccess'`() {
        assertEquals(Modifier.Access.PUBLIC, Mirror.reflect(::Foo.c).access)
        assertTrue(Mirror.reflect(::Foo.c).isInternalAccess)
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
        """.trimIndent())
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