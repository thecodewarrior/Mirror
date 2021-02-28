package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.impl.TypeMapping
import dev.thecodewarrior.mirror.impl.member.ExecutableMirrorImpl
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.Untested
import dev.thecodewarrior.mirror.util.AnnotationList
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Parameter

/**
 * A mirror representing a method or constructor parameter
 */
public interface ParameterMirror {
    /**
     * The Core Reflection object this mirror represents
     */
    public val java: Parameter

    /**
     * The mirror representing this parameter without any generic specialization
     */
    public val raw: ParameterMirror

    /**
     * Whether the parameter has a name compiled in the class file
     */
    public val hasName: Boolean

    /**
     * The name of this parameter. If no name is [present][hasName], parameters are named `argN`, where `N` is the
     * index of the argument.
     */
    public val name: String

    /**
     * The index in the parameter list
     */
    public val index: Int

    /**
     * True if the `final` modifier is present on this parameter
     */
    public val isFinal: Boolean

    /**
     * True if this is a vararg parameter
     */
    @Untested
    public val isVarArgs: Boolean

    /**
     * The type of this parameter, specialized based on the declaring executable's specialization.
     */
    public val type: TypeMirror

    /**
     * Returns annotations that are present on the member this mirror represents.
     *
     * @see AnnotatedElement
     * @see AnnotatedElement.getAnnotations
     */
    public val annotations: AnnotationList

    /**
     * Returns annotations that are declared on the member this mirror represents.
     *
     * @see AnnotatedElement
     * @see AnnotatedElement.getDeclaredAnnotations
     */
    public val declaredAnnotations: AnnotationList

    /**
     * The potentially specialized executable this parameter is declared in
     */
    public val declaringExecutable: ExecutableMirror?

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
    public fun withDeclaringExecutable(enclosing: ExecutableMirror?): ParameterMirror
}