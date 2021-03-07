package dev.thecodewarrior.mirror.util

import dev.thecodewarrior.mirror.member.MemberMirror
import dev.thecodewarrior.mirror.member.ParameterMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeVariableMirror

/**
 * A mirror with a source code declaration.
 *
 * @see ClassMirror
 * @see TypeVariableMirror
 * @see MemberMirror
 * @see ParameterMirror
 */
public interface DeclarationMirror {
    /**
     * Returns a specialized approximation of the source code declaration this mirror represents. In [ClassMirror],
     * [MemberMirror], and [ParameterMirror] this automatically chooses the appropriate language. In
     * [TypeVariableMirror] this defaults to Java.
     */
    public fun toDeclarationString(): String

    /**
     * Returns a specialized approximation of the Java source code declaration this mirror represents.
     */
    public fun toJavaDeclarationString(): String

    /**
     * Returns a specialized approximation of the Kotlin source code declaration this mirror represents.
     */
    public fun toKotlinDeclarationString(): String
}