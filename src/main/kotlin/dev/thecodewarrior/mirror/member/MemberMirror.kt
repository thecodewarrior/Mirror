package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.utils.Untested
import java.lang.reflect.Member

abstract class MemberMirror internal constructor(
    internal val cache: MirrorCache,
    _enclosing: ClassMirror?
) {
    abstract val java: Member

    @Untested("only very basic tests during specialization")
    val declaringClass: ClassMirror by lazy {
        _enclosing ?: cache.types.reflect(java.declaringClass) as ClassMirror
    }

    abstract fun withDeclaringClass(enclosing: ClassMirror?): MemberMirror
}