package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMapping
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.unmodifiableView
import java.lang.reflect.Executable

abstract class ExecutableMirror internal constructor(
    internal val cache: MirrorCache,
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

    // * **Note: this value is immutable**
    val parameters: List<ParameterMirror> by lazy {
        java.parameters.map {
            cache.parameters.reflect(it).withDeclaringExecutable(this)
        }.unmodifiableView()
    }

    // * **Note: this value is immutable**
    val parameterTypes: List<TypeMirror> by lazy {
        parameters.map { it.type }.unmodifiableView()
    }

    // * **Note: this value is immutable**
    val exceptionTypes: List<TypeMirror> by lazy {
        java.annotatedExceptionTypes.map {
             genericMapping[cache.types.reflect(it)]
        }.unmodifiableView()
    }

    // * **Note: this value is immutable**
    val typeParameters: List<TypeMirror> by lazy {
        specialization?.arguments ?: java.typeParameters.map {
            cache.types.reflect(it)
        }.unmodifiableView()
    }

    val declaringClass: ClassMirror by lazy {
        specialization?.enclosing ?: cache.types.reflect(java.declaringClass) as ClassMirror
    }

    val genericMapping: TypeMapping by lazy {
        TypeMapping(this.raw.typeParameters.zip(typeParameters).associate { it }) + specialization?.enclosing?.genericMapping
    }

    open fun withTypeParameters(vararg parameters: TypeMirror): ExecutableMirror {
        if(parameters.isNotEmpty() && parameters.size != typeParameters.size)
            throw InvalidSpecializationException("Passed parameter count ${parameters.size} is different from actual " +
                "parameter count ${typeParameters.size}")
//        if(parameters.toList() == raw.typeParameters) return raw
        val newSpecialization = specialization?.copy(arguments = parameters.toList())
            ?: ExecutableSpecialization(null, parameters.toList())
        return cache.executables.specialize(raw, newSpecialization)
    }

    open fun withDeclaringClass(type: ClassMirror?): ExecutableMirror {
        if(type != null && type.java != java.declaringClass)
            throw InvalidSpecializationException("Invalid declaring class $type. " +
                "$this is declared in ${java.declaringClass}")
//        if(type == raw.declaringClass) return raw
        val newSpecialization = this.specialization?.copy(enclosing = type) ?: ExecutableSpecialization(type, null)
        return cache.executables.specialize(raw, newSpecialization)
    }

    internal val descriptor: Any = Descriptor(this)

    private class Descriptor(val mirror: ExecutableMirror) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Descriptor) return false

            val m1 = mirror
            val m2 = other.mirror
            return m1.returnType == m2.returnType &&
                m1.name == m2.name &&
                m1.parameterTypes == m2.parameterTypes
        }

        override fun hashCode(): Int {
            var result = mirror.returnType.hashCode()
            result = 31 * result + mirror.name.hashCode()
            result = 31 * result + mirror.parameterTypes.hashCode()
            return result
        }
    }
}