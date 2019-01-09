package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.type.AbstractType
import com.teamwizardry.mirror.utils.unmodifiable
import java.lang.reflect.Type

class VoidMirror internal constructor(override val cache: MirrorCache, override val abstractType: AbstractType<*, *>): TypeMirror() {
    override val java: Type = Void.TYPE
    override val annotations: List<Annotation> = abstractType.annotations.unmodifiable()
}