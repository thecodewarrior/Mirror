package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import java.lang.reflect.Field

/**
 * A mirror representing a field.
 */
public interface FieldMirror : MemberMirror {
    override val java: Field

    /**
     * The raw, unspecialized mirror of this field.
     */
    override val raw: FieldMirror

    /**
     * The field's name
     */
    public val name: String

    /**
     * A shorthand for checking if the `static` [modifier][modifiers] is present on this field.
     */
    public val isStatic: Boolean

    /**
     * A shorthand for checking if the `final` [modifier][modifiers] is present on this field.
     */
    public val isFinal: Boolean

    /**
     * A shorthand for checking if the `transient` [modifier][modifiers] is present on this field.
     */
    public val isTransient: Boolean

    /**
     * A shorthand for checking if the `volatile` [modifier][modifiers] is present on this field.
     */
    public val isVolatile: Boolean

    /**
     * True if this field holds an enum constant
     */
    public val isEnumConstant: Boolean

    /**
     * The field type, specialized based on the declaring class's specialization.
     */
    public val type: TypeMirror

    /**
     * Get the value of this field in the passed instance. If this is a static field, `null` should be used for the
     * instance. After the one-time cost of creating the [MethodHandle][java.lang.invoke.MethodHandle], the access should
     * be near-native speed.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any?> get(receiver: Any?): T

    /**
     * Set the value of this field in the passed instance. If this is a static field, `null` should be used for the
     * instance. After the one-time cost of creating the [MethodHandle][java.lang.invoke.MethodHandle], the access
     * should be near-native speed.
     */
    @Suppress("UNCHECKED_CAST")
    public fun set(receiver: Any?, value: Any?)

    override fun withDeclaringClass(enclosing: ClassMirror?): FieldMirror
}
