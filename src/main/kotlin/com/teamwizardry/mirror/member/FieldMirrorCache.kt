package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.type.ClassMirror
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

    fun specialize(field: FieldMirror, enclosing: ClassMirror): FieldMirror {
        val raw = field.raw
        return specializedCache.getOrPut(raw to enclosing) {
            if(enclosing.raw == enclosing)
                return raw
            return FieldMirror(cache, raw, raw.java, enclosing)
        }
    }
}