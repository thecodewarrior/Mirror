package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.utils.MethodHandleHelper
import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.unmodifiableView
import java.lang.reflect.Constructor
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

class ConstructorMirror internal constructor(
    cache: MirrorCache,
    override val java: Constructor<*>,
    raw: ConstructorMirror?,
    specialization: ExecutableSpecialization?
): ExecutableMirror(cache, specialization) {

    override val raw: ConstructorMirror = raw ?: this
    override val name: String = java.name
    override val modifiers: Set<Modifier> = Modifier.fromModifiers(java.modifiers).unmodifiableView()
    override val access: Modifier.Access = Modifier.Access.fromModifiers(java.modifiers)
    override val isVarArgs: Boolean = java.isVarArgs
    override val isSynthetic: Boolean = java.isSynthetic
    override val isInternalAccess: Boolean get() = kCallable?.visibility == KVisibility.INTERNAL

    override val kCallable: KFunction<*>? by lazy {
        declaringClass.kClass.constructors.find { it.javaConstructor == java }
    }

    override fun withTypeParameters(vararg parameters: TypeMirror): ConstructorMirror {
        return super.withTypeParameters(*parameters) as ConstructorMirror
    }

    override fun withDeclaringClass(type: ClassMirror?): ConstructorMirror {
        return super.withDeclaringClass(type) as ConstructorMirror
    }

    private val wrapper by lazy {
        java.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        MethodHandleHelper.wrapperForConstructor(java as Constructor<Any>)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any?> call(vararg args: Any?): T {
        if(args.size != parameters.size)
            throw IllegalArgumentException("Incorrect argument count (${args.size}) for constructor `$this`")
        return raw.wrapper(args as Array<Any?>) as T
    }

    @JvmSynthetic
    operator fun <T> invoke(vararg args: Any?): T = call(*args)

    override fun toString(): String {
        var str = ""
        if(access != Modifier.Access.DEFAULT) {
            str += "$access ".toLowerCase()
        }
        if(specialization == null) {
            if (typeParameters.isNotEmpty()) {
                str += "<${typeParameters.joinToString(", ")}> "
            }
            str += name
        } else {
            str += name
            if (typeParameters.isNotEmpty()) {
                str += "<${typeParameters.joinToString(", ")}>"
            }
        }
        str += "(${parameters.joinToString(", ")})"
        return str
    }
}