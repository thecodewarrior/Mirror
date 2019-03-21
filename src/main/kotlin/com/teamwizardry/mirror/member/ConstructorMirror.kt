package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.MethodHandleHelper
import java.lang.reflect.Constructor

//TODO tests
class ConstructorMirror internal constructor(
    cache: MirrorCache,
    override val java: Constructor<*>,
    raw: ConstructorMirror?,
    specialization: ExecutableSpecialization?
): ExecutableMirror(cache, specialization) {

    override val raw: ConstructorMirror = raw ?: this
    override val name: String = java.name
    val description: String get() = "${declaringClass.java.simpleName}(${raw.parameterTypes.joinToString(", ")})"
    val accessLevel: AccessLevel = AccessLevel.fromModifiers(java.modifiers)

    override fun specialize(vararg parameters: TypeMirror): ConstructorMirror {
        return super.specialize(*parameters) as ConstructorMirror
    }

    override fun enclose(type: ClassMirror): ConstructorMirror {
        return super.enclose(type) as ConstructorMirror
    }

    private val wrapper by lazy {
        @Suppress("UNCHECKED_CAST")
        MethodHandleHelper.wrapperForConstructor(java as Constructor<Any>)
    }

    //TODO test
    @Suppress("UNCHECKED_CAST")
    fun <T : Any?> call(vararg args: Any?): T {
        if(args.size != parameters.size)
            throw IllegalArgumentException("Incorrect argument count (${args.size}) for constructor `$description`")
        return raw.wrapper(args as Array<Any?>) as T
    }

    @JvmSynthetic
    operator fun <T> invoke(vararg args: Any?): T = call(*args)

    override fun toString(): String {
        var str = name
        if(typeParameters.isNotEmpty()) {
            str += "<${typeParameters.joinToString(", ")}>"
        }
        str += "(${parameters.joinToString(", ")})"
        return str
    }
}