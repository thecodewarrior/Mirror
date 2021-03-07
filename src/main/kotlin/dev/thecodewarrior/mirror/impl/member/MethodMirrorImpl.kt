package dev.thecodewarrior.mirror.impl.member

import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.impl.member.ExecutableSpecialization
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.MethodHandleHelper
import dev.thecodewarrior.mirror.impl.utils.Untested
import dev.thecodewarrior.mirror.impl.utils.unmodifiableView
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.member.Modifier
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod

internal class MethodMirrorImpl internal constructor(
    cache: MirrorCache,
    override val java: Method,
    raw: MethodMirrorImpl?,
    specialization: ExecutableSpecialization?
): ExecutableMirrorImpl(cache, java, specialization), MethodMirror {

    override val raw: MethodMirrorImpl = raw ?: this
    override val name: String = java.name
    override val modifiers: Set<Modifier> = Modifier.fromMethodModifiers(java.modifiers).unmodifiableView()
    override val access: Modifier.Access = Modifier.Access.fromModifiers(java.modifiers)
    override val isVarArgs: Boolean = java.isVarArgs
    override val isSynthetic: Boolean = java.isSynthetic
    override val isInternalAccess: Boolean get() = kCallable?.visibility == KVisibility.INTERNAL

    override val kCallable: KFunction<*>? by lazy {
        declaringClass.kClass.functions.find { it.javaMethod == java }
    }

    override val isPublic: Boolean = Modifier.PUBLIC in modifiers
    override val isProtected: Boolean = Modifier.PROTECTED in modifiers
    override val isPrivate: Boolean = Modifier.PRIVATE in modifiers
    override val isPackagePrivate: Boolean = !isPublic && !isProtected && !isPrivate

    override val isAbstract: Boolean = Modifier.ABSTRACT in modifiers
    override val isStatic: Boolean = Modifier.STATIC in modifiers
    override val isFinal: Boolean = Modifier.FINAL in modifiers
    override val isStrict: Boolean = Modifier.STRICT in modifiers
    override val isSynchronized: Boolean = Modifier.SYNCHRONIZED in modifiers
    override val isNative: Boolean = Modifier.NATIVE in modifiers
    override val isBridge: Boolean = java.isBridge
    override val isDefault: Boolean = java.isDefault
    override val defaultValue: Any? = java.defaultValue

    override fun withTypeParameters(vararg parameters: TypeMirror): MethodMirror {
        return super.withTypeParameters(*parameters) as MethodMirror
    }

    override fun withDeclaringClass(enclosing: ClassMirror?): MethodMirror {
        return super.withDeclaringClass(enclosing) as MethodMirror
    }

    private val instanceWrapper by lazy {
        java.isAccessible = true
        MethodHandleHelper.wrapperForMethod(java)
    }
    private val staticWrapper by lazy {
        java.isAccessible = true
        MethodHandleHelper.wrapperForStaticMethod(java)
    }

    @Untested
    override val overrides: MethodMirror? by lazy {
        if(this != this.raw)
            return@lazy this.raw.overrides?.let { declaringClass.getMethod(it.java) }

        generateSequence(declaringClass.superclass) { it.superclass }.forEach { cls ->
            cls.declaredMethods.find { base ->
                base.name == this.name &&
                        !(base.isPrivate || base.isPackagePrivate &&
                                base.declaringClass.java.`package` != this.declaringClass.java.`package`) &&
                        base.declaringClass.isAssignableFrom(this.declaringClass) &&
                        base.erasedParameterTypes == this.erasedParameterTypes
            }?.also { return@lazy it }
        }

        return@lazy null
    }

    /**
     * Returns true if this method overrides the passed method. This performs all its calculations based on this
     * method's declaring class, so if this method is inherited by another class and overrides an interface declared on
     * on that class, this will _not_ detect that.
     */
    override fun doesOverride(otherMethod: Method): Boolean {
        if(this != this.raw)
            return this.raw.doesOverride(otherMethod)
        if(!otherMethod.declaringClass.isAssignableFrom(declaringClass.java))
            return false
        // interfaces are assignable to Object, which leads to issues where the interfaces think
        // they can override methods from Object
        if(!otherMethod.declaringClass.isInterface && this.declaringClass.isInterface)
            return false
        val other = declaringClass.getMethod(otherMethod)
        if(other == this || other.name != this.name ||
            other.erasedParameterTypes != this.erasedParameterTypes) {
            return false
        }

        // Interfaces are _always_ public, so there will never be any access trouble here. If other is assignable from
        // this, we're either an implementing class or a superinterface, in either case we override.
        if(other.declaringClass.isInterface)
            return true
        return generateSequence(this.overrides) { it.overrides }.any { it == other }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(Throwable::class)
    override fun <T> call(receiver: Any?, vararg args: Any?): T {
        if(isStatic) {
            return raw.staticWrapper(args as Array<Any?>) as T
        } else {
            return raw.instanceWrapper(receiver!!, args as Array<Any?>) as T
        }
    }

    @Untested
    override fun toString(): String {
        return ""
    }

    @Untested
    override fun toJavaDeclarationString(): String {
        var str = ""
        str += modifiers.joinToString("") { "$it " }
        if(specialization?.arguments != null) {
            str += "$returnType ${declaringClass.name}.$name"
            if (typeParameters.isNotEmpty()) {
                str += "<${typeParameters.joinToString(", ")}>"
            }
        } else {
            if (typeParameters.isNotEmpty()) {
                str += "<${typeParameters.joinToString(", ")}> "
            }
            str += "$returnType ${declaringClass.name}.$name"
        }
        str += "(${parameters.joinToString(", ")})"
        return str
    }

    @Untested
    override fun toKotlinDeclarationString(): String {
        TODO("Not yet implemented")
    }
}
