package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

internal class MethodMirrorCache(private val cache: MirrorCache) {
    private val rawCache = ConcurrentHashMap<Method, MethodMirror>()
    private val specializedCache = ConcurrentHashMap<Pair<MethodMirror, MethodSpecialization>, MethodMirror>()

    fun reflect(method: Method): MethodMirror {
        return rawCache.getOrPut(method) { MethodMirror(cache, method, null, null) }
    }

    fun specialize(method: MethodMirror, specialization: MethodSpecialization): MethodMirror {
        val raw = method.raw
        return specializedCache.getOrPut(raw to specialization) {
            if(specialization.enclosing?.raw == specialization.enclosing &&
                (specialization.arguments == null || raw.typeParameters == specialization.arguments)) {
                return raw
            }
            return MethodMirror(cache, raw.java, raw.raw, specialization)
        }
    }
}
