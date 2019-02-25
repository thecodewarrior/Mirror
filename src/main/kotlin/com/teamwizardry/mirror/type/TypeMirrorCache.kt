package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
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
                        type == Void.TYPE -> mirror = VoidMirror(cache, type, null, null)
                        type.isArray -> mirror = ArrayMirror(cache, type, null, null)
                        else -> mirror = ClassMirror(cache, type, null, null)
                    }
                }
                is GenericArrayType -> {
                    mirror = ArrayMirror(cache, type, null, null)
                }
                is ParameterizedType -> {
                    var theMirror = reflect(type.rawType) as ClassMirror
                    theMirror = theMirror.specialize(*type.actualTypeArguments.map { reflect(it) }.toTypedArray())
                    type.ownerType?.let {
                        theMirror = theMirror.enclose(reflect(it) as ClassMirror)
                    }
                    mirror = theMirror
                }
                is TypeVariable<*> -> {
                    mirror = VariableMirror(cache, type, null, null)
                }
                is WildcardType -> {
                    mirror = WildcardMirror(cache, type, null, null, null)
                }
                else -> throw IllegalArgumentException("Unknown type $type")
            }

            return@getOrPut mirror
        }
    }

    fun reflect(type: AnnotatedType): TypeMirror {
        return rawCache.getOrPut(type) {
            val mirror: TypeMirror
            val java = type.type
            when (type) {
                is AnnotatedArrayType -> {
                    mirror = (reflect(java) as ArrayMirror)
                        .specialize(reflect(type.annotatedGenericComponentType))
                }
                is AnnotatedParameterizedType -> {
                    java as ParameterizedType
                    mirror = (reflect(java.rawType) as ClassMirror)
                        .specialize(*type.annotatedActualTypeArguments.map { reflect(it) }.toTypedArray())
                }
                is AnnotatedWildcardType -> {
                    mirror = WildcardMirror(cache, java as WildcardType, type, null, null)
                }
                else -> mirror = reflect(java)
            }

            return@getOrPut mirror.annotate(type.annotations.toList())
        }
    }

    internal fun specialize(type: TypeMirror, specialization: TypeSpecialization): TypeMirror {
        return specializedCache.getOrPut(type.raw to specialization) {
            type.raw.applySpecialization(specialization)
        }
    }
}