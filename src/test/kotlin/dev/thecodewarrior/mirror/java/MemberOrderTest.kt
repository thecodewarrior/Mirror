package dev.thecodewarrior.mirror.java

import dev.thecodewarrior.mirror.testsupport.MTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * A bunch of tests that should probably fail, just to quantify Java's bizarre member ordering.
 *
 * Fields:
 * - Java: in declaration order
 * - Kotlin: in declaration order
 *
 * Methods:
 * - Java: mangled
 * - Kotlin: mangled
 *
 * Constructors:
 * - Java: mangled
 * - Kotlin: mangled
 *
 * Member classes:
 * - Java: in reverse(!) order
 * - Kotlin: in declaration order
 */
@Suppress("LocalVariableName")
internal class MemberOrderTest : MTest() {
    // The order for members hops back and forth from start to end, working its way toward the middle
    // A C E ... F D B
    // acegikmoqsuwyzxvtrpnljhfdb
    // ACEGIKMOQSUWYZXVTRPNLJHFDB

//    @Test
    fun `java field order`() {
        val Alphabet by sources.add(
            "Alphabet", """
            class Alphabet {
                int a; int c; static int e; int g; int i; int k; int m; int o; int q; int s; static int u; int w; int y; 
                int z; int x; static int v; int t; int r; int p; int n; int l; int j; int h; static int f; int d; int b; 
            }
            """.trimIndent()
        )
        // because who knows, the compiler might do them differently
        val CommaAlphabet by sources.add(
            "CommaAlphabet", """
            class CommaAlphabet {
                int a,c,e,g,i,k,m,o,q,s,u,w,y,z,x,v,t,r,p,n,l,j,h,f,d,b;
            }
        """.trimIndent()
        )
        sources.compile()

        assertAll(
            { assertEquals("acegikmoqsuwyzxvtrpnljhfdb", Alphabet.declaredFields.joinToString("") { it.name }) },
            { assertEquals("acegikmoqsuwyzxvtrpnljhfdb", CommaAlphabet.declaredFields.joinToString("") { it.name }) },
        )
    }

    class AlphabetFields {
        val a = 0
        val c = 0
        val e = 0
        val g = 0
        val i = 0
        val k = 0
        val m = 0
        val o = 0
        val q = 0
        val s = 0
        val u = 0
        val w = 0
        val y = 0
        val z = 0
        val x = 0
        val v = 0
        val t = 0
        val r = 0
        val p = 0
        val n = 0
        val l = 0
        val j = 0
        val h = 0
        val f = 0
        val d = 0
        val b = 0
    }

//    @Test
    fun `kotlin field order`() {
        assertEquals(
            "acegikmoqsuwyzxvtrpnljhfdb",
            AlphabetFields::class.java.declaredFields.joinToString("") { it.name }
        )
    }

//    @Test
    fun `java method order`() {
        val Alphabet by sources.add(
            "Alphabet", """
            class Alphabet {
                void a() {} void c() {} static void e() {} void g() {} void i() {} static void k() {} 
                void m() {} void o() {} static void q() {} void s() {} void u() {} static void w() {}
                void y() {} void z() {} static void x() {} void v() {} void t() {} static void r() {} 
                void p() {} void n() {} static void l() {} void j() {} void h() {} static void f() {}
                void d() {} void b() {} 
            }
            """
        )
        sources.compile()

        assertEquals("acegikmoqsuwyzxvtrpnljhfdb", Alphabet.declaredMethods.joinToString("") { it.name })
    }

