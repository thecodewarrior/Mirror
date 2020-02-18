@file:Suppress("ClassName", "PropertyName")

package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.member.Modifier
import dev.thecodewarrior.mirror.testsupport.FlatTest
import dev.thecodewarrior.mirror.testsupport.FlatTestScanner
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.TestCompiler
import dev.thecodewarrior.mirror.testsupport.assertSameSet
import dev.thecodewarrior.mirror.typeholders.classmirror.MethodsHolder
import dev.thecodewarrior.mirror.utils.Untested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

@Suppress("LocalVariableName")
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
    class inheritedMethods_withSuperclassMethodsSamePackage_shouldInheritNonPrivate: inheritedMethods_base(
    ) {
        fun run() {
            val classes = TestCompiler()
                .add("X", """
                    public class X {
                        public static class Inner {}
                        public void publicMethod() {}
                        protected void protectedMethod() {}
                        void packagePrivateMethod() {}
                        private void privateMethod() {}
                    }
                """)
                .add("Y", """
                    public class Y extends X {}
                """)
                .compile()
            val X = classes["X"]
            val Y = classes["Y"]

            assertSameSet(_any + listOf(
                Mirror.reflect(X.m("publicMethod")),
                Mirror.reflect(X.m("protectedMethod")),
                Mirror.reflect(X.m("packagePrivateMethod"))
            ), Mirror.reflectClass(Y).inheritedMethods)
        }
    }


    @FlatTest
    class inheritedMethods_withSuperclassMethodsDifferentPackage_shouldInheritNonPrivateNonPackage: inheritedMethods_base(
    ) {
        fun run() {
            val classes = TestCompiler()
                .add("X", """
                    public class X {
                        public static class Inner {}
                        public void publicMethod() {}
                        protected void protectedMethod() {}
                        void packagePrivateMethod() {}
                        private void privateMethod() {}
                    }
                """)
                .add("y.Y", """
                    public class Y extends X {}
                """)
                .compile()
            val X = classes["X"]
            val Y = classes["y.Y"]

            assertSameSet(_any + listOf(
                Mirror.reflect(X.m("publicMethod")),
                Mirror.reflect(X.m("protectedMethod"))
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
