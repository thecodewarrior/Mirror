package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.type.TypeMirror
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

internal class FieldMirrorCache(private val cache: MirrorCache) {
    private val rawCache = ConcurrentHashMap<Field, FieldMirror>()
    private val specializedCache = ConcurrentHashMap<Pair<FieldMirror, TypeMirror>, FieldMirror>()

    fun reflect(field: Field): FieldMirror {
        return rawCache.getOrPut(field) {
            FieldMirror(cache, null, field, null)
        }
    }

    fun specialize(raw: FieldMirror, newType: TypeMirror): FieldMirror {
        return specializedCache.getOrPut(raw to newType) {
            if(raw.type == newType)
                return raw
            return FieldMirror(cache, raw, raw.java, newType)
        }
    }
}