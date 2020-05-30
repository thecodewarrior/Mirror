package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
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

    fun specialize(field: FieldMirror, enclosing: ClassMirror?): FieldMirror {
        val raw = field.raw
        if(enclosing == null)
            return raw
        return specializedCache.getOrPut(raw to enclosing) {
            FieldMirror(cache, raw, raw.java, enclosing)
        }
    }
}