package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.NoParamNames
import dev.thecodewarrior.mirror.annotations.Annotation1
import dev.thecodewarrior.mirror.annotations.AnnotationArg1
import dev.thecodewarrior.mirror.testsupport.ClosedObject1
import dev.thecodewarrior.mirror.testsupport.CompanionHolder
import dev.thecodewarrior.mirror.testsupport.DataObject1
import dev.thecodewarrior.mirror.testsupport.EnumClass1
import dev.thecodewarrior.mirror.testsupport.Exception1
import dev.thecodewarrior.mirror.testsupport.Exception2
import dev.thecodewarrior.mirror.testsupport.FieldVisibilityTestClass
import dev.thecodewarrior.mirror.testsupport.Interface1
import dev.thecodewarrior.mirror.testsupport.KotlinInternalClass
import dev.thecodewarrior.mirror.testsupport.KotlinInternalConstructor
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.SealedClass
import dev.thecodewarrior.mirror.testsupport.assertSameList
import dev.thecodewarrior.mirror.testsupport.assertSetEquals
import dev.thecodewarrior.mirror.testsupport.simpletypes.JObject1
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.typeholders.member.ExecutableMirrorHolder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

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
        val method = Mirror.reflect(NoParamNames::noNames.javaMethod!!)
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
        val method = Mirror.reflect(ExecutableMirrorHolder::noMethodAnnotations.javaMethod!!)
        assertEquals(emptyList<Annotation>(), method.annotations)
    }

    @Test
    fun annotations_withAnnotatedMethod_shouldReturnAnnotations() {
        val method = Mirror.reflect(ExecutableMirrorHolder::methodAnnotations.javaMethod!!)
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
        val constructor = Mirror.reflect(KotlinInternalConstructor::internalMethod.javaMethod!!)
        assertEquals(Modifier.Access.PUBLIC, constructor.access)
        assertTrue(constructor.isInternalAccess)
    }

    @Test
    fun access_ofKotlinInternalConstructor_shouldBePublicAndInternal() {
        val constructor = Mirror.reflect(::KotlinInternalConstructor.javaConstructor!!)
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
        test("public void ()", Modifier.PUBLIC)
        test("default void ()")
        test("protected void ()", Modifier.PROTECTED)
        test("private void ()", Modifier.PRIVATE)
        test("abstract void ()", Modifier.ABSTRACT)
        test("static void ()", Modifier.STATIC)
        test("final void ()", Modifier.FINAL)
        // TODO Strictfp flag missing from java modifiers
         test("strictfp void ()", Modifier.STRICT)
    }

    /*
    private inline fun <reified T> testFlags(vararg flags: ClassMirror.Flag) {
        assertEquals(setOf(*flags), Mirror.reflectClass<T>().flags)
    }

    private fun testFlags(name: String, vararg flags: ClassMirror.Flag) {
        assertEquals(setOf(*flags), Mirror.reflectClass(holder.getClass(name)).flags)
    }

    @Test
    fun kotlinFlags_ofKotlinClass_shouldBeCorrect() {
        assertAll(
            { testFlags<ClosedObject1>(ClassMirror.Flag.FINAL) },
            { testFlags<Object1>() },
            { testFlags<CompanionHolder.Companion>(ClassMirror.Flag.FINAL, ClassMirror.Flag.STATIC, ClassMirror.Flag.MEMBER)
                assertTrue(Mirror.reflectClass<CompanionHolder.Companion>().isCompanion) },
            { testFlags<DataObject1>(ClassMirror.Flag.FINAL)
                assertTrue(Mirror.reflectClass<DataObject1>().isData) },
            { testFlags<SealedClass>(ClassMirror.Flag.ABSTRACT)
                assertTrue(Mirror.reflectClass<SealedClass>().isSealed) },
            { testFlags<Interface1>(ClassMirror.Flag.INTERFACE, ClassMirror.Flag.ABSTRACT) }
        )
    }

    @Test
    fun flags_ofClasses_shouldBeCorrect() {
        assertAll(
            { testFlags("public static class", ClassMirror.Flag.MEMBER) },
            { testFlags("public class", ClassMirror.Flag.MEMBER) },
            { testFlags("default class", ClassMirror.Flag.MEMBER) },
            { testFlags("protected class", ClassMirror.Flag.MEMBER) },
            { testFlags("private class", ClassMirror.Flag.MEMBER) },
            { testFlags("abstract class", ClassMirror.Flag.MEMBER, ClassMirror.Flag.ABSTRACT) },
            { testFlags("static class", ClassMirror.Flag.MEMBER, ClassMirror.Flag.STATIC) },
            { testFlags("final class", ClassMirror.Flag.MEMBER, ClassMirror.Flag.FINAL) },
            // TODO Strictfp flag missing from java modifiers
            // { testFlags("strictfp class", Flag.MEMBER, Flag.STRICT) },
            { testFlags("annotation class", ClassMirror.Flag.MEMBER, ClassMirror.Flag.INTERFACE, ClassMirror.Flag.ABSTRACT, ClassMirror.Flag.ANNOTATION, ClassMirror.Flag.STATIC) },
            { testFlags("interface", ClassMirror.Flag.MEMBER, ClassMirror.Flag.STATIC, ClassMirror.Flag.INTERFACE, ClassMirror.Flag.ABSTRACT) },
            { assertEquals(setOf(ClassMirror.Flag.ANONYMOUS), Mirror.reflectClass(holder.innerAnonymous.javaClass).flags) },
            { assertEquals(setOf(ClassMirror.Flag.ANONYMOUS), Mirror.reflectClass(holder.anonymous.javaClass).flags) },
            { assertEquals(setOf(ClassMirror.Flag.LOCAL), Mirror.reflectClass(holder.local).flags) },
            { assertEquals(setOf(ClassMirror.Flag.FINAL, ClassMirror.Flag.SYNTHETIC), Mirror.reflectClass(holder.lambda.javaClass).flags) },
            { assertEquals(setOf(ClassMirror.Flag.ABSTRACT, ClassMirror.Flag.FINAL, ClassMirror.Flag.PRIMITIVE), Mirror.types.int.flags) },
            { testFlags<JObject1>() },
            { testFlags<EnumClass1>(ClassMirror.Flag.ENUM) }
        )
    }
    */
}