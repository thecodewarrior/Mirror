package dev.thecodewarrior.mirror

import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KTypeProjection

/**
 * A container for a generic type.
 *
 * Java Usage:
 * ```java
 * Type type = new TypeToken<List<Foo>>() {}.get();
 * // type == List<Foo>
 * AnnotatedType type = new TypeToken<List<@TypeAnnotation Foo>>() {}.getAnnotated();
 * // type == List<@TypeAnnotation Foo>
 * ```
 *
 * Kotlin usage:
 * ```kotlin
 * val type = object : TypeToken<List<Foo>>() {}.get()
 * val type = typeToken<List<Foo>>() // or the helper function
 * ```
 *
 * **NOTE!!** Due to a bug in javac before JDK 10, in most cases annotated type tokens will not work. https://github.com/raphw/byte-buddy/issues/583
 *
 * **NOTE!!** As of Kotlin 1.4, Java type annotations are finally supported, though in a limited fashion and gated
 * behind a compiler flag. [(more info here)](https://kotlinlang.org/docs/reference/whatsnew14.html#type-annotations-in-the-jvm-bytecode)
 */
public abstract class TypeToken<T> {
    /**
     * Gets the generic type represented by this TypeToken
     */
    public fun get(): Type {
        return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    }

    /**
     * Gets the annotated type represented by this TypeToken
     */
    public fun getAnnotated(): AnnotatedType {
        return (javaClass.annotatedSuperclass as AnnotatedParameterizedType).annotatedActualTypeArguments[0]
    }

    /**
     * Gets the kotlin type represented by this TypeToken
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath
     */
    public fun getKotlin(): KTypeProjection {
        return javaClass.kotlin.supertypes[0].arguments[0]
    }
}

/**
 * Create a type token with the given type
 *
 * **NOTE!!** As of 1.3, Kotlin does not support runtime type annotations. Thus, type annotations in kotlin code will not work.
 */
public inline fun <reified T> typeToken(): Type {
    return object : TypeToken<T>() {}.get()
}
