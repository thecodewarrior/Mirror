@file:Suppress("ClassName", "PropertyName")

package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.member.Modifier
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.assertSameSet
import dev.thecodewarrior.mirror.testsupport.assertSetEquals
import dev.thecodewarrior.mirror.utils.Untested
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.reflect.Method

@Suppress("LocalVariableName")
internal class MethodsTest: MTest() {

    @Untested // just so it shows up in searches
    @Test
    fun everything_onMutationAttempt_shouldThrow() {}

    @Test
    fun `calling getMethod(Method) with a method present in the list should return the mirror`() {
        val X by sources.add("X", "class X<T> { void method1() {} void method2() {}}")
        sources.compile()
        assertEquals(
            Mirror.reflect(X._m("method1")),
            Mirror.reflectClass(X).getMethod(X._m("method1"))
        )
    }

    @Test
    fun `calling getMethod(Method) with a method not present in the list should throw`() {
        val X by sources.add("X", "class X<T> { void method1() {} void method2() {}}")
        sources.compile()
        assertEquals(
            Mirror.reflect(X._m("method1")),
            Mirror.reflectClass(X).getMethod(X._m("method1"))
        )
    }

    @Test
    fun `calling getMethod(Method) on a generic class should return the unspecialized mirror`() {
        val Generic by sources.add("Generic", "class Generic<T> { void method1() {} void method2() {}}")
        sources.compile()
        assertEquals(
            Mirror.reflect(Generic._m("method1")),
            Mirror.reflectClass(Generic).getMethod(Generic._m("method1"))
        )
    }

    @Test
    fun `calling getMethod(Method) on specialized class should return the specialized mirror`() {
        val X by sources.add("X", "class X {}")
        val Generic by sources.add("Generic", "class Generic<T> { void method1(T param) {} void method2() {}}")
        val types = sources.types {
            +"Generic<X>"
        }
        sources.compile()
        assertEquals(
            Mirror.reflectClass(X),
            Mirror.reflectClass(types["Generic<X>"]).getMethod(Generic._m("method1")).parameterTypes[0]
        )
    }

// abstract val declaredMethods: List<MethodMirror> ====================================================================

    @Test
    fun `declaredMethods of an empty interface should be empty`() {
    }

    @Test
    fun `declaredMethods of Object should be `() {
    }

// abstract val publicMethods: List<MethodMirror> ======================================================================

    @Test
    fun `Core Reflection getMethods() on a class doesn't include overridden methods`() {
        val X by sources.add("X", "class X { public void xMethod() {} public void overrideMethod() {} }")
        val Y by sources.add("Y", "class Y extends X { public void yMethod() {} public void overrideMethod() {} }")
        sources.compile()
        assertSetEquals(listOf(
            X._m("xMethod"), Y._m("yMethod"), Y._m("overrideMethod")
        ), Y.methods.toList() - Any::class.java.methods)
    }

    // supposedly fixed in Java 9: https://bugs.openjdk.java.net/browse/JDK-8029674
    @Test
    fun `Core Reflection getMethods() on a class with multiple redundant implements includes duplicate methods`() {
        val I by sources.add("I", "interface I { void overrideMethod(); }")
        val J by sources.add("J", "interface J extends I { void overrideMethod(); }")
        val X by sources.add("X", "abstract class X implements J, I { }")
        sources.compile()
        assertSetEquals(listOf(
            J._m("overrideMethod"), I._m("overrideMethod")
        ), X.methods.toList() - Any::class.java.methods)
    }

    @Test
    fun `'publicMethods' of a class with multiple redundant implements should not have duplicate methods`() {
        val I by sources.add("I", "interface I { void overrideMethod(); }")
        val J by sources.add("J", "interface J extends I { void overrideMethod(); }")
        val X by sources.add("X", "abstract class X implements J, I { }")
        sources.compile()
        assertSetEquals(listOf(
            Mirror.reflect(J._m("overrideMethod"))
        ), Mirror.reflectClass(X).publicMethods.toList() - Mirror.types.any.methods)
    }

    // supposedly fixed in Java 9: https://bugs.openjdk.java.net/browse/JDK-8029674
    @Test
    fun `Core Reflection getMethods() on a class with extends and a redundant implements does not include duplicate methods`() {
        val I by sources.add("I", "interface I { void overrideMethod(); }")
        val J by sources.add("J", "abstract class J implements I { public void overrideMethod() {} }")
        val X by sources.add("X", "abstract class X extends J implements I { }")
        sources.compile()
        assertSetEquals(listOf(
            J._m("overrideMethod")
        ), X.methods.toList() - Any::class.java.methods)
    }

