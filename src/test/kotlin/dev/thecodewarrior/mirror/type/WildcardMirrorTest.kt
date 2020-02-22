package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import dev.thecodewarrior.mirror.testsupport.assertSameList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class WildcardMirrorTest: MTest() {
    val sources = TestSources()

    val A by sources.add("A", "@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.RUNTIME) @interface A {}")
    val X by sources.add("X", "class X {}")

    val types = sources.types {
        +"? super X"
        +"? super @A X"
        +"@A X"
        +"? extends X"
        +"? extends @A X"
    }

    init {
        sources.compile()
    }

    @Test
    fun getLowerBounds_onLowerBoundedWildcard_shouldReturnLowerBound() {
        val wildcard = types["? super X"]
        val type = Mirror.reflect(wildcard) as WildcardMirror
        assertSameList(listOf(Mirror.reflect(X)), type.lowerBounds)
    }

    @Test
    fun getLowerBounds_onLowerBoundedAnnotatedWildcard_shouldReturnAnnotatedLowerBound() {
        val wildcard = types["? super @A X"]
        val type = Mirror.reflect(wildcard) as WildcardMirror
        assertSameList(
            listOf(Mirror.reflect(types["@A X"])),
            type.lowerBounds
        )
    }

    @Test
    fun getUpperBounds_onUpperBoundedWildcard_shouldReturnUpperBound() {
        val wildcard = types["? extends X"]
        val type = Mirror.reflect(wildcard) as WildcardMirror
        assertSameList(listOf(Mirror.reflect(X)), type.upperBounds)
    }

    @Test
    fun getUpperBounds_onUpperBoundedAnnotatedWildcard_shouldReturnAnnotatedUpperBound() {
        val wildcard = types["? extends @A X"]
        val type = Mirror.reflect(wildcard) as WildcardMirror
        assertSameList(
            listOf(Mirror.reflect(types["@A X"])),
            type.upperBounds
        )
    }

    @Test
    fun getRaw_onWildcard_shouldReturnItself() {
        val wildcard = types["? extends X"]
        val type = Mirror.reflect(wildcard) as WildcardMirror
        assertEquals(type, type.raw)
    }
}