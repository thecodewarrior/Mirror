package com.teamwizardry.mirror.reflection.type

import com.teamwizardry.librarianlib.commons.reflection.typeParameter
import com.teamwizardry.mirror.reflection.Mirror
import com.teamwizardry.mirror.reflection.testsupport.Interface1
import com.teamwizardry.mirror.reflection.testsupport.Interface2
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class VariableMirrorTest {

    @Test
    fun getBounds_onUnboundedType_shouldReturnListOfObject() {
        class TypeVariableHolder<T>
        val typeVariable = TypeVariableHolder::class.java.typeParameter(0)!!
        val type = Mirror.reflect(typeVariable) as VariableMirror
        assertEquals(listOf(Mirror.reflect<Any>()), type.bounds)
    }

    @Test
    fun getBounds_onTypeWithSingleBound_shouldReturnListOfBound() {
        class TypeVariableHolder<T: Interface1>
        val typeVariable = TypeVariableHolder::class.java.typeParameter(0)!!
        val type = Mirror.reflect(typeVariable) as VariableMirror
        assertEquals(listOf(Mirror.reflect<Interface1>()), type.bounds)
    }

    @Test
    fun getBounds_onTypeWithMultipleBounds_shouldReturnListOfBoundsInSourceOrder() {
        class TypeVariableHolder<T> where T: Interface1, T: Interface2
        val typeVariable = TypeVariableHolder::class.java.typeParameter(0)!!
        val type = Mirror.reflect(typeVariable) as VariableMirror
        assertEquals(listOf(Mirror.reflect<Interface1>(), Mirror.reflect<Interface2>()), type.bounds)
    }
}