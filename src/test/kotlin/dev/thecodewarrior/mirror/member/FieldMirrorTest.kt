package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.annotations.Annotation1
import dev.thecodewarrior.mirror.annotations.AnnotationArg1
import dev.thecodewarrior.mirror.testsupport.FieldFlagTestClass
import dev.thecodewarrior.mirror.testsupport.FieldVisibilityTestClass
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.assertSetEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FieldMirrorTest: MirrorTestBase() {
    private enum class TestEnum {
        FIRST, SECOND;

        @JvmField
        val nonConstant = 1
    }

    @Test
    fun reflect_withField_shouldHaveCorrectNameAndType() {
        class FieldHolder(
            @JvmField
            var field: String
        )

        val field = FieldHolder::class.java.getField("field")

        val fieldMirror = Mirror.reflect(field)
        assertEquals("field", fieldMirror.name)
        val fieldType = fieldMirror.type
        assertEquals(Mirror.reflect<String>(), fieldType)
    }

    @Test
    fun isEnumConstant_withEnumConstant_shouldReturnTrue() {
        val baseType = Mirror.reflectClass<TestEnum>()
        assertTrue(baseType.getPublicField("FIRST").isEnumConstant)
        assertTrue(baseType.getPublicField("SECOND").isEnumConstant)
    }

    @Test
    fun isEnumConstant_withNonEnumConstant_shouldReturnFalse() {
        val baseType = Mirror.reflectClass<TestEnum>()
        assertFalse(baseType.getPublicField("nonConstant").isEnumConstant)
    }

    @Test
    fun access_ofFields_shouldBeCorrect() {
        val baseType = Mirror.reflectClass<FieldVisibilityTestClass>()
        assertEquals(Modifier.Access.PUBLIC, baseType.getPublicField("publicField").access)
        assertEquals(Modifier.Access.DEFAULT, baseType.getPublicField("defaultField").access)
        assertEquals(Modifier.Access.PROTECTED, baseType.getPublicField("protectedField").access)
        assertEquals(Modifier.Access.PRIVATE, baseType.getPublicField("privateField").access)
    }

    @Test
    fun modifiers_ofFields_shouldBeCorrect() {
        val baseType = Mirror.reflectClass<FieldFlagTestClass>()
        fun test(name: String, static: Boolean, volatile: Boolean, transient: Boolean) {
            val field = baseType.getPublicField(name)
            assertEquals(listOf(static, volatile, transient), listOf(field.isStatic, field.isVolatile, field.isTransient))
        }
        test("staticField", static = true, volatile = false, transient = false)
        test("volatileField", static = false, volatile = true, transient = false)
        test("transientField", static = false, volatile = false, transient = true)
    }

    @Test
    fun annotations_withNonAnnotatedField_shouldReturnNoAnnotations() {
        class FieldHolder {
            val field: String? = null
        }
        val field = Mirror.reflect(FieldHolder::class.java.getDeclaredField("field"))
        assertEquals(emptyList<Annotation>(), field.annotations)
    }

    @Test
    fun annotations_withAnnotatedField_shouldReturnAnnotations() {
        class FieldHolder {
            @field:Annotation1
            @field:AnnotationArg1(arg = 1)
            val field: String? = null
        }
        val field = Mirror.reflect(FieldHolder::class.java.getDeclaredField("field"))
        assertSetEquals(listOf(
            Mirror.newAnnotation<Annotation1>(),
            Mirror.newAnnotation<AnnotationArg1>("arg" to 1)
        ), field.annotations)
    }
}