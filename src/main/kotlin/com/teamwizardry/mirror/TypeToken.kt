package com.teamwizardry.mirror

import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType

abstract class TypeToken<T> {
    fun get(): AnnotatedType {
        return (javaClass.annotatedSuperclass as AnnotatedParameterizedType).annotatedActualTypeArguments[0]
    }
}

inline fun <reified T> typeToken(): AnnotatedType {
    return (object : TypeToken<T>() {}).get()
}