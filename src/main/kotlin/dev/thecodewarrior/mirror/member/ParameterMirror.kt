package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.impl.TypeMapping
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.Untested
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Parameter

public class ParameterMirror internal constructor(
    internal val cache: MirrorCache,
    raw: ParameterMirror?,
    public val declaringExecutable: ExecutableMirror?,
    internal val java: Parameter
): AnnotatedElement by java {
    public val hasName: Boolean = java.isNamePresent
    public val name: String = java.name

    public val raw: ParameterMirror = raw ?: this


    /**
     * The index in the parameter list
     */
    public val index: Int = java.declaringExecutable.parameters.indexOf(java)

    /**
     * True if the `final` modifier is present on this parameter
     */
    public val isFinal: Boolean = Modifier.FINAL in Modifier.fromModifiers(java.modifiers)

    /**
     * True if this is a vararg parameter
     */
    @Untested
    public val isVarArgs: Boolean = java.isVarArgs

    /**
     * The type of this parameter
     */
    public val type: TypeMirror by lazy {
        java.annotatedType.let {
            genericMapping[cache.types.reflect(it)]
        }
    }

    private val genericMapping: TypeMapping by lazy {
        TypeMapping(emptyMap()) + declaringExecutable?.genericMapping
    }

    /**
     * Returns true if the specified annotation is present on this parameter.
     *
     * @see AnnotatedElement.isAnnotationPresent
     */
    public inline fun <reified T: Annotation> isAnnotationPresent(): Boolean = this.isAnnotationPresent(T::class.java)

    /**
     * Returns the annotation of the specified type, or null if no such annotation is parameter.
     *
     * @see AnnotatedElement.getAnnotation
     */
    public inline fun <reified T: Annotation> getAnnotation(): T? = this.getAnnotation(T::class.java)

    /**
     * Returns the annotation of the specified type, or null if no such annotation is _directly_ present on this
     * parameter.
     *
     * @see AnnotatedElement.getDeclaredAnnotation
     */
    public inline fun <reified T: Annotation> getDeclaredAnnotation(): T? = this.getDeclaredAnnotation(T::class.java)

    /**
     * Returns a copy of this parameter with its enclosing method/constructor replaced with [enclosing].
     * If the passed executable is null this method removes any enclosing executable specialization.
     *
     * **Note: A new mirror is only created if none already exist with the required specialization**
     *
     * @throws InvalidSpecializationException if the passed executable is not equal to or a specialization of this
     * class's raw enclosing method
     * @return A copy of this parameter with the passed enclosing executable, or with the raw enclosing executable if
     * [enclosing] is null
     */
    public fun withDeclaringExecutable(enclosing: ExecutableMirror?): ParameterMirror {
        if(enclosing != null && enclosing.java != java.declaringExecutable)
            throw InvalidSpecializationException("Invalid declaring " +
                (if(enclosing is ConstructorMirror) "constructor" else "method") +
                " $enclosing. $this is declared in ${java.declaringExecutable}")
        return if(enclosing == null || enclosing == raw.declaringExecutable) raw else cache.parameters.specialize(this, enclosing)
    }

    override fun toString(): String {
        var str = ""
        if(isFinal)
            str += "final "
        val type = type
        if(isVarArgs && type is ArrayMirror) {
            str += "${type.component}... $name"
        } else {
            str += "$type $name"
        }
        return str
    }
}