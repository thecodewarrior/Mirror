package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.method.AbstractParameter
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.lazyOrSet
import java.lang.reflect.Parameter

class ParameterMirror internal constructor(internal val cache: MirrorCache, internal val abstractParameter: AbstractParameter) {
    val java: Parameter = abstractParameter.parameter

    val name: String? = abstractParameter.name

    var raw: ParameterMirror = this
        internal set

    var type: TypeMirror by lazyOrSet {
        abstractParameter.type.annotated?.let { cache.types.reflect(it) }
            ?: cache.types.reflect(abstractParameter.type.type)
    }
        internal set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParameterMirror) return false

        if (cache != other.cache) return false
        if (abstractParameter != other.abstractParameter) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cache.hashCode()
        result = 31 * result + abstractParameter.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        var str = ""
        str += "$type $name"
        return str
    }
}