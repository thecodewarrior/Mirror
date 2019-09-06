package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.coretypes.CoreTypeUtils
import java.lang.reflect.AnnotatedType

/**
 * The mirror representing the primitive `void` type
 */
class VoidMirror internal constructor(
    override val cache: MirrorCache,
    override val coreType: Class<*>,
    raw: VoidMirror?,
    override val specialization: TypeSpecialization.Common?
): TypeMirror() {

    override val coreAnnotatedType: AnnotatedType
        = CoreTypeUtils.annotate(coreType, typeAnnotations.toTypedArray())

    override val raw: VoidMirror = raw ?: this

    override fun defaultSpecialization() = TypeSpecialization.Common.DEFAULT

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Common>(
            specialization,
            { true }
        ) {
            VoidMirror(cache, coreType, raw, it)
        }
    }

    override fun isAssignableFrom(other: TypeMirror): Boolean {
        return other == this
    }

    override fun toString(): String {
        return "void"
    }
}