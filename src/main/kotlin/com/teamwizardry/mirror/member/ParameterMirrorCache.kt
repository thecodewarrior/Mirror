package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.method.AbstractParameter
import com.teamwizardry.mirror.type.TypeMirror
import java.util.concurrent.ConcurrentHashMap

internal class ParameterMirrorCache(private val cache: MirrorCache) {
    private val rawCache = ConcurrentHashMap<AbstractParameter, ParameterMirror>()
    private val specializedCache = ConcurrentHashMap<Pair<AbstractParameter, TypeMirror>, ParameterMirror>()

    fun reflect(parameter: AbstractParameter): ParameterMirror {
        return rawCache.getOrPut(parameter) { ParameterMirror(cache, parameter) }
    }

    fun getParameterMirror(parameter: AbstractParameter, type: TypeMirror): ParameterMirror {
        return specializedCache.getOrPut(parameter to type) {
            val raw = reflect(parameter)
            val specialized: ParameterMirror
            if (raw.type == type) {
                specialized = raw
            } else {
                specialized = ParameterMirror(cache, parameter)
                specialized.type = type
                specialized.raw = raw
            }
            return@getOrPut specialized
        }
    }
}
