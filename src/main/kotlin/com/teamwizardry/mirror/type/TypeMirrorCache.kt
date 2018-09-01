package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.type.*

internal class TypeMirrorCache(private val cache: MirrorCache) {
    private val rawCache = mutableMapOf<Any, TypeMirror>()
    private val specializedClasses = mutableMapOf<Pair<AbstractClass, List<TypeMirror>>, ClassMirror>()
    private val specializedArrays = mutableMapOf<TypeMirror, ArrayMirror>()

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
                mirror = getClassMirror(type.rawType, type.actualTypeArguments.map { this.reflect(it) })
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

    internal fun getClassMirror(type: AbstractClass, arguments: List<TypeMirror>): ClassMirror {
        specializedClasses[type to arguments]?.let { return it }

        val raw = reflect(type) as ClassMirror
        val specialized: ClassMirror
        if(raw.typeParameters == arguments) {
            specialized = raw
        } else {
            specialized = ClassMirror(cache, type)
            assert(raw.typeParameters.size == arguments.size)
            specialized.raw = raw
            specialized.typeParameters = arguments
        }

        specializedClasses[type to arguments] = specialized
        return specialized
    }

    internal fun getArrayMirror(component: ConcreteTypeMirror): ArrayMirror {
        specializedArrays[component]?.let { return it }

        val arrayType = AbstractClass(java.lang.reflect.Array.newInstance(component.rawType).javaClass)
        val raw = reflect(arrayType) as ArrayMirror
        val specialized: ArrayMirror
        if(raw.component == component) {
            specialized = raw
        } else {
            specialized = ArrayMirror(cache, arrayType)
            specialized.raw = raw
            specialized.component = component
        }

        specializedArrays[component] = specialized
        return specialized
    }
}