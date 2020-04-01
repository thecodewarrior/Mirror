package dev.thecodewarrior.mirror.coretypes

import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * ## [Java Language Specification](https://docs.oracle.com/javase/specs/jls/se13/html/index.html)
 * ### [8.4.8.1.](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-8.4.8.1) Overriding (by Instance Methods)
 */
@Suppress("LocalVariableName")
internal class MethodOverrideTesterTest: MTest() {
    /**
     * An instance method `mC` declared in or inherited by class `C`, overrides from `C` another method `mA` declared
     * in class `A`, iff all of the following are true:
     *  - `C` is a subclass of `A`.
     *  - `C` does not inherit `mA`.
     *  - The signature of `mC` is a subsignature (§8.4.2) of the signature of `mA`.
     *  - One of the following is true:
     *    - `mA` is public.
     *    - `mA` is protected.
     *    - `mA` is declared with package access in the same package as `C`, and either `C` declares `mC` or `mA` is a
     *    member of the direct superclass of `C`.
     *    - `mA` is declared with package access and `mC` overrides `mA` from some superclass of `C`.
     *    - `mA` is declared with package access and `mC` overrides `a` method `m'` from `C` (`m'` distinct from `mC`
     *    and `mA`), such that `m'` overrides `mA` from some superclass of `C`.
     *
     */
    @Nested
    inner class FromSuperclass {
        @Test
        fun isOverriddenBy_withBasicOverride_shouldReturnTrue() {
            val sources = TestSources()
            val A by sources.add("A", "class A { void method() {} }")
            val C by sources.add("C", "class C extends A { void method() {} }")
            sources.compile()
            assertTrue(MethodOverrideTester.isOverridenBy(A._m("method"), C._m("method")))
        }

        @Test
        fun isOverriddenBy_withUnrelatedClass_shouldReturnFalse() {
            val sources = TestSources()
            val A by sources.add("A", "class A { void method() {} }")
            val C by sources.add("C", "class C { void method() {} }")
            sources.compile()
            assertFalse(MethodOverrideTester.isOverridenBy(A._m("method"), C._m("method")))
        }

        @Test
        fun isOverriddenBy_withUnrelatedSignature_shouldReturnFalse() {
            val sources = TestSources()
            val A by sources.add("A", "class A { void method() {} }")
            val C by sources.add("C", "class C extends A { void method(String string) {} }")
            sources.compile()
            assertFalse(MethodOverrideTester.isOverridenBy(A._m("method"), C._m("method")))
        }
    }
}