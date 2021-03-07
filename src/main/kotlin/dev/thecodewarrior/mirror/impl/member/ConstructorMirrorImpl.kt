package dev.thecodewarrior.mirror.impl.member

import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.impl.member.ExecutableSpecialization
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.MethodHandleHelper
import dev.thecodewarrior.mirror.impl.utils.Untested
import dev.thecodewarrior.mirror.impl.utils.unmodifiableView
import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.member.Modifier
import java.lang.reflect.Constructor
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.javaConstructor

internal class ConstructorMirrorImpl internal constructor(
    cache: MirrorCache,
    override val java: Constructor<*>,
    raw: ConstructorMirrorImpl?,
    specialization: ExecutableSpecialization?
): ExecutableMirrorImpl(cache, java, specialization), ConstructorMirror {

    override val raw: ConstructorMirrorImpl = raw ?: this
    override val name: String = java.name
    override val modifiers: Set<Modifier> = Modifier.fromModifiers(java.modifiers).unmodifiableView()
    override val access: Modifier.Access = Modifier.Access.fromModifiers(java.modifiers)
    override val isVarArgs: Boolean = java.isVarArgs
    override val isSynthetic: Boolean = java.isSynthetic

    override val isPublic: Boolean = Modifier.PUBLIC in modifiers
    override val isProtected: Boolean = Modifier.PROTECTED in modifiers
    override val isPrivate: Boolean = Modifier.PRIVATE in modifiers
    override val isPackagePrivate: Boolean = !isPublic && !isProtected && !isPrivate
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
    override fun <T : Any?> call(vararg args: Any?): T {
        return raw.wrapper(args as Array<Any?>) as T
    }

    @Untested
    override fun toString(): String {
        return ""
    }

    @Untested
    override fun toJavaDeclarationString(): String {
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

    @Untested
    override fun toKotlinDeclarationString(): String {
        TODO("Not yet implemented")
    }
}
