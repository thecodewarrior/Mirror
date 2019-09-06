package dev.thecodewarrior.mirror

import dev.thecodewarrior.mirror.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import java.lang.reflect.AnnotatedType
import kotlin.reflect.KClass

/**
 * Get the ArrayMirror for this class
 */
val Class<*>.arrayMirror: ArrayMirror get() = Mirror.reflect(this) as ArrayMirror
/**
 * Get the ClassMirror for this class. For arrays use [arrayMirror][Class.arrayMirror]
 */
val Class<*>.mirror: ClassMirror get() = Mirror.reflect(this) as ClassMirror

/**
 * Get the ArrayMirror for this class
 */
val KClass<*>.arrayMirror: ArrayMirror get() = Mirror.reflect(this.java) as ArrayMirror
/**
 * Get the ClassMirror for this class. For arrays use [arrayMirror][KClass.arrayMirror]
 */
val KClass<*>.mirror: ClassMirror get() = Mirror.reflect(this.java) as ClassMirror

/**
 * Transforms the passed type to an equivalent one that implements the [equals] and [hashCode] methods.
 */
val <T: AnnotatedType> T.canonical: T get() = CoreTypeUtils.toCanonical(this)
