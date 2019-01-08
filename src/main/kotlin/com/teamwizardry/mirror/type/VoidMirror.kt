package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.type.AbstractType
import java.lang.reflect.Type

class VoidMirror internal constructor(override val cache: MirrorCache, override val abstractType: AbstractType<*, *>): TypeMirror() {
    override val java: Type
        get() = Void.TYPE
}