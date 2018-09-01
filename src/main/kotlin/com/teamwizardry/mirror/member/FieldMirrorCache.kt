package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.field.AbstractField
import com.teamwizardry.mirror.type.TypeMirror
import java.util.concurrent.ConcurrentHashMap

internal class FieldMirrorCache(private val cache: MirrorCache) {
    private val rawCache = ConcurrentHashMap<AbstractField, FieldMirror>()
    private val specializedCache = ConcurrentHashMap<Pair<AbstractField, TypeMirror>, FieldMirror>()

    fun reflect(field: AbstractField): FieldMirror {
        return rawCache.getOrPut(field) { FieldMirror(cache, field) }
    }

    fun getFieldMirror(field: AbstractField, newType: TypeMirror): FieldMirror {
        return specializedCache.getOrPut(field to newType) {
            val raw = this.reflect(field)
            val specialized: FieldMirror
            if(raw.type == newType) {
                specialized = raw
            } else {
                specialized = FieldMirror(cache, field)
                specialized.type = newType
                specialized.raw = raw
            }
            return@getOrPut specialized
        }
    }
}