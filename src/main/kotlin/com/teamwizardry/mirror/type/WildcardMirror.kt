package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import java.lang.reflect.AnnotatedWildcardType
import java.lang.reflect.WildcardType

class WildcardMirror internal constructor(
    override val cache: MirrorCache,
    override val java: WildcardType,
    // type annotations are ignored, all we care about are the annotated bounds
    val annotated: AnnotatedWildcardType?,
    raw: WildcardMirror?,
    override val specialization: TypeSpecialization.Common?
): TypeMirror() {

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
            ?: this.java.lowerBounds.map { cache.types.reflect(it) }
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
            ?: this.java.upperBounds.map { cache.types.reflect(it) }
    }

    override val raw: WildcardMirror = raw ?: this

    override fun defaultSpecialization() = TypeSpecialization.Common.DEFAULT

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Common>(
            specialization,
            { true }
        ) {
            WildcardMirror(cache, java, annotated, raw, it)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WildcardMirror) return false

        if (cache != other.cache) return false
        if (java != other.java) return false
        val annotated = this.annotated
        val otherAnnotated = other.annotated
        if (annotated != null && otherAnnotated != null && annotated != otherAnnotated) {
            if (
                !annotated.annotatedUpperBounds!!.contentEquals(otherAnnotated.annotatedUpperBounds) ||
                !annotated.annotatedLowerBounds!!.contentEquals(otherAnnotated.annotatedLowerBounds)
            ) return false
        }
        if (specialization != other.specialization) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cache.hashCode()
        result = 31 * result + java.hashCode()
        result = 31 * result + annotated?.annotatedUpperBounds.hashCode()
        result = 31 * result + annotated?.annotatedUpperBounds.hashCode()
        result = 31 * result + specialization.hashCode()
        return result
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