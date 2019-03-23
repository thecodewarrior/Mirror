package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.Object1Sub
import com.teamwizardry.mirror.testsupport.assertSameList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class WildcardMirrorTest {
    val holder = TypeMirrorTestAnnotatedTypes()

    @Test
    fun getLowerBounds_onLowerBoundedWildcard_shouldReturnLowerBound() {
        val wildcard = holder["? super Object1Sub"]
        val type = Mirror.reflect(wildcard) as WildcardMirror
        assertSameList(listOf(Mirror.reflect<Object1Sub>()), type.lowerBounds)
    }

    @Test
    fun getLowerBounds_onLowerBoundedAnnotatedWildcard_shouldReturnAnnotatedLowerBound() {
        val wildcard = holder["? super @TypeAnnotation1 Object1"]
        val type = Mirror.reflect(wildcard) as WildcardMirror
        assertSameList(
            listOf(Mirror.reflect(holder["@TypeAnnotation1 Object1"])),
            type.lowerBounds
        )
    }

    @Test
    fun getUpperBounds_onUpperBoundedWildcard_shouldReturnUpperBound() {
        val wildcard = holder["? extends Object1Sub"]
        val type = Mirror.reflect(wildcard) as WildcardMirror
        assertSameList(listOf(Mirror.reflect<Object1Sub>()), type.upperBounds)
    }

    @Test
    fun getUpperBounds_onUpperBoundedAnnotatedWildcard_shouldReturnAnnotatedUpperBound() {
        val wildcard = holder["? extends @TypeAnnotation1 Object1"]
        val type = Mirror.reflect(wildcard) as WildcardMirror
        assertSameList(
            listOf(Mirror.reflect(holder["@TypeAnnotation1 Object1"])),
            type.upperBounds
        )
    }

    @Test
    fun getRaw_onWildcard_shouldReturnItself() {
        val wildcard = holder["? extends Object1Sub"]
        val type = Mirror.reflect(wildcard) as WildcardMirror
        assertEquals(type, type.raw)
    }
}