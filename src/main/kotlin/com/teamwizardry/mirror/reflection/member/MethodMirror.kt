package com.teamwizardry.librarianlib.commons.reflection.mirror.member

/*
class MethodMirror internal constructor(internal val cache: MirrorCache, val method: Method) {

    private var handle: (Any, Array<out Any?>) -> Any? by lazyDefault {
        val handle = MethodHandleHelper.wrapperForMethod<Any>(method)
        return@lazyDefault { instance: Any, parameters: Array<out Any?> ->
            method.isAccessible = true
            handle(instance, parameters)
        }
    }

    var typeParameters: List<TypeMirror> by lazyDefault {
        method.typeParameters.map { cache.reflect(it) }
    }
        private set

    var parameters: List<ParameterMirror> by lazyDefault {
        method.parameters.map { ParameterMirror(cache, it) }
    }
        private set

    var returnType: TypeMirror? by lazyDefault {
        if(method.returnType == Void.TYPE) null else cache.reflect(method.genericReturnType)
    }
        private set

    var checkedExceptions: List<TypeMirror> by lazyDefault {
        method.genericExceptionTypes.map { cache.reflect(it) }
    }
        private set

    var accessLevel: AccessLevel by lazyDefault {
        val modifiers = method.modifiers
        when {
            Modifier.isPrivate(modifiers) -> AccessLevel.PRIVATE
            Modifier.isProtected(modifiers) -> AccessLevel.PROTECTED
            Modifier.isPublic(modifiers) -> AccessLevel.PUBLIC
            else -> AccessLevel.PACKAGE
        }
    }

    var isFinal: Boolean by lazyDefault {
        Modifier.isFinal(method.modifiers)
    }
        private set

    var isAbstract: Boolean by lazyDefault {
        Modifier.isAbstract(method.modifiers)
    }
        private set

    fun call(instance: Any, vararg arguments: Any?): Any? {
        method.annotations
        method.name
        method.parameters
        method.parameterAnnotations
        return handle(instance, arguments)
    }
}

class ParameterMirror internal constructor(internal val cache: MirrorCache, val parameter: Parameter) {

    init {
        parameter.parameterizedType
        parameter.isNamePresent
        parameter.name
        // index
        parameter.modifiers // isFinal
    }
}

class StaticMethodMirror internal constructor(internal val cache: MirrorCache, val companion: Any?, val method: Method) {

//    fun call(vararg arguments: Any?): Any? {
//
//    }
}
*/
