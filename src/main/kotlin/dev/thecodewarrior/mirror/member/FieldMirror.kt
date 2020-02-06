package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.utils.MethodHandleHelper
import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.UntestedNegative
import dev.thecodewarrior.mirror.utils.unmodifiableView
import java.lang.reflect.Field

class FieldMirror internal constructor(
    cache: MirrorCache,
    raw: FieldMirror?,
    override val java: Field,
    _enclosing: ClassMirror?
): MemberMirror(cache, _enclosing) {

    var raw: FieldMirror = raw ?: this
    @UntestedNegative
    val isEnumConstant: Boolean = java.isEnumConstant

    val name: String = java.name

    // * **Note: this value is immutable**
    @Untested
    val modifiers: Set<Modifier> = Modifier.fromModifiers(java.modifiers).unmodifiableView()
    val access: Modifier.Access = Modifier.Access.fromModifiers(java.modifiers)
    @Untested
    val isPublic: Boolean = Modifier.PUBLIC in modifiers
    @Untested
    val isProtected: Boolean = Modifier.PROTECTED in modifiers
    @Untested
    val isPrivate: Boolean = Modifier.PRIVATE in modifiers
    @UntestedNegative
    val isStatic: Boolean = Modifier.STATIC in modifiers
    @Untested
    val isFinal: Boolean = Modifier.FINAL in modifiers
    @UntestedNegative
    val isTransient: Boolean = Modifier.TRANSIENT in modifiers
    @UntestedNegative
    val isVolatile: Boolean = Modifier.VOLATILE in modifiers

    val type: TypeMirror by lazy {
        declaringClass.genericMapping[java.annotatedType.let { cache.types.reflect(it) }]
    }

    /**
     * Returns annotations that are present on the field this mirror represents.
     *
     * **Note: this value is immutable**
     *
     * @see Field.getAnnotations
     */
    @UntestedNegative
    val annotations: List<Annotation> = java.annotations.toList().unmodifiableView()

    override fun withDeclaringClass(enclosing: ClassMirror?): FieldMirror {
        if(enclosing != null && enclosing.java != java.declaringClass)
            throw InvalidSpecializationException("Invalid declaring class $type. " +
                "$this is declared in ${java.declaringClass}")
        return cache.fields.specialize(this, enclosing)
    }

    private val instanceGetWrapper by lazy {
        java.isAccessible = true
        MethodHandleHelper.wrapperForGetter(java)
    }
    private val staticGetWrapper by lazy {
        java.isAccessible = true
        MethodHandleHelper.wrapperForStaticGetter(java)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any?> get(receiver: Any?): T {
        if(isStatic) {
            if(receiver != null)
                throw IllegalArgumentException("Invalid receiver for static field `${declaringClass.java.simpleName}.$name`. Expected null.")
            return raw.staticGetWrapper() as T
        } else {
            if (receiver == null)
                throw NullPointerException("Null receiver for instance field `${declaringClass.java.simpleName}.$name`")
            if(!declaringClass.java.isAssignableFrom(receiver.javaClass))
                throw IllegalArgumentException("Invalid receiver type `${receiver.javaClass.simpleName}` for instance field `${declaringClass.java.simpleName}.$name`")
            return raw.instanceGetWrapper(receiver) as T
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

    @Suppress("UNCHECKED_CAST")
    fun set(receiver: Any?, value: Any?) {
        if(isStatic) {
            if(receiver != null)
                throw IllegalArgumentException("Invalid receiver for static field `${declaringClass.java.simpleName}.$name`. Expected null.")
            if(isFinal)
                throw IllegalStateException("Cannot set the value of final static field `${declaringClass.java.simpleName}.$name`")
            raw.staticSetWrapper(value)
        } else {
            if (receiver == null)
                throw NullPointerException("Null receiver for instance field `${declaringClass.java.simpleName}.$name`")
            if(!declaringClass.java.isAssignableFrom(receiver.javaClass))
                throw IllegalArgumentException("Invalid receiver type `${receiver.javaClass.simpleName}` for instance field `${declaringClass.java.simpleName}.$name`")
            if(isFinal)
                throw IllegalStateException("Cannot set the value of final instance field `${declaringClass.java.simpleName}.$name`")
            raw.instanceSetWrapper(receiver, value)
        }
    }

    @Untested
    override fun toString(): String {
        var str = ""
        str += "$type $name"
        return str
    }
}