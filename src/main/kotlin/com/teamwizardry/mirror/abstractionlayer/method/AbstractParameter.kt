package com.teamwizardry.mirror.abstractionlayer.method

import com.teamwizardry.mirror.abstractionlayer.type.AbstractType
import java.lang.reflect.Parameter

internal class AbstractParameter(val parameter: Parameter) {
    val name: String? = if(parameter.isNamePresent) parameter.name else null
    val type by lazy { AbstractType.create(parameter.annotatedType) }
    val varArgs = parameter.isVarArgs

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractParameter) return false

        if (parameter != other.parameter) return false

        return true
    }

    override fun hashCode(): Int {
        return parameter.hashCode()
    }
}
