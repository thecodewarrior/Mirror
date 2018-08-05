package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.method.AbstractMethod
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.lazyOrSet

class MethodMirror internal constructor(val cache: MirrorCache, val abstract: AbstractMethod) {
    var returnType: TypeMirror by lazyOrSet {
        cache.types.reflect(abstract.returnType)
    }
        internal set

    var typeParameters: List<TypeMirror> by lazyOrSet {
        abstract.typeParameters.map { cache.types.reflect(it) }
    }
        internal set

    //val parameters: List<ParameterMirror>
    val declaringClass: ClassMirror by lazy {
        cache.types.reflect(abstract.delcaringClass) as ClassMirror
    }

}