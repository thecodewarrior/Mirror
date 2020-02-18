@file:Suppress("ClassName", "PropertyName")

package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.member.Modifier
import dev.thecodewarrior.mirror.testsupport.FlatTest
import dev.thecodewarrior.mirror.testsupport.FlatTestScanner
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.assertSameList
import dev.thecodewarrior.mirror.testsupport.assertSameSet
import dev.thecodewarrior.mirror.typeholders.classmirror.MethodsHolder
import dev.thecodewarrior.mirror.utils.Untested
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.net.URI

internal class MethodsTest: MirrorTestBase(MethodsHolder()) {
    @TestFactory
    fun flat() = FlatTestScanner.scan(this)

    @Untested // just so it shows up in searches
    @Test
    fun everything_onMutationAttempt_shouldThrow() {}

// abstract val declaredMethods: List<MethodMirror> ====================================================================
    @Test
    fun declaredMethods_shouldEqualCoreDeclaredMethods() {}

// abstract val publicMethods: List<MethodMirror> ======================================================================
    @Test
    fun publicMethods_shouldEqualCoreMethods() {}

// abstract val inheritedMethods: List<MethodMirror> ============================================================================


    abstract class inheritedMethods_base: MTest() {
        /**
         * Easy access to the list of methods inherited from [Any].
         */
        val _any: List<MethodMirror> get() = Mirror.reflectClass<Any>().declaredMethods.filter {
            it.access == Modifier.Access.PROTECTED || it.access == Modifier.Access.PUBLIC
        }

        inline fun <reified T> inherited() = Mirror.reflectClass<T>().inheritedMethods
    }

    @FlatTest
    class inheritedMethods_ofObject_shouldBeEmpty: inheritedMethods_base() {
        fun run() {
            assertSameSet(emptyList<MethodMirror>(), inherited<Any>())
        }
    }

    @FlatTest
    class inheritedMethods_ofInterface_shouldBeEmpty: inheritedMethods_base() {
        interface I

        fun run() {
            assertSameSet(emptyList<MethodMirror>(), inherited<I>())
        }
    }

    @FlatTest
    class inheritedMethods_ofClass_shouldBeObjectMethods: inheritedMethods_base() {
        class X

        fun run() {
            assertSameSet(_any, Mirror.reflectClass<X>().inheritedMethods)
        }
    }

    @FlatTest
    class inheritedMethods_withEmptySuperclass_shouldBeObjectMethods: inheritedMethods_base() {
        open class X
        class Y: X()

        fun run() {
            assertSameSet(_any, Mirror.reflectClass<Y>().inheritedMethods)
        }
    }

    @FlatTest
    class inheritedMethods_withSuperclassMethods_shouldInheritCorrectMethods: inheritedMethods_base() {
        val X by compile("X", """
            public class X {
                public static class Inner {}
                public void publicMethod() {}
                protected void protectedMethod() {}
                void packagePrivateMethod() {}
                private void privateMethod() {}
            }
        """.trimIndent())
        val Y by compile("Y", """
            public class Y {}
        """.trimIndent())
        val Y2 by compile("Y", """
            public class Y {}
        """.trimIndent())

        fun run() {
            assertSameSet(_any + listOf(
                X.m("publicMethod"),
                X.m("protectedMethod"),
                X.m("packagePrivateMethod")
            ), Mirror.reflectClass(Y).inheritedMethods)
        }
    }

    // # [Java Language Specification §8.4.8](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-8.4.8)
    // A class C inherits from its direct superclass all concrete methods m (both static and instance) of the superclass
    // for which all of the following are true:
    //
    //     m is a member of the direct superclass of C.
    //
    //     m is public, protected, or declared with package access in the same package as C.
    //
    //     No method declared in C has a signature that is a subsignature (§8.4.2) of the signature of m.
    //
    // A class C inherits from its direct superclass and direct superinterfaces all abstract and default (§9.4) methods
    // m for which all of the following are true:
    //
    //     m is a member of the direct superclass or a direct superinterface, D, of C.
    //
    //     m is public, protected, or declared with package access in the same package as C.
    //
    //     No method declared in C has a signature that is a subsignature (§8.4.2) of the signature of m.
    //
    //     No concrete method inherited by C from its direct superclass has a signature that is a subsignature of the signature of m.
    //
    //     There exists no method m' that is a member of the direct superclass or a direct superinterface, D', of C (m distinct from m', D distinct from D'), such that m' overrides from D' (§8.4.8.1, §9.4.1.1) the declaration of the method m.
    //
    // A class does not inherit private or static methods from its superinterfaces.
}
