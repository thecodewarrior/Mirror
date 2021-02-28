package dev.thecodewarrior.mirror.impl.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.impl.TypeMapping
import dev.thecodewarrior.mirror.impl.member.ExecutableSpecialization
import dev.thecodewarrior.mirror.impl.type.ClassMirrorImpl
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.unmodifiableView
import dev.thecodewarrior.mirror.member.ExecutableMirror
import dev.thecodewarrior.mirror.member.ParameterMirror
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.lang.reflect.Constructor
import kotlin.reflect.KCallable

internal abstract class ExecutableMirrorImpl internal constructor(
    cache: MirrorCache,
    annotatedElement: AnnotatedElement,
    internal val specialization: ExecutableSpecialization?
): MemberMirrorImpl(cache, annotatedElement, specialization?.enclosing), ExecutableMirror {

    override val returnType: TypeMirror by lazy {
        java.annotatedReturnType.let {
            genericMapping[cache.types.reflect(it)]
        }
    }

    // * **Note: this value is immutable**
    override val parameters: List<ParameterMirror> by lazy {
        java.parameters.map {
            cache.parameters.reflect(it).withDeclaringExecutable(this)
        }.unmodifiableView()
    }

    // * **Note: this value is immutable**
    override val parameterTypes: List<TypeMirror> by lazy {
        parameters.map { it.type }.unmodifiableView()
    }

    /**
     * Used to determine method override relationships
     */
    override val erasedParameterTypes: List<Class<*>> by lazy {
        parameterTypes.map { it.erasure }
    }

    // * **Note: this value is immutable**
    override val exceptionTypes: List<TypeMirror> by lazy {
        java.annotatedExceptionTypes.map {
            genericMapping[cache.types.reflect(it)]
        }.unmodifiableView()
    }

    // * **Note: this value is immutable**
    override val typeParameters: List<TypeMirror> by lazy {
        specialization?.arguments ?: java.typeParameters.map {
            cache.types.reflect(it)
        }.unmodifiableView()
    }

    val genericMapping: TypeMapping by lazy {
        TypeMapping(this.raw.typeParameters.zip(typeParameters).associate { it }) +
                (specialization?.enclosing as ClassMirrorImpl?)?.genericMapping
    }

    /**
     * Returns annotations that are present on the executable this mirror represents.
     *
     * **Note: this value is immutable**
     *
     * @see Executable.getAnnotations
     */
    override val annotations: List<Annotation> by lazy {
        java.annotations.toList().unmodifiableView()
    }

    /**
     * Returns a copy of this mirror, replacing its type parameters the given types. Passing zero arguments will return
     * a copy of this mirror with the raw type arguments.
     *
     * **Note: A new mirror is only created if none already exist with the required specialization**
     *
     * @throws InvalidSpecializationException if the passed type list is not the same length as [typeParameters] or zero
     * @return A copy of this mirror with its type parameters replaced
     */
    override fun withTypeParameters(vararg parameters: TypeMirror): ExecutableMirror {
        if(parameters.isNotEmpty() && parameters.size != typeParameters.size)
            throw InvalidSpecializationException("Passed parameter count ${parameters.size} is different from actual " +
                    "parameter count ${typeParameters.size}")

        if(parameters.isNotEmpty() && parameters.indices.all { parameters[it] == this.typeParameters[it] }) {
            return this // the same type parameters were specified, return this
        }
        if(this.declaringClass == raw.declaringClass && parameters.indices.all { parameters[it] == raw.typeParameters[it] }) {
            // if the declaring class is raw and the raw type parameters were supplied, return the raw type.
            // Calling the `all` method on an empty list will always return true, so an empty parameter list will
            // conveniently implicitly be like having the raw parameters match.
            return raw
        }
        val newSpecialization = specialization?.copy(arguments = parameters.toList())
            ?: ExecutableSpecialization(null, parameters.toList())
        return cache.executables.specialize(raw, newSpecialization)
    }

    override fun withDeclaringClass(enclosing: ClassMirror?): ExecutableMirror {
        if(enclosing != null && enclosing.java != java.declaringClass)
            throw InvalidSpecializationException("Invalid declaring class $enclosing. " +
                    "$this is declared in ${java.declaringClass}")
        if(enclosing == declaringClass)
            return this
        if(enclosing == raw.declaringClass && typeParameters == raw.typeParameters)
            return raw
        val newSpecialization = this.specialization?.copy(enclosing = enclosing) ?: ExecutableSpecialization(enclosing, null)
        return cache.executables.specialize(raw, newSpecialization)
    }
}
