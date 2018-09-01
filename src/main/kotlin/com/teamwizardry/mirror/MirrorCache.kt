package com.teamwizardry.mirror

import com.teamwizardry.mirror.abstractionlayer.type.*
import com.teamwizardry.mirror.member.FieldMirrorCache
import com.teamwizardry.mirror.type.*

internal class MirrorCache {
    val types = TypeMirrorCache(this)
    val fields = FieldMirrorCache(this)

    companion object {
        @JvmStatic val DEFAULT = MirrorCache()
    }
}

