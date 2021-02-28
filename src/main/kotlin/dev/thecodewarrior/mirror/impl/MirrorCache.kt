package dev.thecodewarrior.mirror.impl

internal class MirrorCache {
    val types = TypeMirrorCache(this)
    val fields = FieldMirrorCache(this)
    val executables = ExecutableMirrorCache(this)
    val parameters = ParameterMirrorCache(this)
}

