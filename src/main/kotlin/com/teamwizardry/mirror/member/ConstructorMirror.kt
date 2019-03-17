package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import java.lang.reflect.Constructor

//TODO tests
class ConstructorMirror internal constructor(
    cache: MirrorCache,
    override val java: Constructor<*>,
    raw: ConstructorMirror?,
    specialization: ExecutableSpecialization?
): ExecutableMirror(cache, raw, specialization) {

    override val raw: ConstructorMirror = raw ?: this
    override val name: String = java.name

    override fun specialize(vararg parameters: TypeMirror): ConstructorMirror {
        return super.specialize(*parameters) as ConstructorMirror
    }

    override fun enclose(type: ClassMirror): ConstructorMirror {
        return super.enclose(type) as ConstructorMirror
    }

    override fun toString(): String {
        var str = name
        if(typeParameters.isNotEmpty()) {
            str += "<${typeParameters.joinToString(", ")}>"
        }
        str += "(${parameters.joinToString(", ")})"
        return str
    }
}