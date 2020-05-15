package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.lang.reflect.Method
import kotlin.reflect.full.functions

@Suppress("LocalVariableName")
internal class MethodMirrorTest : MTest() {

    @Test
    fun `'name' of a method should be correct`() {
        val X by sources.add("X", "class X { public void method() {} }")
        sources.compile()
        assertEquals("method", Mirror.reflect(X._m("method")).name)
    }

    @Test
    fun `'isVarArgs' for a non-variadic method should return false`() {
        val X by sources.add("X", "class X { public void method(int x) {} }")
        sources.compile()
        assertFalse(Mirror.reflect(X._m("method")).isVarArgs)
    }

    @Test
    fun `'isVarArgs' for a variadic method should return true`() {
        val X by sources.add("X", "class X { public void method(int... x) {} }")
        sources.compile()
        assertTrue(Mirror.reflect(X._m("method")).isVarArgs)
    }

    @Nested
    inner class Parameters: MTest() {
        @Test
        fun `'parameters' of a method with no parameters should return an empty list`() {
            val X by sources.add("X", "class X { public void method() {} }")
            sources.compile()
            assertEquals(emptyList<Any>(), Mirror.reflect(X._m("method")).parameters)
        }

        @Test
        fun `'parameterTypes' of a method with no parameters should return an empty list`() {
            val X by sources.add("X", "class X { public void method() {} }")
            sources.compile()
            assertEquals(emptyList<Any>(), Mirror.reflect(X._m("method")).parameterTypes)
        }

        @Test
        fun `'parameters' of a non-generic method should have the correct types`() {
            val X by sources.add("X", "class X { public void method(String theString, int theInt) {} }")
            sources.compile()
            assertEquals(
                listOf(Mirror.reflect<String>(), Mirror.types.int),
                Mirror.reflect(X._m("method")).parameters.map { it.type }
            )
        }

        @Test
        fun `'parameterTypes' of a non-generic method should match the types of each element in 'parameters'`() {
            val X by sources.add("X", "class X { public void method(String theString, int theInt) {} }")
            sources.compile()
            val method = Mirror.reflect(X._m("method"))
            assertEquals(method.parameters.map { it.type }, method.parameterTypes)
        }

        @Test
        fun `'parameters' of a non-generic method in a generic specialized class should have the correct types`() {
            val X by sources.add("X", "class X<T> { public void method(T theT, int theInt) {} }")
            val Y by sources.add("Y", "class Y {}")
            val types = sources.types {
                +"X<Y>"
            }
            sources.compile()
            assertEquals(
                listOf(Mirror.reflect(Y), Mirror.types.int),
                Mirror.reflectClass(types["X<Y>"]).declaredMethods.single()
                    .parameters.map { it.type }
            )
        }

        @Test
        fun `'parameterTypes' of a non-generic method in a generic specialized class should match the types of each element in 'parameters'`() {
            val X by sources.add("X", "class X<T> { public void method(T theT, int theInt) {} }")
            val Y by sources.add("Y", "class Y {}")
            val types = sources.types {
                +"X<Y>"
            }
            sources.compile()
            val method = Mirror.reflectClass(types["X<Y>"]).declaredMethods.single()
            assertEquals(method.parameters.map { it.type }, method.parameterTypes)
        }

        @Test
        fun `'parameters' of a generic specialized method should have the correct types`() {
            val X by sources.add("X", "class X { public <T> void method(T theT, int theInt) {} }")
            val Y by sources.add("Y", "class Y {}")
            sources.compile()
            val method = Mirror.reflectClass(X).declaredMethods.single()
                .withTypeParameters(Mirror.reflect(Y))
            assertEquals(listOf(Mirror.reflect(Y), Mirror.types.int), method.parameters.map { it.type })
        }

        @Test
        fun `'parameterTypes' of a generic specialized method should match the types of each element in 'parameters'`() {
            val X by sources.add("X", "class X { public <T> void method(T theT, int theInt) {} }")
            val Y by sources.add("Y", "class Y {}")
            sources.compile()
            val method = Mirror.reflectClass(X).declaredMethods.single()
                .withTypeParameters(Mirror.reflect(Y))
            assertEquals(method.parameters.map { it.type }, method.parameterTypes)
        }


        @Test
        fun `'parameters' of a generic specialized method in a generic specialized class should have the correct types`() {
            val X by sources.add("X", "class X<A> { public <B> void method(A theA, B theB, int theInt) {} }")
            val Y by sources.add("Y", "class Y {}")
            val Z by sources.add("Z", "class Z {}")
            val types = sources.types {
                +"X<Y>"
            }
            sources.compile()
            val method = Mirror.reflectClass(types["X<Y>"]).declaredMethods.single()
                .withTypeParameters(Mirror.reflect(Z))
            assertEquals(listOf(Mirror.reflect(Y), Mirror.reflect(Z), Mirror.types.int), method.parameters.map { it.type })
        }

        @Test
        fun `'parameterTypes' of a generic specialized method in a generic specialized class should match the types of each element in 'parameters'`() {
            val X by sources.add("X", "class X<A> { public <B> void method(A theA, B theB, int theInt) {} }")
            val Y by sources.add("Y", "class Y {}")
            val Z by sources.add("Z", "class Z {}")
            val types = sources.types {
                +"X<Y>"
            }
            sources.compile()
            val method = Mirror.reflectClass(types["X<Y>"]).declaredMethods.single()
                .withTypeParameters(Mirror.reflect(Z))
            assertEquals(method.parameters.map { it.type }, method.parameterTypes)
        }

        @Test
        fun `'parameters' of a method compiled with parameter names should have the correct names`() {
            val X by sources.add("X", "class X { public void method(String theString, int theInt) {} }")
            sources.compile()
            assertEquals(
                listOf("theString", "theInt"),
                Mirror.reflect(X._m("method")).parameters.map { it.name }
            )
        }

        @Test
        fun `'parameters' of a method compiled without parameter names should have null names`() {
            val X by sources.add("X", "class X { public void method(String theString, int theInt) {} }")
            sources.options.remove("-parameters")
            sources.compile()
            assertEquals(
                listOf(null, null),
                Mirror.reflect(X._m("method")).parameters.map { it.name }
            )
        }

        @Test
        fun `'parameters' of a method should correspond to the correct 'java' parameter objects`() {
            val X by sources.add("X", "class X { public void method(String theString, int theInt) {} }")
            sources.compile()
            assertEquals(
                X._m("method").parameters.toList(),
                Mirror.reflect(X._m("method")).parameters.map { it.java }
            )
        }

        @Test
        fun `'annotations' of an non-annotated parameter should return an empty list`() {
            val X by sources.add("X", "class X { public void method(int param) {} }")
            sources.compile()
            assertArrayEquals(
                emptyArray<Any>(),
                Mirror.reflect(X._m("method")).parameters.single().annotations
            )
        }

        @Test
        fun `'annotations' of an annotated parameter should return the annotations in declaration order`() {
            val A by sources.add("A", "@Target(ElementType.PARAMETER) @Retention(RetentionPolicy.RUNTIME) @interface A {}").typed<Annotation>()
            val A2 by sources.add("A2", "@Target(ElementType.PARAMETER) @Retention(RetentionPolicy.RUNTIME) @interface A2 { String value(); }").typed<Annotation>()
            val X by sources.add("X", "class X { public void method(@A @A2(\"annotation value\") int param) {} }")
            sources.compile()
            assertArrayEquals(
                arrayOf(Mirror.newAnnotation(A), Mirror.newAnnotation(A2, "value" to "annotation value")),
                Mirror.reflect(X._m("method")).parameters.single().annotations
            )
        }

        @Test
        fun `'annotations' of a parameter with an annotated type should return an empty list`() {
            val A by sources.add("A", "@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.RUNTIME) @interface A {}").typed<Annotation>()
            val A2 by sources.add("A2", "@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.RUNTIME) @interface A2 { String value(); }").typed<Annotation>()
            val X by sources.add("X", "class X { public void method(@A @A2(\"annotation value\") int param) {} }")
            sources.compile()
            assertArrayEquals(
                arrayOf<Any>(),
                Mirror.reflect(X._m("method")).parameters.single().annotations
            )
        }
    }

