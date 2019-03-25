package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.coretypes.CoreTypeFactory
import java.lang.reflect.AnnotatedType

class VoidMirror internal constructor(
    override val cache: MirrorCache,
    override val coreType: Class<*>,
    raw: VoidMirror?,
    override val specialization: TypeSpecialization.Common?
): TypeMirror() {

    override val coreAnnotatedType: AnnotatedType
        = CoreTypeFactory.annotate(coreType, typeAnnotations.toTypedArray())

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
}