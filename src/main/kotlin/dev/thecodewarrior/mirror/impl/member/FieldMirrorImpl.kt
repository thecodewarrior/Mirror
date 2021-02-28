package dev.thecodewarrior.mirror.impl.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.impl.type.ClassMirrorImpl
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.MethodHandleHelper
import dev.thecodewarrior.mirror.impl.utils.unmodifiableView
import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.Modifier
import java.lang.reflect.Field

internal class FieldMirrorImpl internal constructor(
    cache: MirrorCache,
    raw: FieldMirrorImpl?,
    override val java: Field,
    _enclosing: ClassMirror?
): MemberMirrorImpl(cache, java, _enclosing), FieldMirror {

    override val raw: FieldMirrorImpl = raw ?: this
    override val name: String = java.name

    override val modifiers: Set<Modifier> = Modifier.fromModifiers(java.modifiers).unmodifiableView()
    override val access: Modifier.Access = Modifier.Access.fromModifiers(java.modifiers)
    override val isPublic: Boolean = Modifier.PUBLIC in modifiers
    override val isProtected: Boolean = Modifier.PROTECTED in modifiers
    override val isPrivate: Boolean = Modifier.PRIVATE in modifiers
    override val isPackagePrivate: Boolean = !isPublic && !isProtected && !isPrivate
    override val isStatic: Boolean = Modifier.STATIC in modifiers
    override val isFinal: Boolean = Modifier.FINAL in modifiers
    override val isTransient: Boolean = Modifier.TRANSIENT in modifiers
    override val isVolatile: Boolean = Modifier.VOLATILE in modifiers
    override val isSynthetic: Boolean = java.isSynthetic
    override val isEnumConstant: Boolean = java.isEnumConstant

    override val type: TypeMirror by lazy {
        (declaringClass as ClassMirrorImpl).genericMapping[java.annotatedType.let { cache.types.reflect(it) }]
    }

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

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> get(receiver: Any?): T {
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

    @Suppress("UNCHECKED_CAST")
    override fun set(receiver: Any?, value: Any?) {
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
