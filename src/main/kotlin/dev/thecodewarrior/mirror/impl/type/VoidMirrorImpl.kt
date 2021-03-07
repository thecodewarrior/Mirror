package dev.thecodewarrior.mirror.impl.type

import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.impl.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.type.VoidMirror
import java.lang.reflect.AnnotatedType

internal class VoidMirrorImpl internal constructor(
    override val cache: MirrorCache,
    override val coreType: Class<*>,
    raw: VoidMirror?,
    override val specialization: TypeSpecialization.Common?
): TypeMirrorImpl(), VoidMirror {

    override val coreAnnotatedType: AnnotatedType
            = CoreTypeUtils.annotate(coreType, typeAnnotations.toTypedArray())

    override val raw: VoidMirror = raw ?: this

    override fun withTypeAnnotations(annotations: List<Annotation>): VoidMirror {
        return withTypeAnnotationsImpl(annotations) as VoidMirror
    }

    override fun defaultSpecialization() = TypeSpecialization.Common.DEFAULT

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Common>(
            specialization,
            { true }
        ) {
            VoidMirrorImpl(cache, coreType, raw, it)
        }
    }

    override fun isAssignableFrom(other: TypeMirror): Boolean {
        return other == this
    }

    override fun toString(): String {
        return toJavaString()
    }

    override fun toJavaString(): String {
        return typeAnnotations.toJavaString(joiner = " ", trailing = " ") + "void"
    }

    override fun toKotlinString(): String {
        return typeAnnotations.toKotlinString(joiner = " ", trailing = " ") + "Unit"
    }
}
