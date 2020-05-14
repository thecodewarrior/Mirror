package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.unmodifiableSetOf

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
    val mask: Int,
    /**
     * The customary sorting order of modifiers, according to JLS §8.1.1, §8.3.1, and §8.4.3
     * `public|protected|private abstract static final transient volatile synchronized native strictfp`
     */
    val customaryOrder: Int
) {
    /** The Java `abstract` modifier */
    ABSTRACT(JvmModifier.ABSTRACT, 1),
    /** The Java `final` modifier */
    FINAL(JvmModifier.FINAL, 3),
    /** The Java `interface` class modifier */
    INTERFACE(JvmModifier.INTERFACE, -1),
    /** The Java `native` modifier */
    NATIVE(JvmModifier.NATIVE, 7),
    /** The Java `private` access modifier */
    PRIVATE(JvmModifier.PRIVATE, 0),
    /** The Java `protected` access modifier */
    PROTECTED(JvmModifier.PROTECTED, 0),
    /** The Java `public` access modifier */
    PUBLIC(JvmModifier.PUBLIC, 0),
    /** The Java `static` modifier */
    STATIC(JvmModifier.STATIC, 2),
    /** The Java `strictfp` modifier */
    STRICT(JvmModifier.STRICT, 8),
    /** The Java `synchronized` modifier */
    SYNCHRONIZED(JvmModifier.SYNCHRONIZED, 6),
    /** The Java `transient` modifier */
    TRANSIENT(JvmModifier.TRANSIENT, 4),
    /** The Java `volatile` modifier */
    VOLATILE(JvmModifier.VOLATILE, 5);

    /**
     * Returns true if this modifier is present in the passed mods
     */
    @Untested
    fun test(mods: Int): Boolean = mask and mods != 0

    override fun toString(): String {
        return super.toString().toLowerCase()
    }

    companion object {
        /**
         * The valid modifiers for class declarations. This set is in customary order, as defined in §8.1.1
         *
         * **Note: this value is immutable**
         */
        @JvmStatic val CLASS: Set<Modifier> = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, FINAL, STRICT)
        /**
         * The valid modifiers for interface declarations. This set is in customary order, as defined in §8.1.1
         *
         * **Note: this value is immutable**
         */
        @JvmStatic val INTERFACE: Set<Modifier> = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, STRICT)
        /**
         * The valid modifiers for constructor declarations. This set is in customary order, as defined in §8.8.3
         *
         * **Note: this value is immutable**
         */
        @JvmStatic val CONSTRUCTOR: Set<Modifier> = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE)
        /**
         * The valid modifiers for method declarations. This set is in customary order, as defined in §8.4.3
         *
         * **Note: this value is immutable**
         */
        @JvmStatic val METHOD: Set<Modifier> = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, FINAL, SYNCHRONIZED, NATIVE, STRICT)
        /**
         * The valid modifiers for field declarations. This set is in customary order, as defined in §8.3.1
         *
         * **Note: this value is immutable**
         */
        @JvmStatic val FIELD: Set<Modifier> = unmodifiableSetOf(PUBLIC, PROTECTED, PRIVATE, STATIC, FINAL, TRANSIENT, VOLATILE)
        /**
         * The valid modifiers for parameter declarations. This set is in customary order, as defined in §8.4.1
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
        @Untested
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

        override fun toString(): String {
            return super.toString().toLowerCase()
        }

        companion object {
            /**
             * Extract the access level from the passed modifier int. If multiple are present the most open one will be
             * returned.
             */
            @JvmStatic
            @Untested
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
