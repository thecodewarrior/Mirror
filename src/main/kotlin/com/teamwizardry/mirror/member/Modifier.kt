package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.utils.unmodifiableSetOf
import java.lang.reflect.Modifier as JModifier

enum class Modifier(val mask: Int) {
    ABSTRACT(JModifier.ABSTRACT),
    FINAL(JModifier.FINAL),
    INTERFACE(JModifier.INTERFACE),
    NATIVE(JModifier.NATIVE),
    PRIVATE(JModifier.PRIVATE),
    PROTECTED(JModifier.PROTECTED),
    PUBLIC(JModifier.PUBLIC),
    STATIC(JModifier.STATIC),
    STRICT(JModifier.STRICT),
    SYNCHRONIZED(JModifier.SYNCHRONIZED),
    TRANSIENT(JModifier.TRANSIENT),
    VOLATILE(JModifier.VOLATILE);

    fun test(mods: Int) = mask and mods != 0

    companion object {
        @JvmStatic val CLASS = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, FINAL, STRICT)
        @JvmStatic val INTERFACE = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, STRICT)
        @JvmStatic val CONSTRUCTOR = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE)
        @JvmStatic val METHOD = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, FINAL, SYNCHRONIZED, NATIVE, STRICT)
        @JvmStatic val FIELD = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, STATIC, FINAL, TRANSIENT, VOLATILE)
        @JvmStatic val PARAMETER = unmodifiableSetOf(FINAL)
        @JvmStatic val ACCESS = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE)

        @JvmStatic fun fromModifiers(mods: Int): Set<Modifier> {
            return values().filter { it.test(mods) }.toSet()
        }
    }

    enum class Access(val modifier: Modifier?) {
        PRIVATE(Modifier.PRIVATE),
        PROTECTED(Modifier.PROTECTED),
        DEFAULT(null),
        PUBLIC(Modifier.PUBLIC);

        companion object {
            @JvmStatic
            fun fromModifiers(mods: Int): Access {
                return when {
                    JModifier.isPrivate(mods) -> PRIVATE
                    JModifier.isProtected(mods) -> PROTECTED
                    JModifier.isPublic(mods) -> PUBLIC
                    else -> DEFAULT
                }
            }
        }
    }
}
