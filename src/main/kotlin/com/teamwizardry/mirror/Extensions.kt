package com.teamwizardry.mirror

import com.teamwizardry.mirror.coretypes.CoreTypeUtils
import com.teamwizardry.mirror.type.ArrayMirror
import com.teamwizardry.mirror.type.ClassMirror
import java.lang.reflect.AnnotatedType
import kotlin.reflect.KClass

val Class<*>.arrayMirror: ArrayMirror get() = Mirror.reflect(this) as ArrayMirror
val Class<*>.mirror: ClassMirror get() = Mirror.reflect(this) as ClassMirror
val KClass<*>.arrayMirror: ArrayMirror get() = Mirror.reflect(this.java) as ArrayMirror
val KClass<*>.mirror: ClassMirror get() = Mirror.reflect(this.java) as ClassMirror

val Any.mirrorClass: ClassMirror get() = Mirror.reflect(this::class.java) as ClassMirror
val Array<*>.mirrorArray: ArrayMirror get() = Mirror.reflect(this::class.java) as ArrayMirror
val BooleanArray.mirrorArray: ArrayMirror get() = Mirror.reflect(this::class.java) as ArrayMirror
val ByteArray.mirrorArray: ArrayMirror get() = Mirror.reflect(this::class.java) as ArrayMirror
val CharArray.mirrorArray: ArrayMirror get() = Mirror.reflect(this::class.java) as ArrayMirror
val ShortArray.mirrorArray: ArrayMirror get() = Mirror.reflect(this::class.java) as ArrayMirror
val IntArray.mirrorArray: ArrayMirror get() = Mirror.reflect(this::class.java) as ArrayMirror
val LongArray.mirrorArray: ArrayMirror get() = Mirror.reflect(this::class.java) as ArrayMirror
val FloatArray.mirrorArray: ArrayMirror get() = Mirror.reflect(this::class.java) as ArrayMirror
val DoubleArray.mirrorArray: ArrayMirror get() = Mirror.reflect(this::class.java) as ArrayMirror

val <T: AnnotatedType> T.canonical get() = CoreTypeUtils.toCanonical(this)
