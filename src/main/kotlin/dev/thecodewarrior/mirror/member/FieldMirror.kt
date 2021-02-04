package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.utils.MethodHandleHelper
import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.UntestedNegative
import dev.thecodewarrior.mirror.utils.unmodifiableView
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field

public class FieldMirror internal constructor(
    cache: MirrorCache,
    raw: FieldMirror?,
    override val java: Field,
    _enclosing: ClassMirror?
): MemberMirror(cache, _enclosing) {

    /**
     * The raw, unspecialized mirror of this field.
     */
    public var raw: FieldMirror = raw ?: this
    override val annotatedElement: AnnotatedElement = java

    /**
     * The field's name
     */
    public val name: String = java.name

    /**
     * The set of modifiers present on this field. The valid modifiers for fields are `public`, `protected`, `private`,
     * `static`, `final`, `transient`, and `volatile`.
     *
     * **Note: this value is immutable**
     */
    public val modifiers: Set<Modifier> = Modifier.fromModifiers(java.modifiers).unmodifiableView()

    /**
     * The access level of this field.
     */
    public val access: Modifier.Access = Modifier.Access.fromModifiers(java.modifiers)

    /**
     * A shorthand for checking if the `public` [modifier][modifiers] is present on this field.
     */
    public val isPublic: Boolean = Modifier.PUBLIC in modifiers

    /**
     * A shorthand for checking if the `protected` [modifier][modifiers] is present on this field.
     */
    public val isProtected: Boolean = Modifier.PROTECTED in modifiers

    /**
     * A shorthand for checking if the `private` [modifier][modifiers] is present on this field.
     */
    public val isPrivate: Boolean = Modifier.PRIVATE in modifiers

    /**
     * A shorthand for checking if neither the `public`, `protected`, nor `private` [modifiers][modifiers] are present
     * on this field.
     */
    public val isPackagePrivate: Boolean = !isPublic && !isProtected && !isPrivate

    /**
     * A shorthand for checking if the `static` [modifier][modifiers] is present on this field.
     */
    public val isStatic: Boolean = Modifier.STATIC in modifiers

    /**
     * A shorthand for checking if the `final` [modifier][modifiers] is present on this field.
     */
    public val isFinal: Boolean = Modifier.FINAL in modifiers

    /**
     * A shorthand for checking if the `transient` [modifier][modifiers] is present on this field.
     */
    public val isTransient: Boolean = Modifier.TRANSIENT in modifiers

    /**
     * A shorthand for checking if the `volatile` [modifier][modifiers] is present on this field.
     */
    public val isVolatile: Boolean = Modifier.VOLATILE in modifiers

    /**
     * Returns true if this field is synthetic.
     */
    public val isSynthetic: Boolean = java.isSynthetic

    /**
     * True if this field holds an enum constant
     */
    public val isEnumConstant: Boolean = java.isEnumConstant

    /**
     * The field type, specialized based on the declaring class's specialization.
     */
    public val type: TypeMirror by lazy {
        declaringClass.genericMapping[java.annotatedType.let { cache.types.reflect(it) }]
    }

    /**
     * Returns annotations that are present on the field this mirror represents.
     *
     * **Note: this value is immutable**
     *
     * @see Field.getAnnotations
     */
    public val annotations: List<Annotation> = java.annotations.toList().unmodifiableView()

    override fun withDeclaringClass(enclosing: ClassMirror?): FieldMirror {
        if(enclosing != null && enclosing.java != java.declaringClass)
            throw InvalidSpecializationException("Invalid declaring class $type. " +
                "$this is declared in ${java.declaringClass}")
        return if(enclosing == null || enclosing == raw.declaringClass) raw else cache.fields.specialize(this, enclosing)
    }

    private val instanceGetWrapper by lazy {
        java.isAccessible = true
        MethodHandleHelper.wrapperForGetter(java)
    }
    private val staticGetWrapper by lazy {
        java.isAccessible = true
        MethodHandleHelper.wrapperForStaticGetter(java)
    }

    /**
     * Get the value of this field in the passed instance. If this is a static field, `null` should be used for the
     * instance. After the one-time cost of creating the [MethodHandle][java.lang.invoke.MethodHandle], the access should
     * be near-native speed.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any?> get(receiver: Any?): T {
        if(isStatic) {
            return raw.staticGetWrapper() as T
        } else {
            return raw.instanceGetWrapper(receiver!!) as T
        }
    }

    private val instanceSetWrapper by lazy {
        java.isAccessible = true
        MethodHandleHelper.wrapperForSetter(java)
    }
    private val staticSetWrapper by lazy {
        java.isAccessible = true
        MethodHandleHelper.wrapperForStaticSetter(java)
    }

    /**
     * Set the value of this field in the passed instance. If this is a static field, `null` should be used for the
     * instance. After the one-time cost of creating the [MethodHandle][java.lang.invoke.MethodHandle], the access
     * should be near-native speed.
     */
    @Suppress("UNCHECKED_CAST")
    public fun set(receiver: Any?, value: Any?) {
        if(isStatic) {
            raw.staticSetWrapper(value)
        } else {
            raw.instanceSetWrapper(receiver!!, value)
        }
    }

    override fun toString(): String {
        var str = ""
        str += "${modifiers.joinToString("") { "$it " }}$type ${declaringClass.name}.$name"
        return str
    }
}