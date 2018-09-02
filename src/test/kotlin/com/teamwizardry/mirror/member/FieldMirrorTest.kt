package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.FieldFlagTestClass
import com.teamwizardry.mirror.testsupport.FieldVisibilityTestClass
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class FieldMirrorTest: MirrorTestBase() {
    private enum class TestEnum {
        FIRST, SECOND;
    }

    @Test
    @DisplayName("Getting the declared fields of an enum type should return fields with the `isEnumConstant` flag " +
            "set to true")
    fun declaredFields_onEnumType() {
        val baseType = Mirror.reflectClass<TestEnum>()
        assertEquals(true, baseType.field("FIRST")?.isEnumConstant)
        assertEquals(true, baseType.field("SECOND")?.isEnumConstant)
    }

    @Test
    @DisplayName("The access levels of fields should be correctly mapped and stored")
    fun field_visibility() {
        val baseType = Mirror.reflectClass<FieldVisibilityTestClass>()
        assertEquals(AccessLevel.PUBLIC, baseType.field("publicField")?.accessLevel)
        assertEquals(AccessLevel.PACKAGE, baseType.field("defaultField")?.accessLevel)
        assertEquals(AccessLevel.PROTECTED, baseType.field("protectedField")?.accessLevel)
        assertEquals(AccessLevel.PRIVATE, baseType.field("privateField")?.accessLevel)
    }

    @Test
    @DisplayName("The access levels of fields should be correctly mapped and stored")
    fun field_flags() {
        val baseType = Mirror.reflectClass<FieldFlagTestClass>()
        assertEquals(true, baseType.field("staticField")?.isStatic)
        assertEquals(true, baseType.field("volatileField")?.isVolatile)
        assertEquals(true, baseType.field("transientField")?.isTransient)
    }
}