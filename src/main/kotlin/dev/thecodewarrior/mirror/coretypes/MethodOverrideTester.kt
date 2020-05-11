package dev.thecodewarrior.mirror.coretypes

import dev.thecodewarrior.mirror.utils.Untested
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier

// Based on Simon V. doing god's work here: https://stackoverflow.com/a/15302411/1541907
@Untested
internal object MethodOverrideTester {

    @Untested
    fun findOverridenMethod(it: Method) = it.findOverridenMethodIn(it.declaringClass)
    @Untested
    fun isOverridenBy(it: Method, other: Method) = it.isOverridenBy(other)

    private fun Method.isOverridableIn(cls: Class<*>): Boolean {
        if (isFinal || isPrivate || isStatic) return false
        if (!(isPublic || isProtected || isDefaultVisible)) return false
        if (!declaringClass.isAssignableFrom(cls)) return false

        if (isPublic) return true
        if (isDefaultVisible && cls.getPackage() == declaringClass.getPackage()) return true

        return false
    }

    private fun Method.findBridgeMethod(): Method? {
        if (isBridge) return null
        return declaringClass.declaredMethods.find {
            it != this &&
                isBridge && //todo should this be `it.isBridge`?
                it.name == name &&
                it.returnType.isAssignableFrom(returnType) &&
                it.parameterCount == parameterCount &&
                it.parameterTypes.zip(parameterTypes).all { (other, our) -> other.isAssignableFrom(our) }
        }
    }

    @Suppress("PlatformExtensionReceiverOfInline")
    @JvmName("isOverriddenByExt")
    private fun Method.isOverridenBy(other: Method): Boolean {
        if(this == other) return false
        val bridge = findBridgeMethod()

        if (bridge != null) return bridge.isOverridenBy(other)

        return name == other.name &&
            isOverridableIn(other.declaringClass) &&
            other.access <= access &&
            other.returnType == returnType &&
            other.parameterTypes.contentEquals(parameterTypes)
    }

    private fun Method.findOverridenMethodIn(cls: Class<*>): Method? {
        val superclasses = arrayListOf<Class<*>?>(cls.superclass)
        cls.interfaces.forEach { superclasses.add(it) }

        for (superclass in superclasses) {
            if (superclass == null) continue

            var overriden = superclass.declaredMethods.find { it.isOverridenBy(this) }
            if (overriden != null) return overriden

            overriden = findOverridenMethodIn(superclass)
            if (overriden != null) return overriden
        }

        return null
    }

    private inline val Member.isFinal: Boolean get() = Modifier.isFinal(modifiers)
    private inline val Member.isStatic: Boolean get() = Modifier.isStatic(modifiers)
    private inline val Member.isPrivate: Boolean get() = Modifier.isPrivate(modifiers)
    private inline val Member.isDefaultVisible: Boolean get() = !isPublic && !isProtected && !isPrivate
    private inline val Member.isProtected: Boolean get() = Modifier.isProtected(modifiers)
    private inline val Member.isPublic: Boolean get() = Modifier.isPublic(modifiers)

    private val Member.access: AccessLevel
        get() = when {
            Modifier.isPrivate(modifiers) -> AccessLevel.PRIVATE
            Modifier.isProtected(modifiers) -> AccessLevel.PROTECTED
            Modifier.isPublic(modifiers) -> AccessLevel.PUBLIC
            else -> AccessLevel.DEFAULT //No scope modifiers = package private
        }

    private enum class AccessLevel {
        PRIVATE,
        DEFAULT,
        PROTECTED,
        PUBLIC;
    }
}