package com.teamwizardry.mirror

import com.teamwizardry.mirror.member.FieldMirrorCache
import com.teamwizardry.mirror.member.ExecutableMirrorCache
import com.teamwizardry.mirror.member.ParameterMirrorCache
import com.teamwizardry.mirror.type.TypeMirrorCache

internal class MirrorCache {
    val types = TypeMirrorCache(this)
    val fields = FieldMirrorCache(this)
    val executables = ExecutableMirrorCache(this)
    val parameters = ParameterMirrorCache(this)
}

