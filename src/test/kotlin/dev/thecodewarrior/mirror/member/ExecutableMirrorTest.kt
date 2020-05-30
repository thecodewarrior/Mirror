package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("LocalVariableName")
internal class ExecutableMirrorTest: MTest() {
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
    fun `'exceptionTypes' of a constructor with no thrown exceptions should return an empty list`() {
        val X by sources.add("X", "class X { public X() {} }")
        sources.compile()
        assertEquals(
            emptyList<Any>(),
            Mirror.reflect(X._constructor()).exceptionTypes
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
    fun `'typeParameters' of a method with no type parameters should return an empty list`() {
        val X by sources.add("X", "class X { public void method() {} }")
        sources.compile()
        assertEquals(
            emptyList<Any>(),
            Mirror.reflect(X._m("method")).typeParameters
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
    fun `'typeParameters' of a method with type parameters should return a list of those type parameters`() {
        val X by sources.add("X", "class X { public <B, A> void method() {} }")
        sources.compile()
        assertEquals(
            X._m("method").typeParameters.map { Mirror.reflect(it) },
            Mirror.reflect(X._m("method")).typeParameters
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
    fun `'annotations' of a method with no annotations should return an empty list`() {
        val X by sources.add("X", "class X { public void method() {} }")
        sources.compile()
        assertEquals(
            emptyList<Any>(),
            Mirror.reflect(X._m("method")).annotations
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
    fun `'annotations' of a method with annotations should return an array of annotations`() {
        val A by sources.add("A", "@rt(METHOD) @interface A {}").typed<Annotation>()
        val A2 by sources.add("A2", "@rt(METHOD) @interface A2 { String value(); }").typed<Annotation>()
        val X by sources.add("X", "class X { @A @A2(\"annotation value\") public void method() {} }")
        sources.compile()
        assertEquals(
            listOf(Mirror.newAnnotation(A), Mirror.newAnnotation(A2, "value" to "annotation value")),
            Mirror.reflect(X._m("method")).annotations
        )
    }

    @Test
    fun `'annotations' of a constructor with annotations should return an array of annotations`() {
        val A by sources.add("A", "@rt(CONSTRUCTOR) @interface A {}").typed<Annotation>()
        val A2 by sources.add("A2", "@rt(CONSTRUCTOR) @interface A2 { String value(); }").typed<Annotation>()
        val X by sources.add("X", "class X { @A @A2(\"annotation value\") public X() {} }")
        sources.compile()
        assertEquals(
            listOf(Mirror.newAnnotation(A), Mirror.newAnnotation(A2, "value" to "annotation value")),
            Mirror.reflect(X._constructor()).annotations
        )
    }

    @Test
    fun `'returnType' of a void method should return the void mirror`() {
        val X by sources.add("X", "class X { public void method() {} }")
        sources.compile()
        assertEquals(
            Mirror.types.void,
            Mirror.reflect(X._m("method")).returnType
        )
    }

    @Test
    fun `'returnType' of a method with a return should return the type mirror`() {
        val X by sources.add("X", "class X { public int method() { NOP; } }")
        sources.compile()
        assertEquals(
            Mirror.types.int,
            Mirror.reflect(X._m("method")).returnType
        )
    }

    @Test
    fun `'returnType' of a constructor with should return the declaring type mirror`() {
        val X by sources.add("X", "class X { public X() { } }")
        sources.compile()
        assertEquals(
            Mirror.reflect(X),
            Mirror.reflect(X._constructor()).returnType
        )
    }

    @Test
    fun `'returnType' of a constructor in a specialized class should return the specialized declaring type mirror`() {
        val X by sources.add("X", "class X {}")
        val G by sources.add("G", "class G<T> { public G() {} }")
        val types = sources.types {
            +"G<X>"
        }
        sources.compile()
        assertEquals(
            Mirror.reflect(types["G<X>"]),
            Mirror.reflectClass(types["G<X>"]).declaredConstructors.single().returnType
        )
    }

    @Nested
    inner class Parameters: MTest() {
        @Test
        fun `'name' of a plain parameter should be correct`() {
            val X by sources.add("X", "class X { public void method(int param) {} }")
            sources.compile()
            assertEquals("param", X._m("method").parameters[0].name)
            assertEquals("param", Mirror.reflect(X._m("method")).parameters.single().name)
        }

        @Test
        fun `'name' of a parameter compiled without names should be argN`() {
            val X by sources.add("X", "class X { public void method(int param) {} }")
            sources.options.remove("-parameters")
            sources.compile()
            assertEquals("arg0", X._m("method").parameters[0].name)
            assertEquals("arg0", Mirror.reflect(X._m("method")).parameters.single().name)
        }

        @Test
        fun `'hasName' of a parameter compiled with names should be true`() {
            val X by sources.add("X", "class X { public void method(int param) {} }")
            sources.compile()
            assertTrue(Mirror.reflect(X._m("method")).parameters.single().hasName)
        }

        @Test
        fun `'hasName' of a parameter compiled without names should be false`() {
            val X by sources.add("X", "class X { public void method(int param) {} }")
            sources.options.remove("-parameters")
            sources.compile()
            assertFalse(Mirror.reflect(X._m("method")).parameters.single().hasName)
        }

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
            val A by sources.add("A", "@rt(PARAMETER) @interface A {}").typed<Annotation>()
            val A2 by sources.add("A2", "@rt(PARAMETER) @interface A2 { String value(); }").typed<Annotation>()
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

        @Test
        fun `'isFinal' of a final parameter should be true`() {
            val X by sources.add("X", "class X { public void method(final int param) {} }")
            sources.compile()
            assertTrue(Mirror.reflect(X._m("method")).parameters.single().isFinal)
        }

        @Test
        fun `'isFinal' of a non-final parameter should be false`() {
            val X by sources.add("X", "class X { public void method(int param) {} }")
            sources.compile()
            assertFalse(Mirror.reflect(X._m("method")).parameters.single().isFinal)
        }

        @Test
        fun `'index' of parameters should be correct`() {
            val X by sources.add("X", "class X { public void method(int param, int param2) {} }")
            sources.compile()
            assertEquals(listOf(0, 1), Mirror.reflect(X._m("method")).parameters.map { it.index })
        }

        @Test
        fun `'toString' of a plain parameter should be correct`() {
            val X by sources.add("X", "class X { public void method(int param) {} }")
            sources.compile()
            val param = Mirror.reflect(X._m("method")).parameters.single()
            assertEquals("int param", param.java.toString())
            assertEquals("int param", param.toString())
        }

        @Test
        fun `'toString' of a final parameter should be correct`() {
            val X by sources.add("X", "class X { public void method(final int param) {} }")
            sources.compile()
            val param = Mirror.reflect(X._m("method")).parameters.single()
            assertEquals("final int param", param.java.toString())
            assertEquals("final int param", param.toString())
        }

        @Test
        fun `'toString' of a vararg parameter should be correct`() {
            val X by sources.add("X", "class X { public void method(int... param) {} }")
            sources.compile()
            val param = Mirror.reflect(X._m("method")).parameters.single()
            assertEquals("int... param", param.java.toString())
            assertEquals("int... param", param.toString())
        }

        @Test
        fun `'toString' of a parameter without a name should be correct`() {
            val X by sources.add("X", "class X { public void method(int param) {} }")
            sources.options.remove("-parameters")
            sources.compile()
            val param = Mirror.reflect(X._m("method")).parameters.single()
            assertEquals("int arg0", param.java.toString())
            assertEquals("int arg0", param.toString())
        }
    }

}
