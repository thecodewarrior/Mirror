package com.teamwizardry.mirror

import com.teamwizardry.mirror.abstractionlayer.type.*
import com.teamwizardry.mirror.member.FieldMirrorCache
import com.teamwizardry.mirror.type.*

internal class MirrorCache {
    val types = TypeMirrorCache(this)
    val fields = FieldMirrorCache(this)

    fun mapType(mapping: Map<AbstractTypeVariable, TypeMirror>, toMap: AbstractType<*>): TypeMirror {
        mapping[toMap]?.let {
            return it
        }
        val unspecialized = this.types.reflect(toMap)
        val specialized = this.types.specializeMapping(toMap, mapping)
        if(specialized != unspecialized) {
            return specialized
        }
        return unspecialized
    }


    companion object {
        @JvmStatic val DEFAULT = MirrorCache()
    }
}