    class AlphabetMethods {
        fun a() = 0
        fun c() = 0
        fun e() = 0
        fun g() = 0
        fun i() = 0
        fun k() = 0
        fun m() = 0
        fun o() = 0
        fun q() = 0
        fun s() = 0
        fun u() = 0
        fun w() = 0
        fun y() = 0
        fun z() = 0
        fun x() = 0
        fun v() = 0
        fun t() = 0
        fun r() = 0
        fun p() = 0
        fun n() = 0
        fun l() = 0
        fun j() = 0
        fun h() = 0
        fun f() = 0
        fun d() = 0
        fun b() = 0
    }

//    @Test
    fun `kotlin method order`() {
        assertEquals(
            "acegikmoqsuwyzxvtrpnljhfdb",
            AlphabetMethods::class.java.declaredMethods.joinToString("") { it.name }
        )
    }

//    @Test
    fun `java constructor order`() {
        for (letter in "ACEGIKMOQSUWYZXVTRPNLJHFDB") {
            sources.add("$letter", "class $letter {}")
        }
        val Alphabet by sources.add(
            "__", """
            class __ {
                __(A v) {} __(C v) {} __(E v) {} __(G v) {} __(I v) {} __(K v) {} __(M v) {} __(O v) {} __(Q v) {} 
                __(S v) {} __(U v) {} __(W v) {} __(Y v) {} __(Z v) {} __(X v) {} __(V v) {} __(T v) {} __(R v) {} 
                __(P v) {} __(N v) {} __(L v) {} __(J v) {} __(H v) {} __(F v) {} __(D v) {} __(B v) {} 
            }
            """
        )
        sources.compile()

        assertEquals("ACEGIKMOQSUWYZXVTRPNLJHFDB", Alphabet.declaredConstructors.joinToString("") {
            it.parameterTypes[0].simpleName
        })
    }

    private class AlphabetConstructors {
        constructor(v: A)
        constructor(v: C)
        constructor(v: E)
        constructor(v: G)
        constructor(v: I)
        constructor(v: K)
        constructor(v: M)
        constructor(v: O)
        constructor(v: Q)
        constructor(v: S)
        constructor(v: U)
        constructor(v: W)
        constructor(v: Y)
        constructor(v: Z)
        constructor(v: X)
        constructor(v: V)
        constructor(v: T)
        constructor(v: R)
        constructor(v: P)
        constructor(v: N)
        constructor(v: L)
        constructor(v: J)
        constructor(v: H)
        constructor(v: F)
        constructor(v: D)
        constructor(v: B)
    }

//    @Test
    fun `kotlin constructor order`() {
        assertEquals(
            "ACEGIKMOQSUWYZXVTRPNLJHFDB",
            AlphabetConstructors::class.java.declaredConstructors.joinToString("") {
                it.parameterTypes[0].simpleName
            }
        )
    }

//    @Test
    fun `member class order`() {
        val Alphabet by sources.add(
            "Alphabet", """
            class Alphabet {
                class A {} class C {} static class E {} class G {} class I {} static class K {} 
                class M {} class O {} static class Q {} class S {} class U {} static class W {} 
                class Y {} class Z {} static class X {} class V {} class T {} static class R {} 
                class P {} class N {} static class L {} class J {} class H {} static class F {}
                class D {} class B {} 
            }
        """.trimIndent()
        )
        sources.compile()

        assertEquals("ACEGIKMOQSUWYZXVTRPNLJHFDB", Alphabet.declaredClasses.joinToString("") { it.simpleName })
    }

//    @Test
    fun `reversed member class order`() {
        val Alphabet by sources.add(
            "Alphabet", """
            class Alphabet {
                class A {} class C {} static class E {} class G {} class I {} static class K {} 
                class M {} class O {} static class Q {} class S {} class U {} static class W {} 
                class Y {} class Z {} static class X {} class V {} class T {} static class R {} 
                class P {} class N {} static class L {} class J {} class H {} static class F {}
                class D {} class B {} 
            }
        """.trimIndent()
        )
        sources.compile()

        assertEquals(
            "ACEGIKMOQSUWYZXVTRPNLJHFDB".reversed(),
            Alphabet.declaredClasses.joinToString("") { it.simpleName })
    }

    class AlphabetClasses {
        class A
        class C
        inner class E
        class G
        class I
        inner class K
        class M
        class O
        inner class Q
        class S
        class U
        inner class W
        class Y
        class Z
        inner class X
        class V
        class T
        inner class R
        class P
        class N
        inner class L
        class J
        class H
        inner class F
        class D
        class B
    }

//    @Test
    fun `kotlin member class order`() {
        assertEquals(
            "ACEGIKMOQSUWYZXVTRPNLJHFDB",
            AlphabetClasses::class.java.declaredClasses.joinToString("") { it.simpleName }
        )
    }

    private class A
    private class C
    private class E
    private class G
    private class I
    private class K
    private class M
    private class O
    private class Q
    private class S
    private class U
    private class W
    private class Y
    private class Z
    private class X
    private class V
    private class T
    private class R
    private class P
    private class N
    private class L
    private class J
    private class H
    private class F
    private class D
    private class B
}