package com.teamwizardry.mirror.reflection

import com.teamwizardry.mirror.reflection.abstractionlayer.type.*
import com.teamwizardry.mirror.reflection.type.*

internal class MirrorCache {
    private val cache = mutableMapOf<Any, TypeMirror>()
    private val specializedClasses = mutableMapOf<Pair<AbstractClass, List<TypeMirror>>, TypeMirror>()
    private val specializedWildcards = mutableMapOf<Pair<AbstractWildcardType, Pair<List<TypeMirror>, List<TypeMirror>>>, TypeMirror>()
    private val specializedArrays = mutableMapOf<Pair<AbstractGenericArrayType, TypeMirror>, TypeMirror>()

    fun reflect(type: AbstractType<*>): TypeMirror {
        val cached = cache[type]
        if(cached != null) return cached

        val mirror: TypeMirror
        when(type) {
            is AbstractClass -> {
                if(type.isArray) {
                    mirror = ArrayMirror(this, type)
                } else {
                    mirror = ClassMirror(this, type)
                }
            }
            is AbstractGenericArrayType -> {
                mirror = ArrayMirror(this, type)
            }
            is AbstractParameterizedType -> {
                mirror = specializeMapping(type, mapOf())
            }
            is AbstractTypeVariable -> {
                mirror = VariableMirror(this, type)
            }
            is AbstractWildcardType -> {
                mirror = WildcardMirror(this, type)
            }
            else -> throw IllegalArgumentException("Unknown type $type")
        }

        cache[type] = mirror

        return mirror
    }

    internal fun specializeMapping(type: AbstractType<*>, mapping: Map<AbstractTypeVariable, TypeMirror>): TypeMirror {
        var typeChanged = false
        fun map(toMap: AbstractType<*>): TypeMirror {
            mapping[type]?.let {
                typeChanged = true
                return it
            }
            val unspecialized = this.reflect(toMap)
            val specialized = specializeMapping(toMap, mapping)
            if(specialized != unspecialized) {
                typeChanged = true
                return specialized
            }
            return unspecialized
        }

        var mirror: TypeMirror? = null
        when (type) {
            is AbstractGenericArrayType -> {
                val component = map(type.genericComponentType)
                mirror = specializeArray(type, component as ConcreteTypeMirror)
            }
            is AbstractParameterizedType -> {
                val parameters = type.actualTypeArguments.map { map(it) }
                mirror = specializeClass(type.rawType, parameters)
            }
            is AbstractWildcardType -> {
                val upperBounds = type.upperBounds.map { map(it) }
                val lowerBounds = type.lowerBounds.map { map(it) }
                mirror = specializeWildcard(type, upperBounds, lowerBounds)
            }
        }

        return mirror ?: reflect(type)
    }

    private fun specializeClass(type: AbstractClass, arguments: List<TypeMirror>): TypeMirror {
        specializedClasses[type to arguments]?.let { return it }

        val raw = reflect(type) as ClassMirror
        val specialized = ClassMirror(this, type)
        assert(raw.typeParameters.size == arguments.size)
        specialized.raw = raw
        specialized.typeParameters = arguments

        specializedClasses[type to arguments] = specialized
        return specialized
    }

    private fun specializeWildcard(type: AbstractWildcardType, upperBounds: List<TypeMirror>, lowerBounds: List<TypeMirror>): TypeMirror {
        specializedWildcards[type to (upperBounds to lowerBounds)]?.let { return it }

        val raw = reflect(type) as WildcardMirror
        val specialized = WildcardMirror(this, type)
        specialized.raw = raw
        specialized.upperBounds = upperBounds
        specialized.lowerBounds = lowerBounds

        specializedWildcards[type to (upperBounds to lowerBounds)] = specialized
        return specialized
    }

    private fun specializeArray(type: AbstractGenericArrayType, component: ConcreteTypeMirror): TypeMirror {
        component as ConcreteTypeMirror
        specializedArrays[type to component]?.let { return it }

        val raw = reflect(type) as ArrayMirror
        val arrayType = AbstractClass(java.lang.reflect.Array.newInstance(component.rawType).javaClass)
        val specialized = ArrayMirror(this, arrayType)
        specialized.raw = raw
        specialized.component = component

        specializedArrays[type to component] = specialized
        return specialized
    }

    companion object {
        @JvmStatic val DEFAULT = MirrorCache()
    }
}

