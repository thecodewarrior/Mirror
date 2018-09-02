package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.method.AbstractMethod
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.lazyOrSet
import com.teamwizardry.mirror.utils.unmodifiable
import java.lang.reflect.Method

class MethodMirror internal constructor(internal val cache: MirrorCache, internal val abstractMethod: AbstractMethod) {
    val java: Method = abstractMethod.method

    var raw: MethodMirror = this
        internal set

    val name: String = abstractMethod.name

    var returnType: TypeMirror by lazyOrSet {
        cache.types.reflect(abstractMethod.returnType)
    }
        internal set

    var parameters: List<ParameterMirror> by lazyOrSet {
        abstractMethod.parameters.map {
            cache.parameters.reflect(it)
        }.unmodifiable()
    }
        internal set

    val parameterTypes: List<TypeMirror> by lazy {
        parameters.map { it.type }.unmodifiable()
    }

    override fun toString(): String {
        var str = ""
        str += "$returnType $name()"
        return str
    }
}