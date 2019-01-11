package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import java.lang.reflect.Type

/**
 * An abstract representation of a Java type that allows simpler reflective access to it, its members, and the generic
 * type information provided by the JVM.
 *
 * Mirrors can be "specialized", which results in the generic type arguments being substituted all the way down the
 * chain. This substitution means that the mirror of [java.util.HashMap]`<Foo, Bar>` would have a superclass
 * [java.util.AbstractMap]`<Foo, Bar>` and a [java.util.Map.get] method whose return value is `Bar`. This "trickle down"
 * approach makes generic reflection dead easy and is much better than the mind-numbingly complex task of tracing type
 * parameters upward to figure out where they are defined.
 *
 * @see ClassMirror
 * @see ArrayMirror
 * @see VariableMirror
 * @see WildcardMirror
 */
abstract class TypeMirror internal constructor() {
    /**
     * The cache this mirror was created by. Mirrors from other caches will not be considered equal even if they
     * represent the same type. However, no production code should use anything but
     * [com.teamwizardry.mirror.Mirror.reflect], which uses a global cache.
     */
    internal abstract val cache: MirrorCache

    /**
     * The Java Core Reflection type this mirror represents
     */
    abstract val java: Type

    internal abstract val specialization: TypeSpecialization?
    internal abstract fun defaultSpecialization(): TypeSpecialization

    abstract val raw: TypeMirror

    internal abstract fun applySpecialization(specialization: TypeSpecialization): TypeMirror

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

    val typeAnnotations: List<Annotation>
        get() = specialization?.annotations ?: emptyList()

    fun annotate(annotations: List<Annotation>): TypeMirror {
        return cache.types.specialize(raw,
            (this.specialization ?: this.defaultSpecialization()).copy(
                annotations = annotations
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeMirror) return false
        if (other.javaClass != this.javaClass) return false

        if (cache != other.cache) return false
        if (java != other.java) return false
        if (specialization != other.specialization) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cache.hashCode()
        result = 31 * result + java.hashCode()
        result = 31 * result + specialization.hashCode()
        return result
    }
}

