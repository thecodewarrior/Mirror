package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.method.AbstractMethod
import com.teamwizardry.mirror.type.ArrayMirror
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.lazyOrSet
import com.teamwizardry.mirror.utils.unmodifiable
import java.lang.reflect.Method

class MethodMirror internal constructor(internal val cache: MirrorCache, internal val abstractMethod: AbstractMethod) {
    val java: Method = abstractMethod.method

    var raw: MethodMirror = this
        internal set

    val name: String = abstractMethod.name

    var returnType: TypeMirror by lazyOrSet {
        abstractMethod.returnType.annotated?.let { cache.types.reflect(it) }
            ?: cache.types.reflect(abstractMethod.returnType.type)
    }
        internal set

    var parameters: List<ParameterMirror> by lazyOrSet {
        abstractMethod.parameters.map {
            cache.parameters.reflect(it)
        }.unmodifiable()
    }
        internal set

    val parameterTypes: List<TypeMirror> by lazy {
        parameters.map { it.type }.unmodifiable()
    }

    var exceptionTypes: List<TypeMirror> by lazyOrSet {
        abstractMethod.exceptionTypes.map {
            it.annotated?.let { cache.types.reflect(it) }
                ?: cache.types.reflect(it.type)
        }.unmodifiable()
    }
        internal set

    var typeParameters: List<TypeMirror> by lazyOrSet {
        abstractMethod.typeParameters.map {
            it.annotated?.let { cache.types.reflect(it) }
                ?: cache.types.reflect(it.type)
        }.unmodifiable()
    }
        internal set

    fun specialize(vararg parameters: TypeMirror): MethodMirror {
        if(parameters.size != typeParameters.size)
            throw IllegalArgumentException("Passed parameter count ${parameters.size} is different from class type " +
                "parameter count ${typeParameters.size}")
        val mapping = raw.typeParameters.zip(parameters).associate { it }

        val newReturnType = this.map(returnType, mapping)
        val newParamTypes = this.parameterTypes.map { this.map(it, mapping) }
        val newExceptionTypes= this.exceptionTypes.map { this.map(it, mapping) }

        return cache.methods.getMethodMirror(abstractMethod,
            newReturnType, newParamTypes, newExceptionTypes, parameters.asList())
    }

    private fun map(type: TypeMirror, mapping: Map<TypeMirror, TypeMirror>): TypeMirror {
        mapping[type]?.let {
            return it
        }

        when (type) {
            is ArrayMirror -> {
                val component = this.map(type.component, mapping)
                if(component != type.component) {
                    return type.specialize(component)
                }
            }
            is ClassMirror -> {
                val parameters = type.typeParameters.map { this.map(it, mapping) }
                if(parameters != type.typeParameters) {
                    return type.specialize(*parameters.toTypedArray())
                }
            }
        }

        return type
    }

    override fun toString(): String {
        var str = ""
        str += "$returnType $name()"
        return str
    }
}