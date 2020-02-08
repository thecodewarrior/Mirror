package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.assertSetEquals
import dev.thecodewarrior.mirror.typeholders.classmirror.MethodHelpersHolder
import org.junit.jupiter.api.Test

class MethodHelpersTest: MirrorTestBase() {

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
}