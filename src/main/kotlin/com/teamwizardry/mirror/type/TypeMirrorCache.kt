package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.type.*

internal class TypeMirrorCache(private val cache: MirrorCache) {
    private val rawCache = mutableMapOf<Any, TypeMirror>()
    private val specializedClasses = mutableMapOf<Pair<AbstractClass, List<TypeMirror>>, ClassMirror>()
    private val specializedWildcards = mutableMapOf<Pair<AbstractWildcardType, Pair<List<TypeMirror>, List<TypeMirror>>>, WildcardMirror>()
    private val specializedArrays = mutableMapOf<Pair<AbstractGenericArrayType, TypeMirror>, ArrayMirror>()

    fun reflect(type: AbstractType<*>): TypeMirror {
        val cached = rawCache[type]
        if(cached != null) return cached

        val mirror: TypeMirror
        when(type) {
            is AbstractClass -> {
                if(type.isArray) {
                    mirror = ArrayMirror(cache, type)
                } else {
                    mirror = ClassMirror(cache, type)
                }
            }
            is AbstractGenericArrayType -> {
                mirror = ArrayMirror(cache, type)
            }
            is AbstractParameterizedType -> {
                mirror = specializeMapping(type, mapOf())
            }
            is AbstractTypeVariable -> {
                mirror = VariableMirror(cache, type)
            }
            is AbstractWildcardType -> {
                mirror = WildcardMirror(cache, type)
            }
            else -> throw IllegalArgumentException("Unknown type $type")
        }

        rawCache[type] = mirror

        return mirror
    }

    internal fun specializeMapping(type: AbstractType<*>, mapping: Map<AbstractTypeVariable, TypeMirror>): TypeMirror {
        mapping[type]?.let {
            return it
        }

        var mirror: TypeMirror? = null
        when (type) {
            is AbstractGenericArrayType -> {
                val component = cache.mapType(mapping, type.genericComponentType)
                mirror = specializeArray(type, component as ConcreteTypeMirror)
            }
            is AbstractParameterizedType -> {
                val parameters = type.actualTypeArguments.map { cache.mapType(mapping, it) }
                mirror = specializeClass(type.rawType, parameters)
            }
            is AbstractWildcardType -> {
                val upperBounds = type.upperBounds.map { cache.mapType(mapping, it) }
                val lowerBounds = type.lowerBounds.map { cache.mapType(mapping, it) }
                mirror = specializeWildcard(type, upperBounds, lowerBounds)
            }
        }

        return mirror ?: reflect(type)
    }

    internal fun specializeClass(type: AbstractClass, arguments: List<TypeMirror>): ClassMirror {
        specializedClasses[type to arguments]?.let { return it }

        val raw = reflect(type) as ClassMirror
        val specialized = ClassMirror(cache, type)
        assert(raw.typeParameters.size == arguments.size)
        specialized.raw = raw
        specialized.typeParameters = arguments

        specializedClasses[type to arguments] = specialized
        return specialized
    }

    private fun specializeWildcard(type: AbstractWildcardType, upperBounds: List<TypeMirror>, lowerBounds: List<TypeMirror>): WildcardMirror {
        specializedWildcards[type to (upperBounds to lowerBounds)]?.let { return it }

        val raw = reflect(type) as WildcardMirror
        val specialized = WildcardMirror(cache, type)
        specialized.raw = raw
        specialized.upperBounds = upperBounds
        specialized.lowerBounds = lowerBounds

        specializedWildcards[type to (upperBounds to lowerBounds)] = specialized
        return specialized
    }

    private fun specializeArray(type: AbstractGenericArrayType, component: ConcreteTypeMirror): ArrayMirror {
        specializedArrays[type to component]?.let { return it }

        val raw = reflect(type) as ArrayMirror
        val arrayType = AbstractClass(java.lang.reflect.Array.newInstance(component.rawType).javaClass)
        val specialized = ArrayMirror(cache, arrayType)
        specialized.raw = raw
        specialized.component = component

        specializedArrays[type to component] = specialized
        return specialized
    }
}