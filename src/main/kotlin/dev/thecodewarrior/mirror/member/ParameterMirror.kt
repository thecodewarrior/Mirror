package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.type.TypeMapping
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.utils.Untested
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Parameter

class ParameterMirror internal constructor(
    internal val cache: MirrorCache,
    raw: ParameterMirror?,
    val declaringExecutable: ExecutableMirror?,
    internal val java: Parameter
): AnnotatedElement by java {
    val name: String? = if(java.isNamePresent) java.name else null

    val raw: ParameterMirror = raw ?: this

    /**
     * True if the `final` modifier is present on this parameter
     */
    val isFinal: Boolean = Modifier.FINAL in Modifier.fromModifiers(java.modifiers)

    /**
     * True if this is a vararg parameter
     */
    @Untested
    val isVarArgs: Boolean = java.isVarArgs

    /**
     * The type of this parameter
     */
    val type: TypeMirror by lazy {
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
    inline fun <reified T: Annotation> isAnnotationPresent(): Boolean = this.isAnnotationPresent(T::class.java)

    /**
     * Returns the annotation of the specified type, or null if no such annotation is parameter.
     *
     * @see AnnotatedElement.getAnnotation
     */
    inline fun <reified T: Annotation> getAnnotation(): T? = this.getAnnotation(T::class.java)

    /**
     * Returns the annotation of the specified type, or null if no such annotation is _directly_ present on this
     * parameter.
     *
     * @see AnnotatedElement.getDeclaredAnnotation
     */
    inline fun <reified T: Annotation> getDeclaredAnnotation(): T? = this.getDeclaredAnnotation(T::class.java)

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
    fun withDeclaringExecutable(enclosing: ExecutableMirror?): ParameterMirror {
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