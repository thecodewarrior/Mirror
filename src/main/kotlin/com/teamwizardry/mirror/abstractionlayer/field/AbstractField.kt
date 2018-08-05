package com.teamwizardry.mirror.abstractionlayer.field

import com.teamwizardry.mirror.abstractionlayer.type.AbstractClass
import com.teamwizardry.mirror.abstractionlayer.type.AbstractType
import java.lang.reflect.Field

internal class AbstractField(val field: Field) {
    val name = field.name
    val isEnumConstant = field.isEnumConstant
    val declaringClass by lazy { AbstractClass(field.declaringClass) }
    val type by lazy { AbstractType.create(field.genericType) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractField) return false

        if (field != other.field) return false

        return true
    }

    override fun hashCode(): Int {
        return field.hashCode()
    }
}