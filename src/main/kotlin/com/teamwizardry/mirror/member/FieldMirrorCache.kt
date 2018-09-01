package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.field.AbstractField
import com.teamwizardry.mirror.abstractionlayer.type.AbstractTypeVariable
import com.teamwizardry.mirror.type.TypeMirror

internal class FieldMirrorCache(private val cache: MirrorCache) {
    private val rawCache = mutableMapOf<AbstractField, FieldMirror>()
    private val specializedCache = mutableMapOf<Pair<AbstractField, TypeMirror>, FieldMirror>()


    fun reflect(field: AbstractField): FieldMirror {
        val cached = rawCache[field]
        if(cached != null) return cached

        val mirror = FieldMirror(cache, field)
        rawCache[field] = mirror
        return mirror
    }

    fun getFieldMirror(field: AbstractField, newType: TypeMirror): FieldMirror {
        specializedCache[field to newType]?.let { return it }
        val raw = this.reflect(field)
        val specialized: FieldMirror
        if(raw.type == newType) {
            specialized = raw
        } else {
            specialized = FieldMirror(cache, field)
            specialized.type = newType
            specialized.raw = raw
        }

        specializedCache[field to newType] = specialized
        return specialized
    }
}