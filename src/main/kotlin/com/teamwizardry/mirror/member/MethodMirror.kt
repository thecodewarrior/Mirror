package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.type.ArrayMirror
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMapping
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.unmodifiable
import java.lang.reflect.Method

class MethodMirror internal constructor(
    internal val cache: MirrorCache,
    val java: Method,
    raw: MethodMirror?,
    val specialization: MethodSpecialization?
) {

    val raw: MethodMirror = raw ?: this

    val name: String = java.name

    val returnType: TypeMirror by lazy {
        java.annotatedReturnType.let {
            genericMapping[cache.types.reflect(it)]
        }
    }

    val parameters: List<ParameterMirror> by lazy {
        java.parameters.map {
            cache.parameters.reflect(it).specialize(this)
        }.unmodifiable()
    }

    val parameterTypes: List<TypeMirror> by lazy {
        parameters.map { it.type }.unmodifiable()
    }

    val exceptionTypes: List<TypeMirror> by lazy {
        java.annotatedExceptionTypes.map {
             genericMapping[cache.types.reflect(it)]
        }.unmodifiable()
    }

    val typeParameters: List<TypeMirror> by lazy {
        specialization?.arguments ?: java.typeParameters.map {
            cache.types.reflect(it)
        }.unmodifiable()
    }

    val genericMapping: TypeMapping by lazy {
        TypeMapping(this.raw.typeParameters.zip(typeParameters).associate { it }) + specialization?.enclosing?.genericMapping
    }

    fun specialize(vararg parameters: TypeMirror): MethodMirror {
        if(parameters.size != typeParameters.size)
            throw InvalidSpecializationException("Passed parameter count ${parameters.size} is different from class type " +
                "parameter count ${typeParameters.size}")
        val newSpecialization = specialization?.copy(arguments = parameters.toList())
            ?: MethodSpecialization(null, parameters.toList())
        return cache.methods.specialize(raw, newSpecialization)
    }

    fun enclose(type: ClassMirror): MethodMirror {
        if(type.java != java.declaringClass)
            throw InvalidSpecializationException("Invalid enclosing class $type. " +
                "$this is declared in ${java.declaringClass}")
        val newSpecialization = this.specialization?.copy(enclosing = type) ?: MethodSpecialization(type, null)
        return cache.methods.specialize(raw, newSpecialization)
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