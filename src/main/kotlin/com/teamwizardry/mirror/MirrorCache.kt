package com.teamwizardry.mirror

import com.teamwizardry.mirror.member.FieldMirrorCache
import com.teamwizardry.mirror.member.MethodMirrorCache
import com.teamwizardry.mirror.member.ParameterMirrorCache
import com.teamwizardry.mirror.type.TypeMirrorCache

internal class MirrorCache {
    val types = TypeMirrorCache(this)
    val fields = FieldMirrorCache(this)
    val methods = MethodMirrorCache(this)
    val parameters = ParameterMirrorCache(this)
}

