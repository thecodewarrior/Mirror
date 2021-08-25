package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("LocalVariableName")
internal class IsKotlinTest: MTest() {

    @Test
    fun `'isKotlinClass' should return true for Kotlin classes`() {
        class X {}
        val mirror = Mirror.reflectClass(X::class.java)
        assertTrue(mirror.isKotlinClass)
    }

    @Test
    fun `'isKotlinClass' should return false for Java classes`() {
        val X by sources.add("X", "class X {}")
        sources.compile()
        val mirror = Mirror.reflectClass(X)
        assertFalse(mirror.isKotlinClass)
    }

}
