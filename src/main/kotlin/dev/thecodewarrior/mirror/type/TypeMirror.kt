package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.impl.utils.Untested
import dev.thecodewarrior.mirror.util.AnnotationList
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type

/**
 * The common superinterface of all mirrors that represent types.
 *
 * @see ArrayMirror
 * @see ClassMirror
 * @see TypeVariableMirror
 * @see VoidMirror
 * @see WildcardMirror
 */
public interface TypeMirror {
    /**
     * The Java Core Reflection type this mirror represents
     */
    public val coreType: Type

    /**
     * The Java Core Reflection annotated type this mirror represents.
     *
     * **!!NOTE!!** The JVM implementations of `AnnotatedType` don't implement `equals` or `hashCode`, so they will
     * equal neither each other nor this.
     * If you need these methods pass any annotated type through [Mirror.toCanonical].
     */
    public val coreAnnotatedType: AnnotatedType

    /**
     * The JVM erasure of this type.
     */
    public val erasure: Class<*>

    /**
     * The mirror representing this type without any generic specialization
     */
    public val raw: TypeMirror

    /**
     * The [type annotations][java.lang.annotation.ElementType.TYPE_USE] present on this type.
     * These are not the annotations present on the _declaration,_ they are the annotations present on the _use_ of the
     * type.
     */
    public val typeAnnotations: AnnotationList

    /**
     * Determines if this mirror represents a logical supertype of the passed mirror, i.e. whether a value of type
     * [other] could be "cast" to the type represented by this mirror, including generic type arguments.
     *
     * @see Class.isAssignableFrom
     */
    public fun isAssignableFrom(other: TypeMirror): Boolean

    /**
     * Creates a copy of this type mirror that has been specialized to have the passed
     * [Type Annotations][typeAnnotations]. Type annotations on this mirror will not carry over to the resulting mirror.
     *
     * Note that are not the annotations present on the class _declaration,_ they are the annotations present on the
     * _use_ of the type.
     */
    public fun withTypeAnnotations(annotations: List<Annotation>): TypeMirror

    /**
     * Casts this TypeMirror to ClassMirror. Designed to avoid the nested casts from hell:
     * ```
     * ((ClassMirror) ((ClassMirror) ((ClassMirror) Mirror.reflect(SomeType.class).findField("name").getType()).getTypeParameters()[0]).findField("name2").getType()).getSimpleName()
     * // vs.
     * Mirror.reflect(SomeType.class).findField("name")
     *     .getType().asClassMirror()
     *     .getTypeParameters()[0].asClassMirror()
     *     .findField("name").getType().asClassMirror().getSimpleName()
     * ```
     *
     * @throws ClassCastException if this TypeMirror object is not a ClassMirror
     */
    @Throws(ClassCastException::class)
    public fun asClassMirror(): ClassMirror

    /**
     * Casts this TypeMirror to ArrayMirror. Designed to avoid the nested casts from hell:
     * ```
     * ((ClassMirror) ((ArrayMirror) ((ArrayMirror) ((ArrayMirror) Mirror.reflect(String[][][].class)).getComponent()).getComponent()).getComponent())
     * // vs.
     * Mirror.reflect(String[][][].class).asArrayMirror()
     *     .getComponent().asArrayMirror()
     *     .getComponent().asArrayMirror()
     *     .getComponent().asClassMirror()
     * ```
     *
     * @throws ClassCastException if this TypeMirror object is not a ArrayMirror
     */
    @Throws(ClassCastException::class)
    public fun asArrayMirror(): ArrayMirror

    /**
     * Returns a string approximating the appearance of this type when used in Java source code.
     */
    @Untested
    public override fun toString(): String

    /**
     * Returns a string approximating the appearance of this type when used in Java source code.
     */
    @Untested
    public fun toJavaString(): String

    /**
     * Returns a string approximating the appearance of this type when used in Kotlin source code.
     */
    @Untested
    public fun toKotlinString(): String
}
