package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.MethodHandleHelper
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class FieldMirror internal constructor(
    internal val cache: MirrorCache,
    raw: FieldMirror?,
    val java: Field,
    _enclosing: ClassMirror?
) {

    var raw: FieldMirror = raw ?: this
    val isEnumConstant: Boolean = java.isEnumConstant

    val name: String = java.name
    val isStatic: Boolean = Modifier.isStatic(java.modifiers)
    val isTransient: Boolean = Modifier.isTransient(java.modifiers)
    val isVolatile: Boolean = Modifier.isVolatile(java.modifiers)
    val accessLevel: AccessLevel = AccessLevel.fromModifiers(java.modifiers)

    val enclosingClass: ClassMirror by lazy {
        _enclosing ?: cache.types.reflect(java.declaringClass) as ClassMirror
    }

    val type: TypeMirror by lazy {
        enclosingClass.genericMapping[java.annotatedType.let { cache.types.reflect(it) }]
    }

    fun specialize(enclosing: ClassMirror): FieldMirror {
        if(enclosing.java != java.declaringClass)
            throw InvalidSpecializationException("Invalid enclosing class $type. " +
                "$this is declared in ${java.declaringClass}")
        return cache.fields.specialize(this, enclosing)
    }

    private val instanceGetWrapper by lazy {
        MethodHandleHelper.wrapperForGetter(java)
    }
    private val staticGetWrapper by lazy {
        MethodHandleHelper.wrapperForStaticGetter(java)
    }

    //TODO test
    @Suppress("UNCHECKED_CAST")
    fun <T : Any?> get(receiver: Any): T {
        if(Modifier.isStatic(java.modifiers))
            return raw.staticGetWrapper() as T
        else
            return raw.instanceGetWrapper(receiver) as T
    }

    private val instanceSetWrapper by lazy {
        MethodHandleHelper.wrapperForSetter(java)
    }
    private val staticSetWrapper by lazy {
        MethodHandleHelper.wrapperForStaticSetter(java)
    }

    //TODO test
    @Suppress("UNCHECKED_CAST")
    fun <T : Any?> set(receiver: Any, value: Any?): T {
        if(Modifier.isStatic(java.modifiers))
            return raw.staticSetWrapper(value) as T
        else
            return raw.instanceSetWrapper(receiver, value) as T
    }

    override fun toString(): String {
        var str = ""
        str += "$type $name"
        return str
    }
}