    @Test
    fun `'exceptionTypes' of a method with no thrown exceptions should return an empty list`() {
        val X by sources.add("X", "class X { public void method() {} }")
        sources.compile()
        assertEquals(
            emptyList<Any>(),
            Mirror.reflect(X._m("method")).exceptionTypes
        )
    }

    @Test
    fun `'exceptionTypes' of a method with multiple thrown exceptions should return the thrown types in declaration order`() {
        val ExceptionA by sources.add("ExceptionA", "class ExceptionA extends Exception {}")
        val ExceptionB by sources.add("ExceptionB", "class ExceptionB extends Exception {}")
        val X by sources.add("X", "class X { public void method() throws ExceptionB, ExceptionA {} }")
        sources.compile()
        assertEquals(
            listOf(Mirror.reflect(ExceptionB), Mirror.reflect(ExceptionA)),
            Mirror.reflect(X._m("method")).exceptionTypes
        )
    }

    @Test
    fun `'typeParameters' of a method with no type parameters should return an empty list`() {
        val X by sources.add("X", "class X { public void method() {} }")
        sources.compile()
        assertEquals(
            emptyList<Any>(),
            Mirror.reflect(X._m("method")).typeParameters
        )
    }

    @Test
    fun `'typeParameters' of a method with type parameters should return a list of those type parameters`() {
        val X by sources.add("X", "class X { public <B, A> void method() {} }")
        sources.compile()
        assertEquals(
            X._m("method").typeParameters.map { Mirror.reflect(it) },
            Mirror.reflect(X._m("method")).typeParameters
        )
    }

