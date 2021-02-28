package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.MethodHandleHelper
import dev.thecodewarrior.mirror.impl.utils.unmodifiableView
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Constructor
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.javaConstructor

public class ConstructorMirror internal constructor(
    cache: MirrorCache,
    override val java: Constructor<*>,
    raw: ConstructorMirror?,
    specialization: ExecutableSpecialization?
): ExecutableMirror(cache, specialization) {

    override val raw: ConstructorMirror = raw ?: this
    override val annotatedElement: AnnotatedElement = java
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

    override fun withDeclaringClass(enclosing: ClassMirror?): ConstructorMirror {
        return super.withDeclaringClass(enclosing) as ConstructorMirror
    }

    private val wrapper by lazy {
        java.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        MethodHandleHelper.wrapperForConstructor(java as Constructor<Any>)
    }

    /**
     * Create a new instance using this constructor. After the one-time cost of creating the
     * [MethodHandle][java.lang.invoke.MethodHandle], the access should be near-native speed.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any?> call(vararg args: Any?): T {
        return raw.wrapper(args as Array<Any?>) as T
    }

    @JvmSynthetic
    public operator fun <T> invoke(vararg args: Any?): T = call(*args)

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