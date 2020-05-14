package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.utils.Untested
import java.lang.reflect.Member

/**
 * The abstract supclass representing any Java class member
 */
abstract class MemberMirror internal constructor(
    internal val cache: MirrorCache,
    _enclosing: ClassMirror?
) {
    /**
     * The Core Reflection object this mirror represents
     */
    abstract val java: Member

    /**
     * The potentially specialized class this member is declared in
     */
    val declaringClass: ClassMirror by lazy {
        _enclosing ?: cache.types.reflect(java.declaringClass) as ClassMirror
    }

    /**
     * Gets the version of this mirror specialized for the specified declaring class, substituting type parameters as
     * necessary.
     */
    abstract fun withDeclaringClass(enclosing: ClassMirror?): MemberMirror
}