    @Test
    fun `'annotations' of a method with no annotations should return an empty list`() {
        val X by sources.add("X", "class X { public void method() {} }")
        sources.compile()
        assertEquals(
            emptyList<Any>(),
            Mirror.reflect(X._m("method")).annotations
        )
    }

    @Test
    fun `'annotations' of a method with annotations should return an array of annotations`() {
        val A by sources.add("A", "@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) @interface A {}").typed<Annotation>()
        val A2 by sources.add("A2", "@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) @interface A2 { String value(); }").typed<Annotation>()
        val X by sources.add("X", "class X { @A @A2(\"annotation value\") public void method() {} }")
        sources.compile()
        assertEquals(
            listOf(Mirror.newAnnotation(A), Mirror.newAnnotation(A2, "value" to "annotation value")),
            Mirror.reflect(X._m("method")).annotations
        )
    }

    @Test
    fun `'isSynthetic' for a non-synthetic method should return false`() {
        val X by sources.add("X", "class X { public void method() {} }")
        sources.compile()
        assertFalse(Mirror.reflect(X._m("method")).isSynthetic)
    }

    @Test
    fun `'isSynthetic' for a synthetic method should return false`() {
        val X by sources.add("X", """
            class X {
                private int outsideAccess = new Nested().method();
                
                class Nested {
                    private int method() { return 1; }
                }
            }
        """.trimIndent())
        sources.compile()
        val method = X._class("Nested").declaredMethods
            .first { it.name.startsWith("access$") }
        assertTrue(Mirror.reflect(method).isSynthetic)
    }

    @Test
    fun `'isBridge' for a non-bridge method should return false`() {
        val X by sources.add("X", "class X {}")
        val Generic by sources.add("Generic", "abstract class Generic<T> { abstract void method(T arg); }")
        val Sub by sources.add("Sub", "class Sub extends Generic<Object> { void method(Object arg) {} }")
        sources.compile()
        val bridge = Mirror.reflect(Sub._m("method", _c<Any>()))
        assertFalse(bridge.isBridge)
    }

