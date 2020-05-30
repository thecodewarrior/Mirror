package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.MirrorCache
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

internal class ExecutableMirrorCache(private val cache: MirrorCache) {
    private val rawCache = ConcurrentHashMap<Executable, ExecutableMirror>()
    private val specializedCache = ConcurrentHashMap<Pair<ExecutableMirror, ExecutableSpecialization>, ExecutableMirror>()

    fun reflect(executable: Executable): ExecutableMirror {
        return rawCache.getOrPut(executable) {
            when (executable) {
                is Constructor<*> -> ConstructorMirror(cache, executable, null, null)
                is Method -> MethodMirror(cache, executable, null, null)
                else -> throw IllegalArgumentException("Unknown executable $executable")
            }
        }
    }

    fun specialize(executable: ExecutableMirror, specialization: ExecutableSpecialization): ExecutableMirror {
        val raw = executable.raw
        return specializedCache.getOrPut(raw to specialization) {
            when (raw) {
                is ConstructorMirror -> ConstructorMirror(cache, raw.java, raw, specialization)
                is MethodMirror -> MethodMirror(cache, raw.java, raw, specialization)
                else -> throw IllegalArgumentException("Unknown executable $executable")
            }
        }
    }
}
