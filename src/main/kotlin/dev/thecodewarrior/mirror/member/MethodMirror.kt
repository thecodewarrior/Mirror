package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.utils.MethodHandleHelper
import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.unmodifiableView
import java.lang.reflect.Method

class MethodMirror internal constructor(
    cache: MirrorCache,
    override val java: Method,
    raw: MethodMirror?,
    specialization: ExecutableSpecialization?
): ExecutableMirror(cache, specialization) {

    override val raw: MethodMirror = raw ?: this
    override val name: String = java.name
    override val modifiers: Set<Modifier> = Modifier.fromModifiers(java.modifiers).unmodifiableView()
    override val access: Modifier.Access = Modifier.Access.fromModifiers(java.modifiers)
    override val isVarArgs: Boolean = java.isVarArgs

    @Untested
    val isAbstract: Boolean = Modifier.ABSTRACT in modifiers
    @Untested
    val isStatic: Boolean = Modifier.STATIC in modifiers
    @Untested
    val isFinal: Boolean = Modifier.FINAL in modifiers
    @Untested
    val isSynchronized: Boolean = Modifier.SYNCHRONIZED in modifiers
    @Untested
    val isNative: Boolean = Modifier.NATIVE in modifiers
    @Untested
    val isStrict: Boolean = Modifier.STRICT in modifiers

    override val isSynthetic: Boolean = java.isSynthetic

    /**
     * Returns true if this method is a [bridge method](https://docs.oracle.com/javase/tutorial/java/generics/bridgeMethods.html#bridgeMethods).
     *
     * @see Method.isBridge
     */
    val isBridge: Boolean = java.isBridge
    /**
     * Returns true if this method is a default interface method. Implementations of default interface methods don't
     * have this flag.
     *
     * @see Method.isDefault
     */
    val isDefault: Boolean = java.isDefault
    /**
     * Returns the default value of the annotation method, if it has one
     *
     * @see Method.getDefaultValue
     */
    val defaultValue: Any? = java.defaultValue

    override fun withTypeParameters(vararg parameters: TypeMirror): MethodMirror {
        return super.withTypeParameters(*parameters) as MethodMirror
    }

    override fun withDeclaringClass(type: ClassMirror?): MethodMirror {
        return super.withDeclaringClass(type) as MethodMirror
    }

    private val instanceWrapper by lazy {
        java.isAccessible = true
        MethodHandleHelper.wrapperForMethod(java)
    }
    private val staticWrapper by lazy {
        java.isAccessible = true
        MethodHandleHelper.wrapperForStaticMethod(java)
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(Throwable::class)
    @Untested("tests use `invoke` operator. They should avoid that level of indirection")
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

    @Untested
    override fun toString(): String {
        var str = "$returnType $name"
        if(typeParameters.isNotEmpty()) {
            str += "<${typeParameters.joinToString(", ")}>"
        }
        str += "(${parameters.joinToString(", ")})"
        return str
    }
}