package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.MethodHandleHelper
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class MethodMirror internal constructor(
    cache: MirrorCache,
    override val java: Method,
    raw: MethodMirror?,
    specialization: ExecutableSpecialization?
): ExecutableMirror(cache, specialization) {

    override val raw: MethodMirror = raw ?: this
    override val name: String = java.name
    val isStatic: Boolean = Modifier.isStatic(java.modifiers)
    val accessLevel: AccessLevel = AccessLevel.fromModifiers(java.modifiers)

    override fun specialize(vararg parameters: TypeMirror): MethodMirror {
        return super.specialize(*parameters) as MethodMirror
    }

    override fun enclose(type: ClassMirror): MethodMirror {
        return super.enclose(type) as MethodMirror
    }

    private val instanceWrapper by lazy {
        MethodHandleHelper.wrapperForMethod(java)
    }
    private val staticWrapper by lazy {
        MethodHandleHelper.wrapperForStaticMethod(java)
    }

    //TODO test
    @Suppress("UNCHECKED_CAST")
    fun <T : Any?> call(receiver: Any, vararg args: Any?): T {
        if(Modifier.isStatic(java.modifiers))
            return raw.staticWrapper(args as Array<Any?>) as T
        else
            return raw.instanceWrapper(receiver, args as Array<Any?>) as T
    }

    override fun toString(): String {
        var str = "$returnType $name"
        if(typeParameters.isNotEmpty()) {
            str += "<${typeParameters.joinToString(", ")}>"
        }
        str += "(${parameters.joinToString(", ")})"
        return str
    }
}