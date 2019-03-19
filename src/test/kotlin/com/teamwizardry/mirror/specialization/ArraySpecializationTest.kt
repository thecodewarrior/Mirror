package com.teamwizardry.mirror.specialization

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.Object1
import com.teamwizardry.mirror.type.ArrayMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.reflect.AnnotatedArrayType

internal class ArraySpecializationTest: MirrorTestBase() {
    @Test
    @DisplayName("Specializing generic array should return an array mirror with the new type")
    fun basicSpecialization() {
        class GenericArrayHolder<T>(
            val array: Array<T>
        )
        val genericType = Mirror.reflectClass(GenericArrayHolder::class.java)
        val specializeWith = Mirror.reflectClass<Object1>()
        val specialized = genericType.specialize(specializeWith)
        val specializedArray = specialized.field("array")!!.type as ArrayMirror

        assertEquals(specializeWith, specializedArray.component)
    }

    @Test
    fun getRawClass_onGenericArray_shouldReturnObjectArray() {
        class FieldHolder<T>(
            @JvmField
            val field: Array<T>
        )
        val genericArray = FieldHolder::class.java.getField("field").annotatedType as AnnotatedArrayType
        val type = Mirror.reflect(genericArray) as ArrayMirror
        assertEquals(Mirror.reflect<Array<Any>>(), type.raw)
    }
}