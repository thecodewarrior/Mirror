package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.method.AbstractMethod
import com.teamwizardry.mirror.type.TypeMirror
import java.util.concurrent.ConcurrentHashMap

internal class MethodMirrorCache(private val cache: MirrorCache) {
    private val rawCache = ConcurrentHashMap<AbstractMethod, MethodMirror>()
    private val specializedCache = ConcurrentHashMap<MirrorSignature, MethodMirror>()

    fun reflect(method: AbstractMethod): MethodMirror {
        return rawCache.getOrPut(method) { MethodMirror(cache, method) }
    }

    fun getMethodMirror(method: AbstractMethod, returnType: TypeMirror, paramTypes: List<TypeMirror>): MethodMirror {
        val signature = MirrorSignature(method, returnType, paramTypes)
        return specializedCache.getOrPut(signature) {
            val raw = reflect(method)
            val specialized: MethodMirror
            if(raw.parameters.size != paramTypes.size)
                throw IllegalArgumentException("Mismatched param type count specializing method. " +
                    "Parameter count: ${raw.parameters.size}, passed param type count: ${paramTypes.size}")
            if (
                raw.returnType == returnType &&
                raw.parameterTypes == paramTypes
            ) {
                specialized = raw
            } else {
                specialized = MethodMirror(cache, method)
                specialized.returnType = returnType
                specialized.parameters = raw.parameters.zip(paramTypes).map {
                    cache.parameters.getParameterMirror(it.first.abstractParameter, it.second)
                }
                specialized.raw = raw
            }
            return@getOrPut specialized
        }
    }

    data class MirrorSignature(
        val method: AbstractMethod,
        val returnType: TypeMirror,
        val paramTypes: List<TypeMirror>
//        val exceptions: List<TypeMirror>
    )
}
