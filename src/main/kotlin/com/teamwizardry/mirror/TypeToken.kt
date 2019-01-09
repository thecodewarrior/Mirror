package com.teamwizardry.mirror

import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class TypeToken<T> {
    fun get(): Type {
        return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    }

    fun getAnnotated(): AnnotatedType {
        return (javaClass.annotatedSuperclass as AnnotatedParameterizedType).annotatedActualTypeArguments[0]
    }
}

inline fun <reified T> typeToken(): Type {
    return (object : TypeToken<T>() {}).get()
}

inline fun <reified T> annotatedTypeToken(): AnnotatedType {
    return (object : TypeToken<T>() {}).getAnnotated()
}
