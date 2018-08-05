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

    internal fun specializeMapping(field: AbstractField, mapping: Map<AbstractTypeVariable, TypeMirror>): FieldMirror {
        val rawMirror = reflect(field)
        val newType = cache.mapType(mapping, field.type)

        if(newType == rawMirror.type) return rawMirror

        val cached = specializedCache[field to newType]
        if(cached != null) return cached

        val newMirror = FieldMirror(cache, field)
        newMirror.type = newType
        newMirror.raw = rawMirror
        specializedCache[field to newType] = newMirror

        return newMirror
    }
}