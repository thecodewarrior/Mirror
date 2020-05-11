@file:Suppress("ClassName", "PropertyName")

package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.member.Modifier
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.assertSameSet
import dev.thecodewarrior.mirror.testsupport.assertSetEquals
import dev.thecodewarrior.mirror.utils.Untested
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("LocalVariableName")
internal class MethodsTest: MTest() {

    @Untested // just so it shows up in searches
    @Test
    fun everything_onMutationAttempt_shouldThrow() {}

// abstract val declaredMethods: List<MethodMirror> ====================================================================

    @Test
    fun `declaredMethods of an empty interface should be empty`() {
    }

    @Test
    fun `declaredMethods of Object should be `() {
    }

// abstract val publicMethods: List<MethodMirror> ======================================================================

    @Test
    fun `getMethods() on a Core Reflection class shouldn't include overridden methods`() {
        val X by sources.add("X", "class X { public void xMethod() {} public void overrideMethod() {} }")
        val Y by sources.add("Y", "class Y extends X { public void yMethod() {} public void overrideMethod() {} }")
        sources.compile()
        assertSetEquals(listOf(
            X._m("xMethod"), Y._m("yMethod"), Y._m("overrideMethod")
        ) + Any::class.java.methods, Y.methods.toList())
    }

    @Test
    fun `getMethods() with interface overriding interface shouldn't include overridden methods`() {
        val I by sources.add("I", "interface I { public void iMethod(); public void overrideMethod(); }")
        val J by sources.add("J", "interface J extends I { public void jMethod(); public void overrideMethod(); }")
        val X by sources.add("X", "abstract class X implements J { public void xMethod() {} }")
        sources.compile()
        assertSetEquals(listOf(
            I._m("iMethod"), J._m("jMethod"), X._m("xMethod"), J._m("overrideMethod")
        ) + Any::class.java.methods, X.methods.toList())
    }

    @Test
    fun publicMethods_shouldEqualCoreMethods() {}

// abstract val inheritedMethods: List<MethodMirror> ============================================================================

