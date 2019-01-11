package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache

class VoidMirror internal constructor(
    override val cache: MirrorCache,
    override val java: Class<*>,
    raw: VoidMirror?,
    override val specialization: TypeSpecialization.Common?
): TypeMirror() {

    override val raw: VoidMirror = raw ?: this

    override fun defaultSpecialization() = TypeSpecialization.Common.DEFAULT

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Common>(
            specialization,
            { true }
        ) {
            VoidMirror(cache, java, raw, it)
        }
    }
}