package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.util.AnnotationList
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Member

/**
 * The abstract superclass representing any Java class member
 */
public interface MemberMirror {
    /**
     * The Core Reflection object this mirror represents
     */
    public val java: Member

    /**
     * The mirror representing this member without any generic specialization
     */
    public val raw: MemberMirror

    /**
     * The set of modifiers present on this member.
     *
     * **Note: This set is immutable**
     */
    public val modifiers: Set<Modifier>

    /**
     * The access control modifier for this member.
     */
    public val access: Modifier.Access

    /**
     * A shorthand for checking if the `public` [modifier][modifiers] is present on this field.
     */
    public val isPublic: Boolean

    /**
     * A shorthand for checking if the `protected` [modifier][modifiers] is present on this field.
     */
    public val isProtected: Boolean

    /**
     * A shorthand for checking if the `private` [modifier][modifiers] is present on this field.
     */
    public val isPrivate: Boolean

    /**
     * A shorthand for checking if neither the `public`, `protected`, nor `private` [modifiers][modifiers] are present
     * on this field.
     */
    public val isPackagePrivate: Boolean

    /**
     * Returns true if this member is synthetic.
     *
     * @see Member.isSynthetic
     */
    public val isSynthetic: Boolean

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
     * The potentially specialized class this member is declared in
     */
    public val declaringClass: ClassMirror

    /**
     * Returns a copy of this member with its enclosing class replaced with [enclosing]. Substituting type variables as
     * necessary. If the passed class is null this method removes any enclosing class specialization.
     *
     * **Note: A new mirror is only created if none already exist with the required specialization**
     *
     * @throws InvalidSpecializationException if [enclosing] is not equal to or a specialization of this
     * class's raw enclosing class
     * @return A copy of this member with the passed enclosing class, or with the raw enclosing class if [enclosing]
     * is null
     */
    public fun withDeclaringClass(enclosing: ClassMirror?): MemberMirror
}
