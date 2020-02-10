package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.NoParamNames
import dev.thecodewarrior.mirror.annotations.Annotation1
import dev.thecodewarrior.mirror.annotations.AnnotationArg1
import dev.thecodewarrior.mirror.testsupport.Exception1
import dev.thecodewarrior.mirror.testsupport.Exception2
import dev.thecodewarrior.mirror.testsupport.KotlinInternalConstructor
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.assertSameList
import dev.thecodewarrior.mirror.testsupport.assertSetEquals
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.typeholders.member.ExecutableMirrorHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.lang.reflect.Method

internal class ExecutableMirrorTest: MirrorTestBase() {
    private val holder = ExecutableMirrorHolder()

    @Test
    fun name_ofConstructor_shouldBeBinaryName() {
        val constructor = Mirror.reflect(holder.getConstructor("<init>()"))
        assertEquals(ExecutableMirrorHolder::class.java.name, constructor.name)
    }

    @Test
    fun name_ofInnerConstructor_shouldBeBinaryName() {
        val constructor = Mirror.reflect(holder.getConstructor("InnerConstructor()"))
        assertEquals(ExecutableMirrorHolder.InnerConstructor::class.java.name, constructor.name)
    }

    @Test
    fun name_withName_shouldReturnMethodName() {
        val method = Mirror.reflect(holder.m("void name()"))
        assertEquals("name", method.name)
    }

    @Test
    fun isVarArgs_withNonVarArgMethod_shouldReturnFalse() {
        assertFalse(Mirror.reflect(holder.m("void (String, int)")).isVarArgs)
    }

    @Test
    fun isVarArgs_withVarArgMethod_shouldReturnTrue() {
        assertTrue(Mirror.reflect(holder.m("void (String...)")).isVarArgs)
    }

    @Test
    fun isVarArgs_withNonVarArgConstructor_shouldReturnFalse() {
        assertFalse(Mirror.reflect(holder.getConstructor("<init>(String)")).isVarArgs)
    }

    @Test
    fun isVarArgs_withVarArgConstructor_shouldReturnTrue() {
        assertTrue(Mirror.reflect(holder.getConstructor("<init>(String...)")).isVarArgs)
    }

    @Test
    fun returnType_withVoidReturn_shouldReturnVoid() {
        val method = Mirror.reflect(holder.m("void ()"))
        assertSame(Mirror.types.void, method.returnType)
    }

    @Test
    fun returnType_withReturnType_shouldReturnCorrectType() {
        val method = Mirror.reflect(holder.m("String ()"))
        assertSame(Mirror.reflect<String>(), method.returnType)
    }

    @Test
    fun parameters_withNoParameters_shouldReturnEmptyList() {
        val method = Mirror.reflect(holder.m("void ()"))
        assertEquals(emptyList<Any>(), method.parameters)
    }

    @Test
    fun parameters_withParameters_shouldHaveCorrectNameTypeAndJava() {
        val method = Mirror.reflect(holder.m("void (String, int)"))
        assertSameList(listOf(
            Mirror.reflect<String>(),
            Mirror.types.int
        ), method.parameters.map { it.type })
        assertEquals(listOf(
            "s",
            "i"
        ), method.parameters.map { it.name })
        assertEquals(listOf(
            holder.p("void (String, int) > s"),
            holder.p("void (String, int) > i")
        ), method.parameters.map { it.java })
    }

    @Test
    fun parameterTypes_withNoParameters_shouldBeEmpty() {
        val method = Mirror.reflect(holder.m("void ()"))
        assertSameList(emptyList(), method.parameterTypes)
    }

    @Test
    fun parameterTypes_withParameters_shouldHaveCorrectTypes() {
        val method = Mirror.reflect(holder.m("void (String, int)"))
        assertSameList(listOf(
            Mirror.reflect<String>(),
            Mirror.types.int
        ), method.parameterTypes)
    }

    @Test
    @DisplayName("A void method with unnamed parameters should have corresponding parameter mirrors with null names")
    fun parameters_withNoBytecodeNames_shouldHaveNullNames() {
        val method = Mirror.reflect(NoParamNames::noNames.m)
        assertEquals(listOf(
            null
        ), method.parameters.map { it.name })
    }

