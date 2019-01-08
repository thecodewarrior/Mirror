package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.type.AbstractArrayType
import com.teamwizardry.mirror.abstractionlayer.type.AbstractClass
import com.teamwizardry.mirror.abstractionlayer.type.AbstractParameterizedType
import com.teamwizardry.mirror.abstractionlayer.type.AbstractType
import com.teamwizardry.mirror.abstractionlayer.type.AbstractTypeVariable
import com.teamwizardry.mirror.abstractionlayer.type.AbstractVoid
import com.teamwizardry.mirror.abstractionlayer.type.AbstractWildcardType
import com.teamwizardry.mirror.utils.unmodifiableCopy
import java.util.concurrent.ConcurrentHashMap

internal class TypeMirrorCache(private val cache: MirrorCache) {
    private val rawCache = ConcurrentHashMap<Any, TypeMirror>()
    private val specializedClasses = ConcurrentHashMap<Pair<AbstractClass, List<TypeMirror>>, ClassMirror>()
    private val specializedArrays = ConcurrentHashMap<TypeMirror, ArrayMirror>()

    fun reflect(type: AbstractType<*, *>): TypeMirror {
        return rawCache.getOrPut(type) {

            val mirror: TypeMirror
            when (type) {
                is AbstractVoid ->
                    mirror = VoidMirror(cache, type)
                is AbstractClass -> {
                    mirror = ClassMirror(cache, type)
                }
                is AbstractArrayType -> {
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

            return@getOrPut mirror
        }
    }

    internal fun getClassMirror(type: AbstractClass, arguments: List<TypeMirror>): ClassMirror {
        return specializedClasses.getOrPut(type to arguments) {
            val raw = reflect(type) as ClassMirror
            val specialized: ClassMirror
            if (raw.typeParameters == arguments) {
                specialized = raw
            } else {
                specialized = ClassMirror(cache, type)
                assert(raw.typeParameters.size == arguments.size)
                specialized.raw = raw
                specialized.typeParameters = arguments.unmodifiableCopy()
            }

            return@getOrPut specialized
        }
    }

    internal fun getArrayMirror(component: ConcreteTypeMirror): ArrayMirror {
        return specializedArrays.getOrPut(component) {
            val arrayType = AbstractArrayType(java.lang.reflect.Array.newInstance(component.java, 0).javaClass, null)
            val raw = reflect(arrayType) as ArrayMirror
            val specialized: ArrayMirror
            if (raw.component == component) {
                specialized = raw
            } else {
                specialized = ArrayMirror(cache, arrayType)
                specialized.raw = raw
                specialized.component = component
            }

            return@getOrPut specialized
        }
    }
}