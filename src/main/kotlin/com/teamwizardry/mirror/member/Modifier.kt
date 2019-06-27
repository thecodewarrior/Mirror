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
    /** The Java `abstract` modifier */
    ABSTRACT(JvmModifier.ABSTRACT),
    /** The Java `final` modifier */
    FINAL(JvmModifier.FINAL),
    /** The Java `interface` class modifier */
    INTERFACE(JvmModifier.INTERFACE),
    /** The Java `native` modifier */
    NATIVE(JvmModifier.NATIVE),
    /** The Java `private` access modifier */
    PRIVATE(JvmModifier.PRIVATE),
    /** The Java `protected` access modifier */
    PROTECTED(JvmModifier.PROTECTED),
    /** The Java `public` access modifier */
    PUBLIC(JvmModifier.PUBLIC),
    /** The Java `static` modifier */
    STATIC(JvmModifier.STATIC),
    /** The Java `strictfp` modifier */
    STRICT(JvmModifier.STRICT),
    /** The Java `synchronized` modifier */
    SYNCHRONIZED(JvmModifier.SYNCHRONIZED),
    /** The Java `transient` modifier */
    TRANSIENT(JvmModifier.TRANSIENT),
    /** The Java `volatile` modifier */
    VOLATILE(JvmModifier.VOLATILE);

    /**
     * Returns true if this modifier is present in the passed mods
     */
    fun test(mods: Int): Boolean = mask and mods != 0

    companion object {
        /**
         * The valid modifiers for class declarations
         *
         * **Note: this value is immutable**
         */
        @JvmStatic val CLASS: Set<Modifier> = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, FINAL, STRICT)
        /**
         * The valid modifiers for interface declarations
         *
         * **Note: this value is immutable**
         */
        @JvmStatic val INTERFACE: Set<Modifier> = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, STRICT)
        /**
         * The valid modifiers for constructor declarations
         *
         * **Note: this value is immutable**
         */
        @JvmStatic val CONSTRUCTOR: Set<Modifier> = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE)
        /**
         * The valid modifiers for method declarations
         *
         * **Note: this value is immutable**
         */
        @JvmStatic val METHOD: Set<Modifier> = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, FINAL, SYNCHRONIZED, NATIVE, STRICT)
        /**
         * The valid modifiers for field declarations
         *
         * **Note: this value is immutable**
         */
        @JvmStatic val FIELD: Set<Modifier> = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, STATIC, FINAL, TRANSIENT, VOLATILE)
        /**
         * The valid modifiers for parameter declarations
         *
         * **Note: this value is immutable**
         */
        @JvmStatic val PARAMETER: Set<Modifier> = unmodifiableSetOf(FINAL)
        /**
         * The access modifiers
         *
         * **Note: this value is immutable**
         */
        @JvmStatic val ACCESS: Set<Modifier> = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE)

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
        /**
         * The `private` access level
         */
        PRIVATE(Modifier.PRIVATE),
        /**
         * The `protected` access level
         */
        PROTECTED(Modifier.PROTECTED),
        /**
         * The default, or "package private", access level
         */
        DEFAULT(null),
        /**
         * The `public` access level
         */
        PUBLIC(Modifier.PUBLIC);

        companion object {
            /**
             * Extract the access level from the passed modifier int. If multiple are present the most open one will be
             * returned.
             */
            @JvmStatic
            fun fromModifiers(mods: Int): Access {
                return when {
                    JvmModifier.isPublic(mods) -> PUBLIC
                    JvmModifier.isProtected(mods) -> PROTECTED
                    JvmModifier.isPrivate(mods) -> PRIVATE
                    else -> DEFAULT
                }
            }
        }
    }
}
