package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.unmodifiableView
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type

/**
 * The common superclass of all mirrors that represent types.
 *
 * @see ArrayMirror
 * @see ClassMirror
 * @see VariableMirror
 * @see VoidMirror
 * @see WildcardMirror
 */
abstract class TypeMirror internal constructor() {
    /**
     * The cache this mirror was created by. Mirrors from other caches will not be considered equal even if they
     * represent the same type. However, no production code should use anything but
     * [dev.thecodewarrior.mirror.Mirror.reflect], which uses a global cache.
     */
    internal abstract val cache: MirrorCache

    /**
     * The Java Core Reflection type this mirror represents
     */
    abstract val coreType: Type

    /**
     * The Java Core Reflection annotated type this mirror represents.
     *
     * **!!NOTE!!** The JVM implementations of `AnnotatedType` don't implement `equals` or `hashCode`, so they will
     * equal neither each other nor this.
     * If you need these methods pass any annotated type through [Mirror.toCanonical].
     */
    abstract val coreAnnotatedType: AnnotatedType

    /**
     * The JVM erasure of this type.
     */
    val erasure: Class<*> get() = CoreTypeUtils.erase(coreType)

    internal abstract val specialization: TypeSpecialization?
    internal abstract fun defaultSpecialization(): TypeSpecialization

    /**
     * The mirror representing this type without any generic specialization
     */
    abstract val raw: TypeMirror

    internal abstract fun applySpecialization(specialization: TypeSpecialization): TypeMirror

    /**
     * Determines if this mirror represents a logical supertype of the passed mirror, i.e. whether a value of type
     * [other] could be "cast" to the type represented by this mirror, including generic type arguments.
     *
     * @see Class.isAssignableFrom
     */
    abstract fun isAssignableFrom(other: TypeMirror): Boolean

    internal inline fun <reified T: TypeSpecialization> defaultApplySpecialization(
        specialization: TypeSpecialization,
        rawTest: (T) -> Boolean,
        crossinline specializedConstructor: (T) -> TypeMirror
    ): TypeMirror {
        if(specialization !is T)
            throw InvalidSpecializationException("Can't apply ${specialization.javaClass}" +
                " to ${this.javaClass.simpleName } $this")
        if(specialization.annotations == this.specialization?.annotations ?: emptyList<Annotation>() &&
            rawTest(specialization)
        )
            return raw

        return specializedConstructor(specialization)
    }

    /**
     * Creates a copy of this type mirror that has been specialized to have the passed
     * [type annotation][java.lang.annotation.ElementType.TYPE_USE]. Type annotations on this mirror will not be
     * present on the resulting mirror.
     *
     * In the case of [ClassMirror], these are not the annotations present on the class _declaration,_
     * they are the annotations present on the _use_ of the type.
     */
    fun withTypeAnnotations(annotations: List<Annotation>): TypeMirror {
        return cache.types.specialize(raw,
            (this.specialization ?: this.defaultSpecialization()).copy(
                annotations = annotations
            )
        )
    }

//region Type Annotations
    /**
     * The [type annotations][java.lang.annotation.ElementType.TYPE_USE] present on this type.
     * In the case of [ClassMirror], these are not the annotations present on the class _declaration,_
     * they are the annotations present on the _use_ of the type.
     */
    val typeAnnotations: List<Annotation>
        get() = specialization?.annotations ?: emptyList()

    /**
     * Returns true if the specified annotation is present on this class.
     */
    @Untested
    fun isTypeAnnotationPresent(annotationType: Class<out Annotation>): Boolean {
        return this.getTypeAnnotation(annotationType) != null
    }

    /**
     * Returns the annotation of the specified type, or null if no such annotation is present.
     */
    @Untested
    fun <T: Annotation> getTypeAnnotation(annotationClass: Class<T>): T? {
        return coreAnnotatedType.getDeclaredAnnotation(annotationClass)
    }

    @Untested
    fun <T: Annotation> getTypeAnnotationsByType(annotationClass: Class<T>): List<T> {
        return coreAnnotatedType.getDeclaredAnnotationsByType(annotationClass).toList()
    }

    /**
     * Returns true if the specified annotation is present on this class.
     */
    inline fun <reified T: Annotation> isTypeAnnotationPresent(): Boolean = this.isTypeAnnotationPresent(T::class.java)

    /**
     * Returns the annotation of the specified type, or null if no such annotation is present.
     */
    inline fun <reified T: Annotation> getTypeAnnotation(): T? = this.getTypeAnnotation(T::class.java)
//endregion

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
    fun asClassMirror(): ClassMirror {
        return this as ClassMirror
    }

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
    fun asArrayMirror(): ArrayMirror {
        return this as ArrayMirror
    }

    /**
     * The "specificity" of this type. Essentially a more specific type can be cast to a less specific type, but not
     * the other way around. If neither type can be cast to the other or both types can be cast to each other they have
     * equal specificity.
     *
     * Specifically, a "more specific" type is one where `other.isAssignableFrom(this)` and not
     * `this.isAssignableFrom(other)`.
     */
    val specificity: Specificity = Specificity(this)

    @Untested
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeMirror) return false
        if (other.javaClass != this.javaClass) return false

        if (cache != other.cache) return false
        if (coreType != other.coreType) return false
        if (specialization != other.specialization) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cache.hashCode()
        result = 31 * result + coreType.hashCode()
        result = 31 * result + specialization.hashCode()
        return result
    }

    /**
     * A wrapper class that allows comparing TypeMirrors by how "specific" they are.
     * A more specific type is one where `other.isAssignableFrom(this)` and the inverse is not true.
     */
    class Specificity internal constructor(private val type: TypeMirror): Comparable<Specificity> {
        override fun compareTo(other: Specificity): Int {
            if(this == other) return 0
            val thisAssignableFromOther = type.isAssignableFrom(other.type)
            val otherAssignableFromThis = other.type.isAssignableFrom(type)

            if(thisAssignableFromOther == otherAssignableFromThis)
                return 0

            return when {
                thisAssignableFromOther  -> -1
                otherAssignableFromThis  -> 1
                else -> 0
            }
        }
    }
}

