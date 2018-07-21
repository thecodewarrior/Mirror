package com.teamwizardry.mirror.reflection

import com.teamwizardry.mirror.reflection.abstractionlayer.type.*
import com.teamwizardry.mirror.reflection.member.field.FieldMirrorCache
import com.teamwizardry.mirror.reflection.type.*

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

