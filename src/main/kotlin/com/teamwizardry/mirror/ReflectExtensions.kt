package com.teamwizardry.mirror

import java.lang.reflect.TypeVariable

internal fun Class<*>.typeParameter(name: String): TypeVariable<*>? {
    return this.typeParameters.find { it.typeName == name }
}

internal fun Class<*>.typeParameter(index: Int): TypeVariable<*>? {
    return this.typeParameters.getOrNull(index)
}
