package com.teamwizardry.mirror

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class TypeToken<T> {
    fun get(): Type {
        return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    }
}

inline fun <reified T> typeToken(): Type {
    return (object : TypeToken<T>() {}).get()
}