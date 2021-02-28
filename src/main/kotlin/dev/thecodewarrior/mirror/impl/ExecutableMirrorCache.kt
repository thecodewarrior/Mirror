package dev.thecodewarrior.mirror.impl

import dev.thecodewarrior.mirror.impl.member.ConstructorMirrorImpl
import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.member.ExecutableMirror
import dev.thecodewarrior.mirror.impl.member.ExecutableSpecialization
import dev.thecodewarrior.mirror.impl.member.MethodMirrorImpl
import dev.thecodewarrior.mirror.member.MethodMirror
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
                is Constructor<*> -> ConstructorMirrorImpl(cache, executable, null, null)
                is Method -> MethodMirrorImpl(cache, executable, null, null)
                else -> throw IllegalArgumentException("Unknown executable $executable")
            }
        }
    }

    fun specialize(executable: ExecutableMirror, specialization: ExecutableSpecialization): ExecutableMirror {
        val raw = executable.raw
        return specializedCache.getOrPut(raw to specialization) {
            when (raw) {
                is ConstructorMirror -> ConstructorMirrorImpl(cache, raw.java, raw as ConstructorMirrorImpl, specialization)
                is MethodMirror -> MethodMirrorImpl(cache, raw.java, raw as MethodMirrorImpl, specialization)
                else -> throw IllegalArgumentException("Unknown executable $executable")
            }
        }
    }
}
