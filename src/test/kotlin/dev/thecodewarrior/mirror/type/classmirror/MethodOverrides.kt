package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

@Suppress("LocalVariableName")
internal class MethodOverrides: MTest() {
    @Test
    fun `methods should not override themselves`() {
        val X by sources.add("X", "public class X { public void method() {} }")
        sources.compile()
        assertFalse(Mirror.reflect(X._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `basic override`() {
        val X by sources.add("X", "public class X { public void method() {} }")
        val Y by sources.add("Y", "public class Y extends X { public void method() {} }")
        sources.compile()
        assertTrue(Mirror.reflect(Y._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `backward override`() {
        val X by sources.add("X", "public class X { public void method() {} }")
        val Y by sources.add("Y", "public class Y extends X { public void method() {} }")
        sources.compile()
        assertFalse(Mirror.reflect(X._m("method")).doesOverride(Y._m("method")))
    }

    @Test
    fun `private methods should not be overridden`() {
        val X by sources.add("X", "public class X { private void method() {} }")
        val Y by sources.add("Y", "public class Y extends X { public void method() {} }")
        sources.compile()
        assertFalse(Mirror.reflect(Y._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `package-private methods in separate packages should not be overridden`() {
        val X by sources.add("sub.X", "public class X { void method() {} }")
        val Y by sources.add("Y", "public class Y extends gen.sub.X { public void method() {} }")
        sources.compile()
        assertFalse(Mirror.reflect(Y._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `methods with different parameters should not be overridden`() {
        val X by sources.add("X", "public class X { public void method(int a) {} }")
        val Y by sources.add("Y", "public class Y extends X { public void method(float a) {} }")
        sources.compile()
        assertFalse(Mirror.reflect(Y._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `methods with different isAssignable parameters should not be overridden`() {
        val A by sources.add("A", "public class A {}")
        val X by sources.add("X", "public class X { public void method(Object a) {} }")
        val Y by sources.add("Y", "public class Y extends X { public void method(A a) {} }")
        sources.compile()
        assertFalse(Mirror.reflect(Y._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `methods in unrelated classes should not be overridden`() {
        val X by sources.add("X", "public class X { public void method() {} }")
        val Y by sources.add("Y", "public class Y { public void method() {} }")
        sources.compile()
        assertFalse(Mirror.reflect(Y._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `abstract methods should be overridden`() {
        val X by sources.add("X", "public abstract class X { public abstract void method(); }")
        val Y by sources.add("Y", "public class Y extends X { public void method() {} }")
        sources.compile()
        assertTrue(Mirror.reflect(Y._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `interfaces should not override Object methods`() {
        // due to potential artifacts with the fact that Object is assignable from interfaces, an interface method might
        // believe it overrides an Object method
        val I by sources.add("I", "public interface I { String toString(); }")
        sources.compile()
        assertFalse(Mirror.reflect(I._m("toString")).doesOverride(_object._m("toString")))
    }

    @Test
    fun `interfaces should override superinterface methods`() {
        val X by sources.add("X", "public interface X { void method(); }")
        val Y by sources.add("Y", "public interface Y extends X { void method(); }")
        sources.compile()
        assertTrue(Mirror.reflect(Y._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `interface methods should be overridden`() {
        val I by sources.add("I", "public interface I { void method(); }")
        val Y by sources.add("Y", "public class Y implements I { public void method() {} }")
        sources.compile()
        assertTrue(Mirror.reflect(Y._m("method")).doesOverride(I._m("method")))
    }

    @Test
    fun `unrelated interface methods should not be overridden`() {
        val I by sources.add("I", "public interface I { void method(); }")
        val Y by sources.add("Y", "public class Y { public void method() {} }")
        sources.compile()
        assertFalse(Mirror.reflect(Y._m("method")).doesOverride(I._m("method")))
    }

    @Test
    fun `methods should override both superclass and interfaces`() {
        val I by sources.add("I", "public interface I { void method(); }")
        val X by sources.add("X", "public class X { public void method() {} }")
        val Y by sources.add("Y", "public class Y extends X implements I { public void method() {} }")
        sources.compile()
        assertTrue(Mirror.reflect(Y._m("method")).doesOverride(I._m("method")))
        assertTrue(Mirror.reflect(Y._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `methods should override superclass and superclass's interfaces`() {
        val I by sources.add("I", "public interface I { public void method(); }")
        val X by sources.add("X", "public class X implements I { public void method() {} }")
        val Y by sources.add("Y", "public class Y extends X { public void method() {} }")
        sources.compile()
        assertTrue(Mirror.reflect(Y._m("method")).doesOverride(I._m("method")))
        assertTrue(Mirror.reflect(Y._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `package-private method hidden by interim class outside of package should not be overridden`() {
        val X by sources.add("sub.X", "public class X { void method() {} }")
        val Y by sources.add("Y", "public class Y extends gen.sub.X { public void method() {} }")
        val Z by sources.add("sub.Z", "public class Z extends Y { public void method() {} }")
        sources.compile()
        assertTrue(Mirror.reflect(Z._m("method")).doesOverride(Y._m("method")))
        assertFalse(Mirror.reflect(Z._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `package-private method not hidden by interim class outside of package should be overridden`() {
        val X by sources.add("sub.X", "public class X { void method() {} }")
        val Y by sources.add("Y", "public class Y extends gen.sub.X { }")
        val Z by sources.add("sub.Z", "public class Z extends Y { void method() {} }")
        sources.compile()
        assertTrue(Mirror.reflect(Z._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `overriding a package-private method via a public override should override the original method`() {
        val X by sources.add("sub.X", "public class X { void method() {} }")
        val Y by sources.add("sub.Y", "public class Y extends X { public void method() {} }")
        val Z by sources.add("Z", "public class Z extends gen.sub.Y { public void method() {} }")
        sources.compile()
        assertTrue(Mirror.reflect(Z._m("method")).doesOverride(X._m("method")))
    }

    @Test
    fun `methods should override generic superclass when erasures match the specialized superclass`() {
        val X by sources.add("X", "public class X<T> { void method(T t) {} }")
        val Y by sources.add("Y", "public class Y extends X<String> { void method(String t) {} }")
        sources.compile()
        assertTrue(Mirror.reflect(Y._m("method", _c<String>())).doesOverride(X._m("method")))
    }

    @Test
    fun `methods should not override generic superclass when erasures don't match the specialized superclass`() {
        val X by sources.add("X", "public class X<T> { void method(T t) {} }")
        val Y by sources.add("Y", "public class Y extends X<Y> { void method(String t) {} }")
        sources.compile()
        assertFalse(Mirror.reflect(Y._m("method", _c<String>())).doesOverride(X._m("method")))
    }

    @Test
    fun `specialization shouldn't change method override`() {
        val X by sources.add("X", "public class X { void method(String s) {} }")
        val Y by sources.add("Y", "public class Y<T> extends X { void method(T t) {} }")
        val types = sources.types {
            +"Y<String>"
        }
        sources.compile()
        assertFalse(Mirror.reflect(Y._m("method")).doesOverride(X._m("method")))
        val specializedMethod = Mirror.reflectClass(types["Y<String>"]).declaredMethods.get("method", Mirror.reflect<String>())
        assertFalse(specializedMethod.doesOverride(X._m("method")))
        assertNull(specializedMethod.overrides)
    }

    @Test
    fun `methods should override when signatures match but generic superclass specialization doesn't match`() {
        val X by sources.add("X", "public class X { void method() {} }")
        val Y by sources.add("Y", "public class Y<T> extends X { void method() {} }")
        val types = sources.types {
            +"Y<String>"
        }
        sources.compile()
        assertTrue(Mirror.reflectClass(types["Y<String>"]).declaredMethods.get("method").doesOverride(X._m("method")))
    }

    @Test
    fun `specialized generic methods should override based on their raw type parameters`() {
        val X by sources.add("X", "public class X { void method(String t) {} }")
        val Y by sources.add("Y", "public class Y extends X { <T> void method(T t) {} }")
        sources.compile()
        assertFalse(Mirror.reflect(Y._m("method")).withTypeParameters(Mirror.reflect(_c<String>()))
            .doesOverride(X._m("method")))
    }

    @Test
    fun `methods should override even when multiple bridge methods are chained`() {
        val A by sources.add("A", "public class A {}")
        val B by sources.add("B", "public class B extends A {}")
        val X by sources.add("X", "public class X<T> { void method(T t) {} }")
        val Y by sources.add("Y", "public class Y<T extends A> extends X<T> { void method(T t) {} }")
        val Z by sources.add("Z", "public class Z extends Y<B> { void method(B t) {} }")
        sources.compile()
        assertTrue(Mirror.reflect(Z._m("method", B)).doesOverride(X._m("method")))
    }

    @Test
    fun `methods should override the generic supertype and not override the interim methods using erased type variables`() {
        val A by sources.add("A", "public class A {}")
        val B by sources.add("B", "public class B extends A {}")
        val X by sources.add("X", "public class X<T> { void method(T t) {} }")
        val Y by sources.add("Y", "public class Y<T extends A> extends X<T> { void method(A t) {} }")
        val Z by sources.add("Z", "public class Z extends Y<B> { void method(B t) {} }")
        sources.compile()
        assertTrue(Mirror.reflect(Z._m("method", B)).doesOverride(X._m("method")))
        assertTrue(Mirror.reflect(Y._m("method", A)).doesOverride(X._m("method")))
        assertFalse(Mirror.reflect(Z._m("method", B)).doesOverride(Y._m("method", A)))
    }

    @Test
    fun `methods should override even when multiple bridge methods are present`() {
        val A by sources.add("A", "public class A {}")
        val B by sources.add("B", "public class B extends A {}")
        // X will have an erased method `public void method(Object t)`
        val X by sources.add("X", "public class X<T> { public void method(T t) {} }")
        // I will have an erased method `public void method(A t)`
        val I by sources.add("I", "public interface I<T extends A> { void method(T t); }")
        // Y will have bridge methods to forward calls to `X.method(Object)` and `I.method(A)` to its own `Y.method(B)`
        val Y by sources.add("Y", "public class Y extends X<B> implements I<B> { public void method(B t) {} }")
        sources.compile()
        assertTrue(Mirror.reflect(Y._m("method", B)).doesOverride(I._m("method")))
        assertTrue(Mirror.reflect(Y._m("method", B)).doesOverride(X._m("method")))
    }

    @Test
    fun `methods should not mistake other methods' bridge methods as their own`() {
        val A by sources.add("A", "public class A {}")
        val X by sources.add("X", "public class X<T> { public void method(T t) {} }")
        val Y by sources.add("Y", """
            public class Y extends X<A> {
                public void method(A t) {}
                public void method(String s) {} 
            }
        """)
        sources.compile()
        assertTrue(Mirror.reflect(Y._m("method", A)).doesOverride(X._m("method")))
        assertFalse(Mirror.reflect(Y._m("method", _c<String>())).doesOverride(X._m("method")))
    }
}