package com.teamwizardry.librarianlib.commons.reflection

import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable

fun Class<*>.typeParameter(name: String): TypeVariable<*>? {
    return this.typeParameters.find { it.typeName == name }
}

fun Class<*>.typeParameter(index: Int): TypeVariable<*>? {
    return this.typeParameters.getOrNull(index)
}