    @Test
    fun `'isBridge' for a bridge method should return true`() {
        val X by sources.add("X", "class X {}")
        val Generic by sources.add("Generic", "abstract class Generic<T> { abstract void method(T arg); }")
        val Sub by sources.add("Sub", "class Sub extends Generic<X> { void method(X arg) {} }")
        sources.compile()
        val bridge = Mirror.reflect(Sub._m("method", _c<Any>()))
        assertTrue(bridge.isBridge)
    }

    /**
     * https://bugs.openjdk.java.net/browse/JDK-5070593
     */
    @Test
    fun `bridge method should not include the 'volatile' modifier`() {
        val X by sources.add("X", "class X {}")
        val Generic by sources.add("Generic", "abstract class Generic<T> { abstract void method(T arg); }")
        val Sub by sources.add("Sub", "class Sub extends Generic<X> { void method(X arg) {} }")
        sources.compile()
        val bridge = Mirror.reflect(Sub._m("method", _c<Any>()))
        assertEquals(setOf<Any>(), bridge.modifiers)
    }

    @Test
    fun `'isDefault' for a default interface method should return true`() {
        val I by sources.add("I", "interface I { default int method() { return 1; } }")
        sources.compile()
        assertTrue(Mirror.reflect(I._m("method")).isDefault)
    }

    @Test
    fun `'isDefault' for a non-default interface method should return false`() {
        val I by sources.add("I", "interface I { int method(); }")
        sources.compile()
        assertFalse(Mirror.reflect(I._m("method")).isDefault)
    }

    @Test
    fun `'isDefault' for a class method should return false`() {
        val X by sources.add("X", "class X { int method() { return 1; } }")
        sources.compile()
        assertFalse(Mirror.reflect(X._m("method")).isDefault)
    }

    @Test
    fun `'isDefault' for an overridden default method should return false`() {
        val I by sources.add("I", "interface I { default int method() { return 1; } }")
        val X by sources.add("X", "class X implements I { public int method() { return 2; } }")
        sources.compile()
        assertFalse(Mirror.reflect(X._m("method")).isDefault)
    }

    @Test
    fun `'defaultValue' for a String default method should return the String value`() {
        val A by sources.add("A", "@interface A { String value() default \"defaultValue\"; }")
        sources.compile()
        assertEquals("defaultValue", Mirror.reflect(A._m("value")).defaultValue)
    }

    @Test
    fun `'defaultValue' for an int default method should return the int value`() {
        val A by sources.add("A", "@interface A { int value() default 1; }")
        sources.compile()
        assertEquals(1, Mirror.reflect(A._m("value")).defaultValue)
    }

    @Test
    fun `'access' for methods should be correct`() {
        val X by sources.add("X", """
            public class X {
                public void _public() {}
                protected void _protected() {}
                void _default() {}
                private void _private() {}
            }
        """.trimIndent())
        sources.compile()
        assertAll(
            { assertEquals(Modifier.Access.PUBLIC, Mirror.reflect(X._m("_public")).access) },
            { assertEquals(Modifier.Access.PROTECTED, Mirror.reflect(X._m("_protected")).access) },
            { assertEquals(Modifier.Access.DEFAULT, Mirror.reflect(X._m("_default")).access) },
            { assertEquals(Modifier.Access.PRIVATE, Mirror.reflect(X._m("_private")).access) }
        )
    }

    internal fun `internal access test case`() {}

    @Test
    fun `'access' for a Kotlin 'internal' method should be public and 'isInternalAccess'`() {
        assertEquals(Modifier.Access.PUBLIC, Mirror.reflect(::`internal access test case`.m).access)
        assertTrue(Mirror.reflect(::`internal access test case`.m).isInternalAccess)
    }

    @Test
    fun `'KCallable' for a Kotlin method should be correct`() {
        class X { }
        val method = X::class.java.declaredConstructors.single()
        assertEquals(::X, Mirror.reflect(method).kCallable)
    }

