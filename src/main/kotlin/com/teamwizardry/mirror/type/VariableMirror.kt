package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import java.lang.reflect.TypeVariable

/**
 * Represents type variables (the `T` in `List<T>` and `public T theField`)
 */
class VariableMirror internal constructor(
    override val cache: MirrorCache,
    override val java: TypeVariable<*>,
    raw: VariableMirror?,
    override val specialization: TypeSpecialization.Common?
): TypeMirror() {

    /**
     * The bounds of this variable. Types specializing this variable must extend all of these.
     *
     * By default it contains the [Object] mirror.
     */
    val bounds: List<TypeMirror> by lazy {
        java.annotatedBounds.map { cache.types.reflect(it) }
    }

    override val raw: TypeMirror = raw ?: this

    override fun defaultSpecialization() = TypeSpecialization.Common.DEFAULT

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Common>(
            specialization,
            { true }
        ) {
            VariableMirror(cache, java, this, it)
        }
    }

    override fun toString(): String {
        var str = ""
        str += java.name
        if(bounds.isNotEmpty()) {
            str += " extends ${bounds.joinToString(" & ")}"
        }
        return str
    }
}