    @Test
    fun exceptionTypes_withNoExceptions_shouldReturnExceptionTypes() {
        val method = Mirror.reflect(holder.m("void ()"))
        assertSameList(emptyList(), method.exceptionTypes)
    }

    @Test
    fun exceptionTypes_withCheckedExceptions_shouldReturnExceptionTypes() {
        val method = Mirror.reflect(holder.m("void () throws"))
        assertSameList(listOf(
            Mirror.reflect<Exception1>(),
            Mirror.reflect<Exception2>()
        ), method.exceptionTypes)
    }

    @Test
    fun typeParameters_withNoTypeParameters_shouldBeEmpty() {
        val method = Mirror.reflect(holder.m("void ()"))
        assertSameList(emptyList(), method.typeParameters)
    }

    @Test
    fun typeParameters_withTypeParameters_shouldHaveCorrectTypeParameters() {
        val method = Mirror.reflect(holder.m("<T> void (T)"))
        assertSameList(listOf(
            Mirror.reflect(holder.t("<T> void (T) > T"))
        ), method.typeParameters)
    }

    @Test
    fun annotations_withNoAnnotations_shouldReturnEmptyList() {
        val method = Mirror.reflect(ExecutableMirrorHolder::noMethodAnnotations.m)
        assertEquals(emptyList<Annotation>(), method.annotations)
    }

    @Test
    fun annotations_withAnnotatedMethod_shouldReturnAnnotations() {
        val method = Mirror.reflect(ExecutableMirrorHolder::methodAnnotations.m)
        assertSetEquals(listOf(
            Mirror.newAnnotation<Annotation1>(),
            Mirror.newAnnotation<AnnotationArg1>("arg" to 1)
        ), method.annotations)
    }

    @Test
    fun parameterAnnotations_withNoAnnotations_shouldReturnEmptyList() {
        val method = Mirror.reflect(holder.m("void (_)"))
        assertEquals(emptyList<Annotation>(), method.parameters[0].annotations)
    }

    @Test
    fun parameterAnnotations_withAnnotations_shouldReturnAnnotations() {
        val method = Mirror.reflect(holder.m("void (@- @- String)"))
        assertSetEquals(listOf(
            Mirror.newAnnotation<Annotation1>(),
            Mirror.newAnnotation<AnnotationArg1>("arg" to 1)
        ), method.parameters[0].annotations)
    }

    @Test
    fun isSynthetic_withNonSyntheticMethod_shouldReturnFalse() {
        val synthetic = Mirror.reflect(holder.c("SyntheticHolder").m("nonSynthetic"))
        assertFalse(synthetic.isSynthetic)
    }

    @Test
    fun isSynthetic_withSyntheticMethod_shouldReturnTrue() {
        val synthetic = Mirror.reflect(holder.c("SyntheticHolder").m("access\$100", holder.c("SyntheticHolder")))
        assertTrue(synthetic.isSynthetic)
    }

    @Test
    fun isSynthetic_withNonSyntheticConstructor_shouldReturnFalse() {
        val synthetic = Mirror.reflect(holder.c("SyntheticHolder").declaredConstructors.first { it.parameterCount == 0})
        assertFalse(synthetic.isSynthetic)
    }

    @Test
    fun isSynthetic_withSyntheticConstructor_shouldReturnTrue() {
        val synthetic = Mirror.reflect(holder.c("SyntheticHolder").declaredConstructors.first { it.parameterCount == 1})
        assertTrue(synthetic.isSynthetic)
    }

    @Test
    fun access_withMethod_shouldReturnCorrectAccess() {
        assertEquals(Modifier.Access.PUBLIC, Mirror.reflect(holder.m("public void ()")).access)
        assertEquals(Modifier.Access.DEFAULT, Mirror.reflect(holder.m("default void ()")).access)
        assertEquals(Modifier.Access.PROTECTED, Mirror.reflect(holder.m("protected void ()")).access)
        assertEquals(Modifier.Access.PRIVATE, Mirror.reflect(holder.m("private void ()")).access)
    }

