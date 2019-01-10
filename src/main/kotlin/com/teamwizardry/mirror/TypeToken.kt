package com.teamwizardry.mirror

import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * A container for a generic type
 *
 * Java Usage:
 * ```java
 * Type type = new TypeToken<List<Foo>>() {}.get();
 * AnnotatedType type = new TypeToken<List<@TypeAnnotation Foo>>() {}.getAnnotated();
 * ```
 *
 * Kotlin usage:
 * ```kotlin
 * val type = object : TypeToken<List<Foo>>() {}.get()
 * val type = typeToken<List<Foo>>() // or the helper function
 * ```
 *
 * NOTE!! Due to a bug in javac before JDK 10, in most cases annotated type tokens will not work. https://github.com/raphw/byte-buddy/issues/583
 * NOTE!! As of 1.3, Kotlin does not support runtime type annotations. Thus, type annotations in kotlin code will not work.
 */
abstract class TypeToken<T> {
    fun get(): Type {
        return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    }

    fun getAnnotated(): AnnotatedType {
        return (javaClass.annotatedSuperclass as AnnotatedParameterizedType).annotatedActualTypeArguments[0]
    }
}

inline fun <reified T> typeToken(): Type {
    return object : TypeToken<T>() {}.get()
}
