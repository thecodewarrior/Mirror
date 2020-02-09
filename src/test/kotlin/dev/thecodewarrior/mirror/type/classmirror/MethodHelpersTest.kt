package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.assertSetEquals
import dev.thecodewarrior.mirror.typeholders.classmirror.MethodHelpersHolder
import dev.thecodewarrior.mirror.typeholders.member.ExecutableMirrorHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class MethodHelpersTest: MirrorTestBase() {
    val holder = MethodHelpersHolder()

    private inline fun <reified T> testMethodsAgainstJava()
        = assertSetEquals(T::class.java.methods.map { Mirror.reflect(it) }, Mirror.reflectClass<T>().publicMethods)

    @Test
    fun methods_ofRawTypes_shouldMatchJava() {
        assertSetEquals(Mirror.types.int.java.methods.map { Mirror.reflect(it) }, Mirror.types.int.publicMethods)
        testMethodsAgainstJava<MethodHelpersHolder.EmptyInterface>()
        testMethodsAgainstJava<MethodHelpersHolder.NonEmptyInterface>()
        testMethodsAgainstJava<MethodHelpersHolder.NonEmptyInterfaceOverride>()
        testMethodsAgainstJava<MethodHelpersHolder.NonEmptyInterfaceShadow>()
        testMethodsAgainstJava<MethodHelpersHolder.NonEmptyInterfaceImplSuperOverrideImpl>()
        testMethodsAgainstJava<MethodHelpersHolder.ClassWithStaticsInSupertypes>()
    }

    @Test
    fun coreMethod_whenGettingTwice_shouldEqualItself() {
        val method1 = m<ExecutableMirrorHolder>("name")
        val method2 = m<ExecutableMirrorHolder>("name")
        assertEquals(method1, method2)
    }

    @Test
    fun coreMethod_withOverriddenMethod_shouldNotEqualSuperclassMethod() {
        val superclassMethod = holder.c("MethodsToInherit").getDeclaredMethod("methodToOverride")
        val subclassMethod = holder.c("MethodInheritor").getDeclaredMethod("methodToOverride")
        assertNotEquals(superclassMethod, subclassMethod)
    }

    @Test
    fun coreGetMethod_withInheritedMethod_shouldReturnSuperclassMethod() {
        val superclassMethod = holder.c("MethodsToInherit").getMethod("methodToInherit")
        val subclassMethod = holder.c("MethodInheritor").getMethod("methodToInherit")
        assertEquals(superclassMethod, subclassMethod)
    }

    @Test
    fun coreGetMethod_withOverriddenMethod_shouldNotReturnSuperclassMethod() {
        val superclassMethod = holder.c("MethodsToInherit").getMethod("methodToOverride")
        val subclassMethod = holder.c("MethodInheritor").getMethod("methodToOverride")
        assertNotEquals(superclassMethod.declaringClass, subclassMethod.declaringClass)
    }

}