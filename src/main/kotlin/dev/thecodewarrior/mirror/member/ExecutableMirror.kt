package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.impl.TypeMapping
import dev.thecodewarrior.mirror.impl.type.ClassMirrorImpl
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.unmodifiableView
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.lang.reflect.Constructor
import kotlin.reflect.KCallable

public abstract class ExecutableMirror internal constructor(
    cache: MirrorCache,
    internal val specialization: ExecutableSpecialization?
): MemberMirror(cache, specialization?.enclosing) {
    abstract override val java: Executable

    public abstract val raw: ExecutableMirror

    public abstract val modifiers: Set<Modifier>
    public abstract val access: Modifier.Access

    /**
     * Returns true if this method takes a variable number of arguments.
     *
     * @see Method.isVarArgs
     */
    public abstract val isVarArgs: Boolean

    /**
     * Returns true if this method/constructor is synthetic.
     *
     * @see Method.isSynthetic
     * @see Constructor.isSynthetic
     */
    public abstract val isSynthetic: Boolean

    /**
     * Returns true if this method/constructor has internal visibility in Kotlin
     */
    public abstract val isInternalAccess: Boolean

    /**
     * Returns the Kotlin [KCallable] instance that represents the same method/constructor as this. This is null if this
     * is [synthetic][isSynthetic] or if this is a [bridge method][MethodMirror.isBridge]. Other cases where no
     * KCallable exists are not yet known, but may exist.
     */
    public abstract val kCallable: KCallable<*>?

    /**
     * The method name. The class's [binary name][Class.getName] for constructors.
     */
    public abstract val name: String

    public val returnType: TypeMirror by lazy {
        java.annotatedReturnType.let {
            genericMapping[cache.types.reflect(it)]
        }
    }

    // * **Note: this value is immutable**
    public val parameters: List<ParameterMirror> by lazy {
        java.parameters.map {
            cache.parameters.reflect(it).withDeclaringExecutable(this)
        }.unmodifiableView()
    }

    // * **Note: this value is immutable**
    public val parameterTypes: List<TypeMirror> by lazy {
        parameters.map { it.type }.unmodifiableView()
    }

    /**
     * Used to determine method override relationships
     */
    internal val erasedParameterTypes: List<Class<*>> by lazy {
        parameterTypes.map { it.erasure }
    }

    // * **Note: this value is immutable**
    public val exceptionTypes: List<TypeMirror> by lazy {
        java.annotatedExceptionTypes.map {
             genericMapping[cache.types.reflect(it)]
        }.unmodifiableView()
    }

    // * **Note: this value is immutable**
    public val typeParameters: List<TypeMirror> by lazy {
        specialization?.arguments ?: java.typeParameters.map {
            cache.types.reflect(it)
        }.unmodifiableView()
    }

    internal val genericMapping: TypeMapping by lazy {
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
    public val annotations: List<Annotation> by lazy {
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
    public open fun withTypeParameters(vararg parameters: TypeMirror): ExecutableMirror {
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