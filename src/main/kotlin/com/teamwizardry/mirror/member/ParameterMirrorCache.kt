package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import java.lang.reflect.Parameter
import java.util.concurrent.ConcurrentHashMap

internal class ParameterMirrorCache(private val cache: MirrorCache) {
    private val rawCache = ConcurrentHashMap<Parameter, ParameterMirror>()
    private val specializedCache = ConcurrentHashMap<Pair<ParameterMirror, ExecutableMirror>, ParameterMirror>()

    fun reflect(parameter: Parameter): ParameterMirror {
        return rawCache.getOrPut(parameter) {
            ParameterMirror(cache, null, null, parameter)
        }
    }

    fun specialize(parameter: ParameterMirror, executable: ExecutableMirror): ParameterMirror {
        val raw = parameter.raw
        return specializedCache.getOrPut(raw to executable) {
            if(raw.declaringExecutable == executable)
                raw
            else
                ParameterMirror(cache, raw, executable, raw.java)
        }
    }
}
