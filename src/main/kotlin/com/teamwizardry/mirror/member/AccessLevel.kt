package com.teamwizardry.mirror.member

import java.lang.reflect.Modifier

enum class AccessLevel {
    /**
     * The `private` access level. Visible only within the class itself.
     */
    PRIVATE,
    /**
     * The `protected` access level. Visible only to subclasses.
     */
    PROTECTED,
    /**
     * The default "no modifier" access level. Visible only within the same package
     */
    PACKAGE,
    /**
     * The `public` access level. Visible to everyone.
     */
    PUBLIC;

    companion object {
        @JvmStatic
        fun fromModifiers(modifiers: Int): AccessLevel {
            when {
                Modifier.isPrivate(modifiers) -> return PRIVATE
                Modifier.isProtected(modifiers) -> return PROTECTED
                Modifier.isPublic(modifiers) -> return PUBLIC
                else -> return PACKAGE
            }
        }
    }
}