    @Test
    fun `'publicMethods' of a class with extends and a redundant implements should not have duplicate methods`() {
        val I by sources.add("I", "interface I { void overrideMethod(); }")
        val J by sources.add("J", "abstract class J implements I { public void overrideMethod() {} }")
        val X by sources.add("X", "abstract class X extends J implements I { }")
        sources.compile()
        assertSetEquals(listOf(
            Mirror.reflect(J._m("overrideMethod"))
        ), Mirror.reflectClass(X).publicMethods.toList() - Mirror.types.any.methods)
    }

    @Test
    fun `'publicMethods' should not include static methods from interfaces`() {
        val I by sources.add("I", "interface I { public static void overrideMethod() {} }")
        val X by sources.add("X", "abstract class X implements I { }")
        sources.compile()
        assertSetEquals(listOf(), Mirror.reflectClass(X).publicMethods.toList() - Mirror.types.any.methods)
    }

    @Test
    fun `Core Reflection getMethods() on an empty interface should be empty`() {
        val I by sources.add("I", "interface I {}")
        sources.compile()
        assertEquals(emptyList<Method>(), I.methods.toList())
    }

    @Test
    fun `'methods' of an empty interface should be empty`() {
        val I by sources.add("I", "interface I {}")
        sources.compile()
        assertEquals(emptyList<MethodMirror>(), Mirror.reflectClass(I).publicMethods)
    }

    @Test
    fun `Core Reflection getMethods() on an interface with one method should contain one method`() {
        val I by sources.add("I", "interface I { void method(); }")
        sources.compile()
        assertEquals(listOf(I._m("method")), I.methods.toList())
    }

    @Test
    fun `'methods' of an interface with one method should contain one mirror`() {
        val I by sources.add("I", "interface I { void method(); }")
        sources.compile()
        assertEquals(listOf(Mirror.reflect(I._m("method"))), Mirror.reflectClass(I).publicMethods)
    }

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
        fun `'inheritedMethods' of 'Object' class should be empty`() {
            assertSameSet(emptyList<MethodMirror>(), Mirror.reflectClass<Any>().inheritedMethods)
        }

        @Test
        fun `'inheritedMethods' of an interface should be empty`() {
            val I by sources.add("I", "interface I {}")
            sources.compile()
            assertSameSet(emptyList<MethodMirror>(), Mirror.reflectClass(I).inheritedMethods)
        }

        @Test
        fun `'inheritedMethods' of an empty class should contain the Object methods`() {
            val X by sources.add("X", "class X {}")
            sources.compile()
            assertSameSet(_any, Mirror.reflectClass(X).inheritedMethods)
        }

        @Test
        fun `'inheritedMethods' with an empty superclass should contain the Object methods`() {
            val X by sources.add("X", "class X {}")
            val Y by sources.add("Y", "class Y {}")
            sources.compile()
            assertSameSet(_any, Mirror.reflectClass(Y).inheritedMethods)
        }

        @Test
        fun `'inheritedMethods' with the superclass in the same package should contain all its non-private methods`() {
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
        fun inheritedMethods_viaClassInSamePackage_shouldInheritNonPrivate() {
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

        @Test
        fun `'inheritedMethods' of a class with multiple redundant implements should not have duplicate methods`() {
            val I by sources.add("I", "interface I { void overrideMethod(); }")
            val J by sources.add("J", "interface J extends I { void overrideMethod(); }")
            val X by sources.add("X", "abstract class X implements J, I { }")
            sources.compile()
            assertSetEquals(listOf(
                Mirror.reflect(J._m("overrideMethod"))
            ), Mirror.reflectClass(X).inheritedMethods.toList() - Mirror.types.any.methods)
        }

        @Test
        fun `'inheritedMethods' of a class with extends and a redundant implements should not have duplicate methods`() {
            val I by sources.add("I", "interface I { void overrideMethod(); }")
            val J by sources.add("J", "abstract class J implements I { public void overrideMethod() {} }")
            val X by sources.add("X", "abstract class X extends J implements I { }")
            sources.compile()
            assertSetEquals(listOf(
                Mirror.reflect(J._m("overrideMethod"))
            ), Mirror.reflectClass(X).inheritedMethods.toList() - Mirror.types.any.methods)
        }

        @Test
        fun `'inheritedMethods' should not include static methods from interfaces`() {
            val I by sources.add("I", "interface I { public static void overrideMethod() {} }")
            val X by sources.add("X", "abstract class X implements I { }")
            sources.compile()
            assertSetEquals(listOf(), Mirror.reflectClass(X).inheritedMethods.toList() - Mirror.types.any.methods)
        }
    }
}
