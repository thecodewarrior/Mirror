package dev.thecodewarrior.mirror

import dev.thecodewarrior.mirror.member.FieldMirrorCache
import dev.thecodewarrior.mirror.member.ExecutableMirrorCache
import dev.thecodewarrior.mirror.member.ParameterMirrorCache
import dev.thecodewarrior.mirror.type.TypeMirrorCache

internal class MirrorCache {
    val types = TypeMirrorCache(this)
    val fields = FieldMirrorCache(this)
    val executables = ExecutableMirrorCache(this)
    val parameters = ParameterMirrorCache(this)
}

