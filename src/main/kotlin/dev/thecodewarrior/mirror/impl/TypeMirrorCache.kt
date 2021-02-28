package dev.thecodewarrior.mirror.impl

import dev.thecodewarrior.mirror.impl.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.impl.type.*
import dev.thecodewarrior.mirror.impl.type.ArrayMirrorImpl
import dev.thecodewarrior.mirror.impl.type.ClassMirrorImpl
import dev.thecodewarrior.mirror.impl.type.TypeMirrorImpl
import dev.thecodewarrior.mirror.impl.type.TypeSpecialization
import dev.thecodewarrior.mirror.impl.type.VoidMirrorImpl
import dev.thecodewarrior.mirror.type.*
import java.lang.reflect.AnnotatedArrayType
import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.AnnotatedWildcardType
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.util.concurrent.ConcurrentHashMap

internal class TypeMirrorCache(private val cache: MirrorCache) {
    private val rawCache = ConcurrentHashMap<Any, TypeMirror>()
    private val specializedCache = ConcurrentHashMap<Pair<TypeMirror, TypeSpecialization>, TypeMirror>()

    fun reflect(type: Type): TypeMirror {
        return rawCache.getOrPut(type) {
            val mirror: TypeMirror
            when (type) {
                is Class<*> -> {
                    when {
                        type == Void.TYPE -> mirror = VoidMirrorImpl(cache, type, null, null)
                        type.isArray -> mirror = ArrayMirrorImpl(cache, type, null, null)
                        else -> mirror = ClassMirrorImpl(cache, type, null, null)
                    }
                }
                is GenericArrayType -> {
                    val component = reflect(type.genericComponentType)
                    val rawArray = java.lang.reflect.Array.newInstance(component.erasure, 0).javaClass
                    mirror = (reflect(rawArray) as ArrayMirror).withComponent(component)
                }
                is ParameterizedType -> {
                    var theMirror = reflect(type.rawType) as ClassMirror
                    theMirror = theMirror.withTypeArguments(*type.actualTypeArguments.map { reflect(it) }.toTypedArray())
                    type.ownerType?.let {
                        theMirror = theMirror.withEnclosingClass(reflect(it) as ClassMirror)
                    }
                    mirror = theMirror
                }
                is TypeVariable<*> -> {
                    mirror = TypeVariableMirrorImpl(cache, type, null, null)
                }
                is WildcardType -> {
                    mirror = WildcardMirrorImpl(cache, type, null, null, null)
                }
                else -> throw IllegalArgumentException("Unknown type $type")
            }

            return@getOrPut mirror
        }
    }

    fun reflect(type: AnnotatedType): TypeMirror {
        return rawCache.getOrPut(CoreTypeUtils.toCanonical(type)) {
            val mirror: TypeMirror
            when (type) {
                is AnnotatedArrayType -> {
                    mirror = (reflect(type.type) as ArrayMirror)
                        .withComponent(reflect(type.annotatedGenericComponentType))
                }
                is AnnotatedParameterizedType -> {
                    mirror = (reflect(type.type) as ClassMirror)
                        .withTypeArguments(*type.annotatedActualTypeArguments.map { reflect(it) }.toTypedArray())
                }
                is AnnotatedWildcardType -> {
                    mirror = WildcardMirrorImpl(cache, type.type as WildcardType, type, null, null)
                }
                else -> mirror = reflect(type.type)
            }

            return@getOrPut mirror.withTypeAnnotations(type.annotations.toList())
        }
    }

    internal fun specialize(type: TypeMirror, specialization: TypeSpecialization): TypeMirror {
        return specializedCache.getOrPut(type.raw to specialization) {
            (type.raw as TypeMirrorImpl).applySpecialization(specialization)
        }
    }
}