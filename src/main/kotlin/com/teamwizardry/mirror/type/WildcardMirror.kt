package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.coretypes.CoreTypeUtils
import java.lang.reflect.AnnotatedWildcardType
import java.lang.reflect.WildcardType

/**
 * The mirror type representing [wildcard types](https://docs.oracle.com/javase/tutorial/java/generics/wildcards.html).
 * Wildcard mirrors can have their bounds specialized.
 */
class WildcardMirror internal constructor(
    override val cache: MirrorCache,
    override val coreType: WildcardType,
    // type annotations are ignored, all we care about are the annotated bounds
    private val annotated: AnnotatedWildcardType?,
    raw: WildcardMirror?,
    override val specialization: TypeSpecialization.Wildcard?
): TypeMirror() {

    override val coreAnnotatedType: AnnotatedWildcardType = if(annotated != null)
        CoreTypeUtils.replaceAnnotations(annotated, typeAnnotations.toTypedArray())
    else
        CoreTypeUtils.annotate(coreType, typeAnnotations.toTypedArray()) as AnnotatedWildcardType

    override val raw: WildcardMirror = raw ?: this

    /**
     * `? super T` or `out T`. The lowermost type in the hierarchy that is valid. Any valid type must be a supertype
     * of `T`. The current language spec only allows for one, but the Core Reflection API supports multiple for
     * future-proofing reasons.
     *
     * In plain english, a lower bounded wildcard represents "somewhere I can put a < bound >", as opposed to upper
     * bounded wildcards being "some kind of < bound >".
     *
     * ```
     * For `? super AbstractList`
     * - Object          - Valid   - `Object myVar = myAbstractList;` compiles
     * - List            - Valid   - `List myVar = myAbstractList;` compiles
     * * AbstractList    - Valid   - `AbstractList myVar = myAbstractList;` compiles
     * - ArrayList       - Invalid - `ArrayList myVar = myAbstractList;` does not compile
     * ```
     */
    val lowerBounds: List<TypeMirror> by lazy {
        annotated?.annotatedLowerBounds?.map { cache.types.reflect(it) }
            ?: this.coreType.lowerBounds.map { cache.types.reflect(it) }
    }

    /**
     * `? super T` or `out T`. The lowermost type in the hierarchy that is valid. Any valid type must be a supertype
     * of `T`. This is a shorthand for the first element of [lowerBounds], if it exists, as currently that is the only
     * one supported by the language.
     *
     * In plain english, a lower bounded wildcard represents "somewhere I can put a < bound >", as opposed to upper
     * bounded wildcards being "some kind of < bound >".
     *
     * ```
     * For `? super AbstractList`
     * - Object          - Valid   - `Object myVar = myAbstractList;` compiles
     * - List            - Valid   - `List myVar = myAbstractList;` compiles
     * * AbstractList    - Valid   - `AbstractList myVar = myAbstractList;` compiles
     * - ArrayList       - Invalid - `ArrayList myVar = myAbstractList;` does not compile
     * ```
     */
    val lowerBound: TypeMirror?
        get() = lowerBounds.getOrNull(0)

    /**
     * `? extends T` or `in T`. The uppermost type in the hierarchy that is valid. Any valid type must be a subclass
     * of and implement the interfaces in [upperBounds]. The current language spec only allows for one, but the Core
     * Reflection API supports multiple for future-proofing reasons.
     *
     * In plain english, an upper bounded wildcard represents "some kind of < bound >", as opposed to lower bounded
     * wildcards being "somewhere I can put a < bound >".
     *
     * ```
     * For `? extends List`
     * - Object          - Invalid - `public List foo() { return myObject; }` does not compile
     * * List            - Valid   - `public List foo() { return myList; }` compiles
     * - AbstractList    - Valid   - `public List foo() { return myAbstractList; }` compiles
     * - ArrayList       - Valid   - `public List foo() { return myArrayList; }` compiles
     * ```
     */
    val upperBounds: List<TypeMirror> by lazy {
        annotated?.annotatedUpperBounds?.map { cache.types.reflect(it) }
            ?: this.coreType.upperBounds.map { cache.types.reflect(it) }
    }

    /**
     * `? extends T` or `in T`. The uppermost type in the hierarchy that is valid. Any valid type must be a subclass
     * of or implement the classes in [upperBounds]. This is a shorthand for the first element of [upperBounds], if it
     * exists, as currently that is the only one supported by the language.
     *
     * In plain english, an upper bounded wildcard represents "some kind of < bound >", as opposed to lower bounded
     * wildcards being "somewhere I can put a < bound >".
     *
     * ```
     * For `? extends List`
     * - Object          - Invalid - `public List foo() { return myObject; }` does not compile
     * * List            - Valid   - `public List foo() { return myList; }` compiles
     * - AbstractList    - Valid   - `public List foo() { return myAbstractList; }` compiles
     * - ArrayList       - Valid   - `public List foo() { return myArrayList; }` compiles
     * ```
     */
    val upperBound: TypeMirror?
        get() = upperBounds.getOrNull(0)

    override fun defaultSpecialization() = TypeSpecialization.Wildcard.DEFAULT

    /**
     * Specialize this wildcard with the provided upper and lower bounds. If the upper and lower bounds aren't
     */
    fun withBounds(upperBounds: List<TypeMirror>?, lowerBounds: List<TypeMirror>?): WildcardMirror {
        if(upperBounds != null && (upperBounds.size != raw.upperBounds.size ||
            raw.upperBounds.zip(upperBounds).any { (raw, specialized) ->
                !raw.isAssignableFrom(specialized)
            })
        )
            throw InvalidSpecializationException("Passed upper bounds $upperBounds are not assignable to raw " +
                "upper bounds ${raw.upperBounds}")
        if(lowerBounds != null && (lowerBounds.size != raw.lowerBounds.size ||
                raw.lowerBounds.zip(lowerBounds).any { (raw, specialized) ->
                    !raw.isAssignableFrom(specialized)
                })
        )
            throw InvalidSpecializationException("Passed lower bounds $lowerBounds are not assignable to raw " +
                "lower bounds ${raw.lowerBounds}")
        val newSpecialization = (specialization ?: defaultSpecialization())
            .copy(upperBounds = upperBounds, lowerBounds = lowerBounds)
        return cache.types.specialize(this, newSpecialization) as WildcardMirror
    }

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Wildcard>(
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
            str += " extends ${upperBounds.joinToString(" & ")}"
        }
        if(lowerBounds.isNotEmpty()) {
            // java spec doesn't have multi-bounded wildcards, but we don't want to throw away data, so join to ` & `
            str += " super ${lowerBounds.joinToString(" & ")}"
        }
        return str
    }
}