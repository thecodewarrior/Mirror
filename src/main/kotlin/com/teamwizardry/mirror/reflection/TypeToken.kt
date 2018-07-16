package com.teamwizardry.mirror.reflection

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class TypeToken<T> {
    fun get(): Type {
        return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    }
}

inline fun <reified T> javaType(): Type {
    return (object : TypeToken<T>() {}).get()
}