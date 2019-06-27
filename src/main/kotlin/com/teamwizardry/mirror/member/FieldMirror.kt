package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.MethodHandleHelper
import com.teamwizardry.mirror.utils.unmodifiableView
import java.lang.reflect.Field

class FieldMirror internal constructor(
    internal val cache: MirrorCache,
    raw: FieldMirror?,
    val java: Field,
    _enclosing: ClassMirror?
) {

    var raw: FieldMirror = raw ?: this
    val isEnumConstant: Boolean = java.isEnumConstant

    val name: String = java.name

    // * **Note: this value is immutable**
    val modifiers: Set<Modifier> = Modifier.fromModifiers(java.modifiers).unmodifiableView()
    val access: Modifier.Access = Modifier.Access.fromModifiers(java.modifiers)
    val isPublic: Boolean = Modifier.PUBLIC in modifiers
    val isProtected: Boolean = Modifier.PROTECTED in modifiers
    val isPrivate: Boolean = Modifier.PRIVATE in modifiers
    val isStatic: Boolean = Modifier.STATIC in modifiers
    val isFinal: Boolean = Modifier.FINAL in modifiers
    val isTransient: Boolean = Modifier.TRANSIENT in modifiers
    val isVolatile: Boolean = Modifier.VOLATILE in modifiers

    val declaringClass: ClassMirror by lazy {
        _enclosing ?: cache.types.reflect(java.declaringClass) as ClassMirror
    }

    val type: TypeMirror by lazy {
        declaringClass.genericMapping[java.annotatedType.let { cache.types.reflect(it) }]
    }

    fun withDeclaringClass(enclosing: ClassMirror): FieldMirror {
        if(enclosing.java != java.declaringClass)
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

    override fun toString(): String {
        var str = ""
        str += "$type $name"
        return str
    }
}