package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.annotations.Annotation1
import dev.thecodewarrior.mirror.annotations.AnnotationArg1
import dev.thecodewarrior.mirror.testsupport.FieldFlagTestClass
import dev.thecodewarrior.mirror.testsupport.FieldVisibilityTestClass
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.assertSetEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class FieldMirrorTest: MirrorTestBase() {
    private enum class TestEnum {
        FIRST, SECOND;
    }

    @Test
    @DisplayName("Reflecting a field should return a field mirror with the correct name and type")
    fun reflectingBasicField() {
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
    @DisplayName("Getting the declared fields of an enum type should return fields with the `isEnumConstant` flag " +
            "set to true")
    fun declaredFields_onEnumType() {
        val baseType = Mirror.reflectClass<TestEnum>()
        assertEquals(true, baseType.findPublicField("FIRST")?.isEnumConstant)
        assertEquals(true, baseType.findPublicField("SECOND")?.isEnumConstant)
    }

    @Test
    @DisplayName("The access levels of fields should be correctly mapped and stored")
    fun field_visibility() {
        val baseType = Mirror.reflectClass<FieldVisibilityTestClass>()
        assertEquals(Modifier.Access.PUBLIC, baseType.findPublicField("publicField")?.access)
        assertEquals(Modifier.Access.DEFAULT, baseType.findPublicField("defaultField")?.access)
        assertEquals(Modifier.Access.PROTECTED, baseType.findPublicField("protectedField")?.access)
        assertEquals(Modifier.Access.PRIVATE, baseType.findPublicField("privateField")?.access)
    }

    @Test
    @DisplayName("The access levels of fields should be correctly mapped and stored")
    fun field_flags() {
        val baseType = Mirror.reflectClass<FieldFlagTestClass>()
        assertEquals(true, baseType.findPublicField("staticField")?.isStatic)
        assertEquals(true, baseType.findPublicField("volatileField")?.isVolatile)
        assertEquals(true, baseType.findPublicField("transientField")?.isTransient)
    }

    @Test
    @DisplayName("A field that has no annotations should have an empty annotations list")
    fun nonAnnotatedField() {
        class FieldHolder {
            val field: String? = null
        }
        val field = Mirror.reflect(FieldHolder::class.java.getDeclaredField("field"))
        assertEquals(emptyList<Annotation>(), field.annotations)
    }

    @Test
    @DisplayName("A field that has annotations should have an annotations list containing those annotations")
    fun annotatedField() {
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