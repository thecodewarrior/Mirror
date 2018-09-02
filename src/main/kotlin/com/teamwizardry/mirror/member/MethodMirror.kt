package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.method.AbstractMethod
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.lazyOrSet

class MethodMirror internal constructor(val cache: MirrorCache, val abstractMethod: AbstractMethod) {

    var raw: MethodMirror = this
        internal set

    val name: String = abstractMethod.name

    var returnType: TypeMirror by lazyOrSet {
        cache.types.reflect(abstractMethod.returnType)
    }
        internal set

    var typeParameters: List<TypeMirror> by lazyOrSet {
        abstractMethod.typeParameters.map { cache.types.reflect(it) }
    }
        internal set

    //val parameters: List<ParameterMirror>
    val declaringClass: ClassMirror by lazy {
        cache.types.reflect(abstractMethod.delcaringClass) as ClassMirror
    }

    override fun toString(): String {
        var str = ""
        str += "$returnType $name()"
        return str
    }
}