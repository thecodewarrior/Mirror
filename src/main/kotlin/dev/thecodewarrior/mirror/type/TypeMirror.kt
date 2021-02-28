package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.impl.utils.Untested
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
    public val typeAnnotations: List<Annotation>

    /**
     * Determines if this mirror represents a logical supertype of the passed mirror, i.e. whether a value of type
     * [other] could be "cast" to the type represented by this mirror, including generic type arguments.
     *
     * @see Class.isAssignableFrom
     */
    public fun isAssignableFrom(other: TypeMirror): Boolean

    /**
     * Creates a copy of this type mirror that has been specialized to have the passed
     * [type annotation][java.lang.annotation.ElementType.TYPE_USE]. Type annotations on this mirror will not be
     * present on the resulting mirror.
     *
     * In the case of [ClassMirror], these are not the annotations present on the class _declaration,_
     * they are the annotations present on the _use_ of the type.
     */
    public fun withTypeAnnotations(annotations: List<Annotation>): TypeMirror

    /**
     * Returns true if the specified [type annotation][java.lang.annotation.ElementType.TYPE_USE] is present on this
     * type.
     */
    @Untested
    public fun isTypeAnnotationPresent(annotationType: Class<out Annotation>): Boolean

    /**
     * Returns the [type annotation][java.lang.annotation.ElementType.TYPE_USE] of the specified type, or null if no
     * such annotation is present.
     */
    @Untested
    public fun <T: Annotation> getTypeAnnotation(annotationClass: Class<T>): T?

    /**
     * Returns the [type annotation][java.lang.annotation.ElementType.TYPE_USE] with the specified type, detecting
     * repeatable annotations.
     */
    @Untested
    public fun <T: Annotation> getTypeAnnotationsByType(annotationClass: Class<T>): List<T>

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
}
