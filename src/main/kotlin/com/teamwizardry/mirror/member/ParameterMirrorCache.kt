package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import java.lang.reflect.Parameter
import java.util.concurrent.ConcurrentHashMap

internal class ParameterMirrorCache(private val cache: MirrorCache) {
    private val rawCache = ConcurrentHashMap<Parameter, ParameterMirror>()
    private val specializedCache = ConcurrentHashMap<Pair<ParameterMirror, MethodMirror>, ParameterMirror>()

    fun reflect(parameter: Parameter): ParameterMirror {
        return rawCache.getOrPut(parameter) {
            ParameterMirror(cache, null, null, parameter)
        }
    }

    fun specialize(parameter: ParameterMirror, method: MethodMirror): ParameterMirror {
        val raw = parameter.raw
        return specializedCache.getOrPut(raw to method) {
            if(method.raw == method)
                raw
            else
                ParameterMirror(cache, raw, method, raw.java)
        }
    }
}
