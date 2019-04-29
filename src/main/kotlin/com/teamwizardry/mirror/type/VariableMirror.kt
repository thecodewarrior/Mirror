package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.coretypes.CoreTypeUtils
import java.lang.reflect.AnnotatedTypeVariable
import java.lang.reflect.TypeVariable

class VariableMirror internal constructor(
    override val cache: MirrorCache,
    override val coreType: TypeVariable<*>,
    raw: VariableMirror?,
    override val specialization: TypeSpecialization.Common?
): TypeMirror() {

    override val coreAnnotatedType: AnnotatedTypeVariable
        = CoreTypeUtils.annotate(coreType, typeAnnotations.toTypedArray()) as AnnotatedTypeVariable

    override val raw: VariableMirror = raw ?: this

    /**
     * The bounds of this variable. Types specializing this variable must extend all of these.
     *
     * By default it contains the [Object] mirror.
     */
    val bounds: List<TypeMirror> by lazy {
        coreType.annotatedBounds.map { cache.types.reflect(it) }
    }

    override fun defaultSpecialization() = TypeSpecialization.Common.DEFAULT

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Common>(
            specialization,
            { true }
        ) {
            VariableMirror(cache, coreType, raw, it)
        }
    }

    override fun isAssignableFrom(other: TypeMirror): Boolean {
        if(other == this) return true
        if(other is VariableMirror)
            return this.bounds.zip(other.bounds).all { (ours, theirs) -> ours.isAssignableFrom(theirs) }
        return bounds.all {
            it.isAssignableFrom(other)
        }
    }

    override fun toString(): String {
        var str = ""
        str += coreType.name
        if(bounds.isNotEmpty()) {
            str += " extends ${bounds.joinToString(" & ")}"
        }
        return str
    }
}
