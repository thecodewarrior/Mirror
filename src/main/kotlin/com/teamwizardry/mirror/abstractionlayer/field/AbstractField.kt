package com.teamwizardry.mirror.abstractionlayer.field

import com.teamwizardry.mirror.abstractionlayer.type.AbstractType
import java.lang.reflect.Field

internal class AbstractField(val field: Field) {
    val name: String = field.name
    val isEnumConstant = field.isEnumConstant
    val type by lazy { AbstractType.create(field.genericType) }
    val modifiers = field.modifiers

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