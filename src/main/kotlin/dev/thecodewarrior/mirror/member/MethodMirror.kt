package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.utils.MethodHandleHelper
import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.unmodifiableView
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod

class MethodMirror internal constructor(
    cache: MirrorCache,
    override val java: Method,
    raw: MethodMirror?,
    specialization: ExecutableSpecialization?
): ExecutableMirror(cache, specialization) {

    override val raw: MethodMirror = raw ?: this
    override val annotatedElement: AnnotatedElement = java
    override val name: String = java.name
    // Removing VOLATILE due to this interaction: https://bugs.openjdk.java.net/browse/JDK-5070593
    override val modifiers: Set<Modifier> = (Modifier.fromModifiers(java.modifiers) - setOf(Modifier.VOLATILE)).unmodifiableView()
    override val access: Modifier.Access = Modifier.Access.fromModifiers(java.modifiers)
    override val isVarArgs: Boolean = java.isVarArgs
    override val isSynthetic: Boolean = java.isSynthetic
    override val isInternalAccess: Boolean get() = kCallable?.visibility == KVisibility.INTERNAL

    override val kCallable: KFunction<*>? by lazy {
        declaringClass.kClass.functions.find { it.javaMethod == java }
    }

    /**
     * A shorthand for checking if the `public` [modifier][modifiers] is present on this field.
     */
    val isPublic: Boolean = Modifier.PUBLIC in modifiers

    /**
     * A shorthand for checking if the `protected` [modifier][modifiers] is present on this field.
     */
    val isProtected: Boolean = Modifier.PROTECTED in modifiers

    /**
     * A shorthand for checking if the `private` [modifier][modifiers] is present on this field.
     */
    val isPrivate: Boolean = Modifier.PRIVATE in modifiers

    /**
     * A shorthand for checking if neither the `public`, `protected`, nor `private` [modifiers][modifiers] are present
     * on this field.
     */
    val isPackagePrivate: Boolean = !isPublic && !isProtected && !isPrivate

    /**
     * A shorthand for checking if the `abstract` [modifier][modifiers] is present on this field.
     */
    val isAbstract: Boolean = Modifier.ABSTRACT in modifiers

    /**
     * A shorthand for checking if the `static` [modifier][modifiers] is present on this field.
     */
    val isStatic: Boolean = Modifier.STATIC in modifiers

    /**
     * A shorthand for checking if the `final` [modifier][modifiers] is present on this field.
     */
    val isFinal: Boolean = Modifier.FINAL in modifiers

    /**
     * A shorthand for checking if the `strictfp` [modifier][modifiers] is present on this field.
     */
    val isStrict: Boolean = Modifier.STRICT in modifiers

    /**
     * A shorthand for checking if the `synchronized` [modifier][modifiers] is present on this field.
     */
    val isSynchronized: Boolean = Modifier.SYNCHRONIZED in modifiers

    /**
     * A shorthand for checking if the `native` [modifier][modifiers] is present on this field.
     */
    val isNative: Boolean = Modifier.NATIVE in modifiers

    /**
     * Returns true if this method is a [bridge method](https://docs.oracle.com/javase/tutorial/java/generics/bridgeMethods.html#bridgeMethods).
     *
     * @see Method.isBridge
     */
    val isBridge: Boolean = java.isBridge
    /**
     * Returns true if this method is a default interface method. Implementations of default interface methods don't
     * have this flag. For the default values of annotation parameters, use [defaultValue].
     *
     * @see Method.isDefault
     */
    val isDefault: Boolean = java.isDefault
    /**
     * Returns the default value of the annotation method, if it has one. Somewhat confusingly, this is entirely
     * separate from [isDefault]
     *
     * @see Method.getDefaultValue
     */
    val defaultValue: Any? = java.defaultValue

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

    /**
     * Returns the method overridden by this method, if any. This will return the method this overrides from its
     * superclass, not from any interfaces.
     */
    @Untested
    val overrides: MethodMirror? by lazy {
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
     * on that other class, this will _not_ detect that.
     */
    fun doesOverride(otherMethod: Method): Boolean {
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

    /**
     * Call this method on the passed instance. If this is a static method, `null` should be used for the instance. If
     * performance is of the essence use [callFast], which should be near-native speed, but will provide somewhat less
     * helpful exceptions.
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(Throwable::class)
    fun <T> call(receiver: Any?, vararg args: Any?): T {
        if(isStatic) {
            if(receiver != null)
                throw IllegalArgumentException("Invalid receiver for static method `${declaringClass.java.simpleName}.$name`. Expected null.")
            if(args.size != parameters.size)
                throw IllegalArgumentException("Incorrect argument count (${args.size}) for static method `$this`")

            return raw.staticWrapper(args as Array<Any?>) as T
        } else {
            if(receiver == null)
                throw NullPointerException("Null receiver for instance method `${declaringClass.java.simpleName}.$name`")
            if(!declaringClass.java.isAssignableFrom(receiver.javaClass))
                throw IllegalArgumentException("Invalid receiver type `${receiver.javaClass.simpleName}` for instance method `${declaringClass.java.simpleName}.$name`")
            if(args.size != parameters.size)
                throw IllegalArgumentException("Incorrect argument count (${args.size}) for instance method `$this`")

            return raw.instanceWrapper(receiver, args as Array<Any?>) as T
        }
    }

    @JvmSynthetic
    operator fun <T> invoke(receiver: Any?, vararg args: Any?): T = call(receiver, *args)

    /**
     * Call this method on the passed instance. If this is a static method, `null` should be used for the instance.
     * After the one-time cost of creating the [MethodHandle][java.lang.invoke.MethodHandle], the access should be
     * near-native speed. This method, while faster than [call], will provide somewhat less helpful exceptions.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any?> callFast(receiver: Any?, vararg args: Any?): T {
        if(isStatic) {
            return raw.staticWrapper(args as Array<Any?>) as T
        } else {
            return raw.instanceWrapper(receiver!!, args as Array<Any?>) as T
        }
    }

    override fun toString(): String {
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
}