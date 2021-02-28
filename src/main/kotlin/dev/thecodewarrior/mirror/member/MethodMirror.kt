package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.impl.member.ExecutableSpecialization
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.MethodHandleHelper
import dev.thecodewarrior.mirror.impl.utils.Untested
import dev.thecodewarrior.mirror.impl.utils.unmodifiableView
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod

/**
 * A mirror representing a method.
 */
public interface MethodMirror : ExecutableMirror {
    public override val java: Method
    public override val raw: MethodMirror

    /**
     * A shorthand for checking if the `abstract` [modifier][modifiers] is present on this field.
     */
    public val isAbstract: Boolean

    /**
     * A shorthand for checking if the `static` [modifier][modifiers] is present on this field.
     */
    public val isStatic: Boolean

    /**
     * A shorthand for checking if the `final` [modifier][modifiers] is present on this field.
     */
    public val isFinal: Boolean

    /**
     * A shorthand for checking if the `strictfp` [modifier][modifiers] is present on this field.
     */
    public val isStrict: Boolean

    /**
     * A shorthand for checking if the `synchronized` [modifier][modifiers] is present on this field.
     */
    public val isSynchronized: Boolean

    /**
     * A shorthand for checking if the `native` [modifier][modifiers] is present on this field.
     */
    public val isNative: Boolean

    /**
     * Returns true if this method is a [bridge method](https://docs.oracle.com/javase/tutorial/java/generics/bridgeMethods.html#bridgeMethods).
     *
     * @see Method.isBridge
     */
    public val isBridge: Boolean

    /**
     * Returns true if this method is a default interface method. Implementations of default interface methods don't
     * have this flag. For the default values of annotation parameters, use [defaultValue].
     *
     * @see Method.isDefault
     */
    public val isDefault: Boolean

    /**
     * Returns the default value of the annotation method, if it has one. Somewhat confusingly, this is entirely
     * distinct from [isDefault], despite the similar name.
     *
     * @see Method.getDefaultValue
     */
    public val defaultValue: Any?

    /**
     * Returns the method overridden by this method, if any. This will return the method this overrides from its
     * superclass, not from any interfaces.
     */
    @Untested
    public val overrides: MethodMirror?

    /**
     * Returns true if this method overrides the passed method. This performs all its calculations based on this
     * method's declaring class, so if this method is inherited by another class and overrides an interface declared on
     * on that other class, this will _not_ detect that.
     */
    public fun doesOverride(otherMethod: Method): Boolean

    /**
     * Calls the represented method on the passed instance. If this represents a static method, `null` should be used
     * for the instance.
     *
     * After the one-time cost of creating the [MethodHandle][java.lang.invoke.MethodHandle], the access should be
     * near-native speed.
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(Throwable::class)
    public fun <T> call(receiver: Any?, vararg args: Any?): T

    override fun withTypeParameters(vararg parameters: TypeMirror): MethodMirror

    override fun withDeclaringClass(enclosing: ClassMirror?): MethodMirror
}