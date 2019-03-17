package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.type.TypeMapping
import com.teamwizardry.mirror.type.TypeMirror
import java.lang.reflect.Parameter

class ParameterMirror internal constructor(
    internal val cache: MirrorCache,
    raw: ParameterMirror?,
    val specialization: ExecutableMirror?,
    internal val java: Parameter
) {
    val name: String? = if(java.isNamePresent) java.name else null

    val raw: ParameterMirror = raw ?: this

    val type: TypeMirror by lazy {
        java.annotatedType.let {
            genericMapping[cache.types.reflect(it)]
        }
    }

    val genericMapping: TypeMapping by lazy {
        TypeMapping(emptyMap()) + specialization?.genericMapping
    }

    fun specialize(executable: ExecutableMirror): ParameterMirror {
        if(executable.java != java.declaringExecutable)
            throw InvalidSpecializationException("Invalid enclosing " +
                (if(executable is ConstructorMirror) "constructor" else "method") +
                " $executable. $this is declared in ${java.declaringExecutable}")
        return cache.parameters.specialize(this, executable)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParameterMirror) return false

        if (cache != other.cache) return false
        if (java != other.java) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cache.hashCode()
        result = 31 * result + java.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        var str = ""
        str += "$type $name"
        return str
    }
}