    /**
     * # Java Language Specification [§8.4.8](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-8.4.8)
     *
     * A class C inherits from its direct superclass all concrete methods m (both static and instance) of the superclass
     * for which all of the following are true:
     *
     * - m is a member of the direct superclass of C.
     * - m is public, protected, or declared with package access in the same package as C.
     * - No method declared in C has a signature that is a subsignature
     * ([§8.4.2](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-8.4.2)) of the signature of m.
     *
     * A class C inherits from its direct superclass and direct superinterfaces all abstract and default (§9.4) methods
     * m for which all of the following are true:
     *
     * - m is a member of the direct superclass or a direct superinterface, D, of C.
     * - m is public, protected, or declared with package access in the same package as C.
     * - No method declared in C has a signature that is a subsignature
     * ([§8.4.2](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-8.4.2)) of the signature of m.
     * - No concrete method inherited by C from its direct superclass has a signature that is a subsignature of the
     * signature of m.
     * - There exists no method m' that is a member of the direct superclass or a direct superinterface, D', of C
     * (m distinct from m', D distinct from D'), such that m' overrides from D'
     * ([§8.4.8.1](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-8.4.8.1),
     * [§9.4.1.1](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-9.4.1.1)) the declaration of the
     * method m.
     *
     * A class does not inherit private or static methods from its superinterfaces.
     */
    @Nested
    inner class InheritedMethods: MTest() {
        /**
         * Easy access to the list of methods inherited from [Any].
         */
        val _any: List<MethodMirror>
            get() = Mirror.reflectClass<Any>().declaredMethods.filter {
                it.access == Modifier.Access.PROTECTED || it.access == Modifier.Access.PUBLIC
            }

        @Test
        fun inheritedMethods_ofObject_shouldBeEmpty() {
            assertSameSet(emptyList<MethodMirror>(), Mirror.reflectClass<Any>().inheritedMethods)
        }

        @Test
        fun inheritedMethods_ofInterface_shouldBeEmpty() {
            val I by sources.add("I", "interface I {}")
            sources.compile()
            assertSameSet(emptyList<MethodMirror>(), Mirror.reflectClass(I).inheritedMethods)
        }

        @Test
        fun inheritedMethods_ofClass_shouldBeObjectMethods() {
            val X by sources.add("X", "class X {}")
            sources.compile()
            assertSameSet(_any, Mirror.reflectClass(X).inheritedMethods)
        }

        @Test
        fun inheritedMethods_withEmptySuperclass_shouldBeObjectMethods() {
            val X by sources.add("X", "class X {}")
            val Y by sources.add("Y", "class Y {}")
            sources.compile()
            assertSameSet(_any, Mirror.reflectClass(Y).inheritedMethods)
        }

        @Test
        fun inheritedMethods_withSuperclassMethodsSamePackage_shouldInheritNonPrivate() {
            val X by sources.add("X", """
                public class X {
                    public void publicMethod() {}
                    protected void protectedMethod() {}
                    void packagePrivateMethod() {}
                    private void privateMethod() {}
                }
            """.trimIndent())
            val Y by sources.add("Y", """
                public class Y extends X {}
            """.trimIndent())
            sources.compile()

            assertSameSet(_any + listOf(
                Mirror.reflect(X._m("publicMethod")),
                Mirror.reflect(X._m("protectedMethod")),
                Mirror.reflect(X._m("packagePrivateMethod"))
            ), Mirror.reflectClass(Y).inheritedMethods)
        }

        @Test
        fun inheritedMethods_withSuperclassMethodsDifferentPackage_shouldInheritNonPrivateNonPackage() {
            val X by sources.add("X", """
                public class X {
                    public void publicMethod() {}
                    protected void protectedMethod() {}
                    void packagePrivateMethod() {}
                    private void privateMethod() {}
                }
            """)
            val Y by sources.add("y.Y", """
                public class Y extends X {}
            """)
            sources.compile()

            assertSameSet(_any + listOf(
                Mirror.reflect(X._m("publicMethod")),
                Mirror.reflect(X._m("protectedMethod"))
            ), Mirror.reflectClass(Y).inheritedMethods)
        }

        @Test
        fun inheritedMethods_viaClassInSamePackage_shouldInheritNonPrivateNonPackage() {
            val X by sources.add("X", """
                public class X {
                    public void publicMethod() {}
                    protected void protectedMethod() {}
                    void packagePrivateMethod() {}
                    private void privateMethod() {}
                }
            """)
            val Y by sources.add("Y", """
                public class Y extends X {}
            """)
            val Z by sources.add("Z", """
                public class Z extends Y {}
            """)
            sources.compile()

            assertSameSet(_any + listOf(
                Mirror.reflect(X._m("publicMethod")),
                Mirror.reflect(X._m("protectedMethod")),
                Mirror.reflect(X._m("packagePrivateMethod"))
            ), Mirror.reflectClass(Z).inheritedMethods)
        }

        @Test
        fun inheritedMethods_viaClassInDifferentPackage_shouldInheritNonPrivateNonPackage() {
            val X by sources.add("X", """
                public class X {
                    public void publicMethod() {}
                    protected void protectedMethod() {}
                    void packagePrivateMethod() {}
                    private void privateMethod() {}
                }
            """)
            val Y by sources.add("y.Y", """
                public class Y extends X {}
            """)
            val Z by sources.add("Z", """
                public class Z extends gen.y.Y {}
            """)
            sources.compile()

            assertSameSet(_any + listOf(
                Mirror.reflect(X._m("publicMethod")),
                Mirror.reflect(X._m("protectedMethod"))
            ), Mirror.reflectClass(Z).inheritedMethods)
        }

        @Test
        fun inheritedMethods_withUnimplementedInterfaceMethods_shouldInheritInterfaceMethods() {
            val I by sources.add("I", """
                public interface I {
                    void interfaceMethod();
                }
            """)
            val X by sources.add("X", """
                public abstract class X implements I {
                }
            """)
            sources.compile()

            assertSameSet(_any + listOf(
                Mirror.reflect(I._m("interfaceMethod"))
            ), Mirror.reflectClass(X).inheritedMethods)
        }

        @Test
        fun inheritedMethods_withImplementedInterfaceMethods_shouldNotInheritInterfaceMethods() {
            val I by sources.add("I", """
                public interface I {
                    void interfaceMethod();
                }
            """)
            val X by sources.add("X", """
                public class X implements I {
                    public void interfaceMethod() {}
                }
            """)
            sources.compile()

            assertSameSet(_any + listOf(
            ), Mirror.reflectClass(X).inheritedMethods)
        }

        @Test
        fun inheritedMethods_withUnimplementedInterfaceExtendsMethods_shouldInheritInterfaceExtendsMethods() {
            val I by sources.add("I", """
                public interface I {
                    void interfaceMethod();
                }
            """)
            val I2 by sources.add("I2", "interface I2 extends I {}")
            val X by sources.add("X", """
                public abstract class X implements I2 {
                }
            """)
            sources.compile()

            assertSameSet(_any + listOf(
                Mirror.reflect(I._m("interfaceMethod"))
            ), Mirror.reflectClass(X).inheritedMethods)
        }

        @Test
        fun inheritedMethods_withGenericMethods_withDifferentConcreteSignature_shouldInheritGenericMethods() {
            val Generic by sources.add("Generic", """
                class Generic<T> {
                    void generic(T arg) {}
                }
            """)
            val X by sources.add("X", """
                class X<V> extends Generic<V> {
                    void generic(String arg) {}
                }
            """)
            sources.compile()

            assertSameSet(_any + listOf(
                Mirror.reflectClass(X).getMethod(Generic._m("generic")) // getMethod because it will be specialized for V
            ), Mirror.reflectClass(X).inheritedMethods)
        }

        @Test
        fun inheritedMethods_withGenericMethods_withGenericOverrideSignature_shouldNotInheritGenericMethods() {
            val Generic by sources.add("Generic", """
                class Generic<T> {
                    void generic(T arg) {}
                }
            """)
            val X by sources.add("X", """
                class X<V> extends Generic<V> {
                    void generic(V arg) {}
                }
            """)
            sources.compile()

            assertSameSet(_any + listOf(
            ), Mirror.reflectClass(X).inheritedMethods)
        }

        @Test
        fun inheritedMethods_withGenericMethods_withConcreteOverrideSignature_shouldNotInheritGenericMethods() {
            val Generic by sources.add("Generic", """
                class Generic<T> {
                    void generic(T arg) {}
                }
            """)
            val X by sources.add("X", """
                class X extends Generic<String> {
                    void generic(String arg) {}
                }
            """)
            sources.compile()

            assertSameSet(_any + listOf(
            ), Mirror.reflectClass(X).inheritedMethods)
        }

        @Test
        fun inheritedMethods_withGenericMethods_withConcreteBoundedOverrideSignature_shouldNotInheritGenericMethods() {
            val Generic by sources.add("Generic", """
                class Generic<T> {
                    void generic(T arg) {}
                }
            """)
            val X by sources.add("X", """
                class X<V extends String> extends Generic<V> {
                    void generic(String arg) {}
                }
            """)
            sources.compile()

            assertSameSet(_any + listOf(
            ), Mirror.reflectClass(X).inheritedMethods)
        }
    }
}
