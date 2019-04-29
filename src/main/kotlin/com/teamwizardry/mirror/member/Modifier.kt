package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.utils.unmodifiableSetOf

/**
 * Java Core Reflection Modifier class
 */
typealias JvmModifier = java.lang.reflect.Modifier

/**
 * A convenient wrapper for Java's Core Reflection Modifiers.
 */
enum class Modifier(
    /**
     * The Core Reflection modifier int bitmask
     */
    val mask: Int
) {
    ABSTRACT(JvmModifier.ABSTRACT),
    FINAL(JvmModifier.FINAL),
    INTERFACE(JvmModifier.INTERFACE),
    NATIVE(JvmModifier.NATIVE),
    PRIVATE(JvmModifier.PRIVATE),
    PROTECTED(JvmModifier.PROTECTED),
    PUBLIC(JvmModifier.PUBLIC),
    STATIC(JvmModifier.STATIC),
    STRICT(JvmModifier.STRICT),
    SYNCHRONIZED(JvmModifier.SYNCHRONIZED),
    TRANSIENT(JvmModifier.TRANSIENT),
    VOLATILE(JvmModifier.VOLATILE);

    /**
     * Returns true if this modifier is present in the passed mods
     */
    fun test(mods: Int) = mask and mods != 0

    companion object {
        /**
         * The valid modifiers for class declarations
         */
        @JvmStatic val CLASS = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, FINAL, STRICT)
        /**
         * The valid modifiers for interface declarations
         */
        @JvmStatic val INTERFACE = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, STRICT)
        /**
         * The valid modifiers for constructor declarations
         */
        @JvmStatic val CONSTRUCTOR = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE)
        /**
         * The valid modifiers for method declarations
         */
        @JvmStatic val METHOD = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, FINAL, SYNCHRONIZED, NATIVE, STRICT)
        /**
         * The valid modifiers for field declarations
         */
        @JvmStatic val FIELD = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, STATIC, FINAL, TRANSIENT, VOLATILE)
        /**
         * The valid modifiers for parameter declarations
         */
        @JvmStatic val PARAMETER = unmodifiableSetOf(FINAL)
        /**
         * The access modifiers
         */
        @JvmStatic val ACCESS = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE)

        /**
         * Extracts a set of [Modifiers][Modifier] from the given Core Reflection mods
         */
        @JvmStatic fun fromModifiers(mods: Int): Set<Modifier> {
            return values().filter { it.test(mods) }.toSet()
        }
    }

    /**
     * Accessibility levels, including default (package-private) visibility
     */
    enum class Access(
        /**
         * The [Modifier] corresponding to this access level, or null for default (package-private) visibility
         */
        val modifier: Modifier?
    ) {
        PRIVATE(Modifier.PRIVATE),
        PROTECTED(Modifier.PROTECTED),
        DEFAULT(null),
        PUBLIC(Modifier.PUBLIC);

        companion object {
            @JvmStatic
            fun fromModifiers(mods: Int): Access {
                return when {
                    JvmModifier.isPrivate(mods) -> PRIVATE
                    JvmModifier.isProtected(mods) -> PROTECTED
                    JvmModifier.isPublic(mods) -> PUBLIC
                    else -> DEFAULT
                }
            }
        }
    }
}
