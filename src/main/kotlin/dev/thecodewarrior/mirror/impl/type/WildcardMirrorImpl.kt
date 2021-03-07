package dev.thecodewarrior.mirror.impl.type

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.impl.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.type.WildcardMirror
import dev.thecodewarrior.mirror.impl.utils.Untested
import java.lang.reflect.AnnotatedWildcardType
import java.lang.reflect.WildcardType

internal class WildcardMirrorImpl internal constructor(
    override val cache: MirrorCache,
    override val coreType: WildcardType,
    // you can't annotate wildcard types, so type annotations are ignored, all we care about are the annotated bounds
    private val annotated: AnnotatedWildcardType?,
    raw: WildcardMirror?,
    override val specialization: TypeSpecialization.Wildcard?
): TypeMirrorImpl(), WildcardMirror {

    override val coreAnnotatedType: AnnotatedWildcardType = if(annotated != null)
        CoreTypeUtils.replaceAnnotations(annotated, typeAnnotations.toTypedArray())
    else
        CoreTypeUtils.annotate(coreType, typeAnnotations.toTypedArray()) as AnnotatedWildcardType

    override val raw: WildcardMirror = raw ?: this

    override val lowerBounds: List<TypeMirror> by lazy {
        annotated?.annotatedLowerBounds?.map { cache.types.reflect(it) }
            ?: this.coreType.lowerBounds.map { cache.types.reflect(it) }
    }

    @Untested
    override val lowerBound: TypeMirror?
        get() = lowerBounds.getOrNull(0)

    override val upperBounds: List<TypeMirror> by lazy {
        annotated?.annotatedUpperBounds?.map { cache.types.reflect(it) }
            ?: this.coreType.upperBounds.map { cache.types.reflect(it) }
    }

    @Untested
    override val upperBound: TypeMirror?
        get() = upperBounds.getOrNull(0)

    override fun defaultSpecialization() = TypeSpecialization.Wildcard.DEFAULT

    @Untested
    override fun withBounds(upperBounds: List<TypeMirror>?, lowerBounds: List<TypeMirror>?): WildcardMirror {
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

    override fun withTypeAnnotations(annotations: List<Annotation>): WildcardMirror {
        return withTypeAnnotationsImpl(annotations) as WildcardMirror
    }

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Wildcard>(
            specialization,
            { true }
        ) {
            WildcardMirrorImpl(cache, coreType, annotated, raw, it)
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

    @Untested
    override fun toString(): String {
        return toJavaString()
    }

    @Untested
    override fun toJavaString(): String {
        var str = "?"
        if(upperBounds.isNotEmpty() && upperBounds != listOf(cache.types.reflect(Any::class.java))) {
            // java spec doesn't have multi-bounded wildcards, but we don't want to throw away data, so join to ` & `
            str += " extends ${upperBounds.joinToString(" & ")}"
        }
        if(lowerBounds.isNotEmpty()) {
            // java spec doesn't have multi-bounded wildcards, but we don't want to throw away data, so join to ` & `
            str += " super ${lowerBounds.joinToString(" & ")}"
        }
        return str
    }

    @Untested
    override fun toKotlinString(): String {
        TODO("Not yet implemented")
    }
}
