package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMapping
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.unmodifiable
import java.lang.reflect.Executable

abstract class ExecutableMirror internal constructor(
    internal val cache: MirrorCache,
    raw: ExecutableMirror?,
    val specialization: ExecutableSpecialization?
) {
    abstract val java: Executable

    abstract val raw: ExecutableMirror

    abstract val name: String

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

    val enclosingClass: ClassMirror by lazy {
        specialization?.enclosing ?: cache.types.reflect(java.declaringClass) as ClassMirror
    }

    val genericMapping: TypeMapping by lazy {
        TypeMapping(this.raw.typeParameters.zip(typeParameters).associate { it }) + specialization?.enclosing?.genericMapping
    }

    open fun specialize(vararg parameters: TypeMirror): ExecutableMirror {
        if(parameters.size != typeParameters.size)
            throw InvalidSpecializationException("Passed parameter count ${parameters.size} is different from actual " +
                "parameter count ${typeParameters.size}")
        val newSpecialization = specialization?.copy(arguments = parameters.toList())
            ?: ExecutableSpecialization(null, parameters.toList())
        return cache.executables.specialize(raw, newSpecialization)
    }

    open fun enclose(type: ClassMirror): ExecutableMirror {
        if(type.java != java.declaringClass)
            throw InvalidSpecializationException("Invalid enclosing class $type. " +
                "$this is declared in ${java.declaringClass}")
        val newSpecialization = this.specialization?.copy(enclosing = type) ?: ExecutableSpecialization(type, null)
        return cache.executables.specialize(raw, newSpecialization)
    }
}