    @Test
    fun access_withConstructor_shouldReturnCorrectAccess() {
        assertEquals(Modifier.Access.PUBLIC, Mirror.reflect(holder.getConstructor("public <init>()")).access)
        assertEquals(Modifier.Access.DEFAULT, Mirror.reflect(holder.getConstructor("default <init>()")).access)
        assertEquals(Modifier.Access.PROTECTED, Mirror.reflect(holder.getConstructor("protected <init>()")).access)
        assertEquals(Modifier.Access.PRIVATE, Mirror.reflect(holder.getConstructor("private <init>()")).access)
    }

    @Test
    fun access_ofKotlinInternalMethod_shouldBePublicAndInternal() {
        val constructor = Mirror.reflect(KotlinInternalConstructor::internalMethod.m)
        assertEquals(Modifier.Access.PUBLIC, constructor.access)
        assertTrue(constructor.isInternalAccess)
    }

    @Test
    fun access_ofKotlinInternalConstructor_shouldBePublicAndInternal() {
        val constructor = Mirror.reflect(::KotlinInternalConstructor.c)
        assertEquals(Modifier.Access.PUBLIC, constructor.access)
        assertTrue(constructor.isInternalAccess)
    }

    @Test
    fun kCallable_ofSyntheticMethod_shouldExist() {
        val synthetic = Mirror.reflect(holder.c("SyntheticHolder").m("access\$100", holder.c("SyntheticHolder")))
        assertNull(synthetic.kCallable)
    }

    @Test
    fun kCallable_ofSyntheticConstructor_shouldExist() {
        val synthetic = Mirror.reflect(holder.c("SyntheticHolder").declaredConstructors.first { it.parameterCount == 1})
        assertNull(synthetic.kCallable)
    }

    @Test
    fun modifiers_ofJavaMethod_shouldBeCorrect() {
        fun test(name: String, vararg mods: Modifier) = assertEquals(setOf(*mods), Mirror.reflect(holder.m(name)).modifiers)
        assertAll(
            { test("public void ()", Modifier.PUBLIC) },
            { test("default void ()") },
            { test("protected void ()", Modifier.PROTECTED) },
            { test("private void ()", Modifier.PRIVATE) },
            { test("abstract void ()", Modifier.ABSTRACT) },
            { test("static void ()", Modifier.STATIC) },
            { test("final void ()", Modifier.FINAL) },
            { test("strictfp void ()", Modifier.STRICT) }
        )
    }

    @Test
    fun modifiers_ofJavaConstructor_shouldBeCorrect() {
        fun test(name: String, vararg mods: Modifier) = assertEquals(setOf(*mods), Mirror.reflect(holder.getConstructor(name)).modifiers)
        assertAll(
            { test("public <init>()", Modifier.PUBLIC) },
            { test("default <init>()") },
            { test("protected <init>()", Modifier.PROTECTED) },
            { test("private <init>()", Modifier.PRIVATE) }
        )
    }

    @Test
    fun modifiers_ofKotlinMethod_shouldBeCorrect() {
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
            { test(K::class.m("_protected"), Modifier.PROTECTED, Modifier.FINAL)
                assertFalse(Mirror.reflect(K::class.m("_protected")).isInternalAccess) },
            { test(K::class.m("_private"), Modifier.PRIVATE, Modifier.FINAL)
                assertFalse(Mirror.reflect(K::class.m("_private")).isInternalAccess) },
            { test(K::_open.m, Modifier.PUBLIC)
                assertFalse(Mirror.reflect(K::_open.m).isInternalAccess) },
            { test(K::_abstract.m, Modifier.PUBLIC, Modifier.ABSTRACT)
                assertFalse(Mirror.reflect(K::_abstract.m).isInternalAccess) },
            { test(ExecutableMirrorTest::class.m("kotlinStatic"), Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                assertFalse(Mirror.reflect(ExecutableMirrorTest::class.m("kotlinStatic")).isInternalAccess) },
            { test(K::_synchronized.m, Modifier.PUBLIC, Modifier.SYNCHRONIZED)
                assertFalse(Mirror.reflect(K::_synchronized.m).isInternalAccess) },
            { test(K::_strictfp.m, Modifier.PUBLIC, Modifier.STRICT)
                assertFalse(Mirror.reflect(K::_strictfp.m).isInternalAccess) }
        )
    }

    @Test
    fun modifiers_ofKotlinConstructor_shouldBeCorrect() {
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

    companion object {
        @JvmStatic
        fun kotlinStatic() {}
    }
}