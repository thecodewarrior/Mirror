package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.type.classmirror.ClassMirrorImpl
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
                    mirror = WildcardMirror(cache, type.type as WildcardType, type, null, null)
                }
                else -> mirror = reflect(type.type)
            }

            return@getOrPut mirror.withTypeAnnotations(type.annotations.toList())
        }
    }

    internal fun specialize(type: TypeMirror, specialization: TypeSpecialization): TypeMirror {
        return specializedCache.getOrPut(type.raw to specialization) {
            type.raw.applySpecialization(specialization)
        }
    }
}