    @Test
    fun `'KCallable' for a Java method should be correct`() {
        val X by sources.add("X", "class X { public void method() {} }").typed<Any>()
        sources.compile()
        assertEquals(X.kotlin.functions.find { it.name == "method" }, Mirror.reflect(X.declaredMethods.single()).kCallable)
    }

    @Test
    fun `'KCallable' for a synthetic method should not exist`() {
        val X by sources.add("X", """
            class X {
                private int outsideAccess = new Nested().method();
                
                class Nested {
                    private int method() { return 1; }
                }
            }
        """.trimIndent())
        sources.compile()
        val method = X._class("Nested").declaredMethods
            .first { it.name.startsWith("access$") }
        assertNull(Mirror.reflect(method).kCallable)
    }

    @Test
    fun `'KCallable' for a bridge method should not exist`() {
        val X by sources.add("X", "class X {}")
        val Generic by sources.add("Generic", "abstract class Generic<T> { abstract void method(T arg); }")
        val Sub by sources.add("Sub", "class Sub extends Generic<X> { void method(X arg) {} }")
        sources.compile()
        val bridge = Mirror.reflect(Sub._m("method", _c<Any>()))
        assertNull(bridge.kCallable)
    }

    @Test
    fun `'modifiers' for methods should be correct`() {
        val X by sources.add("X", """
            public abstract class X {
                public void _public() {}
                protected void _protected() {}
                void _default() {}
                private void _private() {}
                abstract void _abstract();
                static void _static() {}
                final void _final() {}
                strictfp void _strictfp() {}
                synchronized void _synchronized() {}
                native void _native();
            }
        """.trimIndent())
        sources.compile()
        fun test(method: Method, vararg mods: Modifier) = assertEquals(setOf(*mods), Mirror.reflect(method).modifiers)
        assertAll(
            { test(X._m("_public"), Modifier.PUBLIC) },
            { test(X._m("_default")) },
            { test(X._m("_protected"), Modifier.PROTECTED) },
            { test(X._m("_private"), Modifier.PRIVATE) },
            { test(X._m("_abstract"), Modifier.ABSTRACT) },
            { test(X._m("_static"), Modifier.STATIC) },
            { test(X._m("_final"), Modifier.FINAL) },
            { test(X._m("_strictfp"), Modifier.STRICT) },
            { test(X._m("_synchronized"), Modifier.SYNCHRONIZED) },
            { test(X._m("_native"), Modifier.NATIVE) }
        )
    }

    @Test
    fun `modifier helpers for methods should be correct`() {
        val X by sources.add("X", """
            public abstract class X {
                public void _public() {}
                protected void _protected() {}
                void _default() {}
                private void _private() {}
                abstract void _abstract();
                static void _static() {}
                final void _final() {}
                strictfp void _strictfp() {}
                synchronized void _synchronized() {}
                native void _native();
            }
        """.trimIndent())
        sources.compile()
        fun test(method: Method, vararg mods: Modifier) = assertEquals(setOf(*mods), Mirror.reflect(method).modifiers)
        assertAll(
            { assertTrue(Mirror.reflect(X._m("_public")).isPublic) },
            { assertTrue(Mirror.reflect(X._m("_default")).isPackagePrivate) },
            { assertTrue(Mirror.reflect(X._m("_protected")).isProtected) },
            { assertTrue(Mirror.reflect(X._m("_private")).isPrivate) },
            { assertTrue(Mirror.reflect(X._m("_abstract")).isAbstract) },
            { assertTrue(Mirror.reflect(X._m("_static")).isStatic) },
            { assertTrue(Mirror.reflect(X._m("_final")).isFinal) },
            { assertTrue(Mirror.reflect(X._m("_strictfp")).isStrict) },
            { assertTrue(Mirror.reflect(X._m("_synchronized")).isSynchronized) },
            { assertTrue(Mirror.reflect(X._m("_native")).isNative) }
        )
    }

