package com.teamwizardry.mirror.member

enum class AccessLevel {
    /**
     * The `public` access level. Visible to everyone.
     */
    PUBLIC,
    /**
     * The `internal` access level. Visible only within the same module. The term "Module" has various meanings, more
     * information is available [here.](https://kotlinlang.org/docs/reference/visibility-modifiers.html)
     */
    INTERNAL,
    /**
     * The default "no modifier" access level. Visible only within the same package
     */
    PACKAGE,
    /**
     * The `protected` access level. Visible only to subclasses.
     */
    PROTECTED,
    /**
     * The `private` access level. Visible only within the class itself.
     */
    PRIVATE
}