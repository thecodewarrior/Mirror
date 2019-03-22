package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import io.leangen.geantyref.GenericTypeReflector
import java.lang.reflect.AnnotatedWildcardType
import java.lang.reflect.WildcardType

class WildcardMirror internal constructor(
    override val cache: MirrorCache,
    override val coreType: WildcardType,
    // type annotations are ignored, all we care about are the annotated bounds
    private val annotated: AnnotatedWildcardType?,
    raw: WildcardMirror?,
    override val specialization: TypeSpecialization.Common?
): TypeMirror() {

    override val coreAnnotatedType: AnnotatedWildcardType = if(annotated != null)
        GenericTypeReflector.replaceAnnotations(annotated, typeAnnotations.toTypedArray())
    else
        GenericTypeReflector.annotate(coreType, typeAnnotations.toTypedArray()) as AnnotatedWildcardType

    override val raw: WildcardMirror = raw ?: this

    /**
     * `? super T` or `out T`. The lowermost type in the hierarchy that is valid. Any valid type must be a supertype
     * of `T`.
     *
     * When referring to something with this type you will be able to store an instance of the lower bounds.
     *
     * ```
     * For `? super AbstractList`
     * - Object          - Valid
     * - List<T>         - Valid
     * * AbstractList<T> - Valid
     * - ArrayList<>     - Invalid. `anArrayList = superAbstractListVariable` will throw
     */
    val lowerBounds: List<TypeMirror> by lazy {
        annotated?.annotatedLowerBounds?.map { cache.types.reflect(it) }
            ?: this.coreType.lowerBounds.map { cache.types.reflect(it) }
    }

    /**
     * `? extends T` or `in T`. The uppermost type in the hierarchy that is valid. Any valid type must be a subclass
     * of or implement the classes in [upperBounds]
     *
     * When referring to something with this type you will get something that is an instance of the upper bounds.
     *
     * ```
     * For `? extends List`
     * - Object          - Invalid. `extendsListVariable = anObject` will throw
     * * List<T>         - Valid
     * - AbstractList<T> - Valid
     * - ArrayList<T>    - Valid
     * ```
     */
    val upperBounds: List<TypeMirror> by lazy {
        annotated?.annotatedUpperBounds?.map { cache.types.reflect(it) }
            ?: this.coreType.upperBounds.map { cache.types.reflect(it) }
    }

    override fun defaultSpecialization() = TypeSpecialization.Common.DEFAULT

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Common>(
            specialization,
            { true }
        ) {
            WildcardMirror(cache, coreType, annotated, raw, it)
        }
    }

    override fun isAssignableFrom(other: TypeMirror): Boolean {
        if(other == this) return true
        if(other is WildcardMirror)
            return this.upperBounds.zip(other.upperBounds).all { (ours, theirs) -> ours.isAssignableFrom(theirs) } &&
                this.lowerBounds.zip(other.lowerBounds).all { (ours, theirs) -> ours.isAssignableFrom(theirs) }
        return upperBounds.all {
            it.isAssignableFrom(other)
        } && lowerBounds.all {
            other.isAssignableFrom(it)
        }
    }

    override fun toString(): String {
        var str = "?"
        if(upperBounds.isNotEmpty()) {
            // java spec doesn't have multi-bounded wildcards, but we don't want to throw away data, so join to ` & `
            str += " super ${upperBounds.joinToString(" & ")}"
        }
        if(lowerBounds.isNotEmpty()) {
            // java spec doesn't have multi-bounded wildcards, but we don't want to throw away data, so join to ` & `
            str += " extends ${lowerBounds.joinToString(" & ")}"
        }
        return str
    }
}