    @Test
    fun `'modifiers' for Kotlin methods should be correct`() {
        @Suppress("TestFunctionName")
        abstract class K {
            fun final() {}
            internal fun _internal() {}
            protected fun _protected() {}
            private fun _private() {}
            open fun _open() {}
            abstract fun _abstract()
            @Synchronized
            open fun _synchronized() {}
            @Strictfp
            open fun _strictfp() {}
        }

        fun test(method: Method, vararg mods: Modifier) = assertEquals(setOf(*mods), Mirror.reflect(method).modifiers)
        assertAll(
            { test(K::final.m, Modifier.PUBLIC, Modifier.FINAL)
                assertFalse(Mirror.reflect(K::final.m).isInternalAccess) },
            { test(K::_internal.m, Modifier.PUBLIC, Modifier.FINAL)
                assertTrue(Mirror.reflect(K::_internal.m).isInternalAccess) },
            { test(_c<K>()._m("_protected"), Modifier.PROTECTED, Modifier.FINAL)
                assertFalse(Mirror.reflect(_c<K>()._m("_protected")).isInternalAccess) },
            { test(_c<K>()._m("_private"), Modifier.PRIVATE, Modifier.FINAL)
                assertFalse(Mirror.reflect(_c<K>()._m("_private")).isInternalAccess) },
            { test(K::_open.m, Modifier.PUBLIC)
                assertFalse(Mirror.reflect(K::_open.m).isInternalAccess) },
            { test(K::_abstract.m, Modifier.PUBLIC, Modifier.ABSTRACT)
                assertFalse(Mirror.reflect(K::_abstract.m).isInternalAccess) },
            { test(_c<KotlinStatic>()._m("kotlinStatic"), Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                assertFalse(Mirror.reflect(_c<KotlinStatic>()._m("kotlinStatic")).isInternalAccess) },
            { test(K::_synchronized.m, Modifier.PUBLIC, Modifier.SYNCHRONIZED)
                assertFalse(Mirror.reflect(K::_synchronized.m).isInternalAccess) },
            { test(K::_strictfp.m, Modifier.PUBLIC, Modifier.STRICT)
                assertFalse(Mirror.reflect(K::_strictfp.m).isInternalAccess) }
        )
    }

    object KotlinStatic {
        @JvmStatic
        fun kotlinStatic() {}
    }

    @Test
    fun `'toString' for non-generic method should be correct`() {
        val X by sources.add("X", "public class X { public void method(Y arg) {} }")
        val Y by sources.add("Y", "public class Y { }")
        sources.compile()
        assertEquals("public void gen.X.method(gen.Y arg)", Mirror.reflect(X._m("method")).toString())
    }

    @Test
    fun `'toString' for generic method should place type parameters before the name`() {
        val X by sources.add("X", "public class X { public <T> void method(T arg) {} }")
        val Y by sources.add("Y", "public class Y { }")
        sources.compile()
        assertEquals("public <T> void gen.X.method(T arg)", Mirror.reflect(X._m("method")).toString())
    }

    @Test
    fun `'toString' for specialized generic method should place specialization after the name`() {
        val X by sources.add("X", "public class X { public <T> void method(T arg) {} }")
        val Y by sources.add("Y", "public class Y { }")
        sources.compile()
        assertEquals("public void gen.X.method<gen.Y>(gen.Y arg)", Mirror.reflect(X._m("method")).withTypeParameters(Mirror.reflect(Y)).toString())
    }

    @Test
    fun `'toString' for a method in an anonymous class should use the dot-separated binary class name`() {
        val X by sources.add("X", "class X { static Class type = new Y() { private void method() {} }.getClass(); }")
        val Y by sources.add("Y", "interface Y {}")
        sources.compile()
        val method = X._f("type")._get<Class<*>>(null)._m("method")
        assertEquals("private void gen.X$1.method()", Mirror.reflect(method).toString())
    }
}