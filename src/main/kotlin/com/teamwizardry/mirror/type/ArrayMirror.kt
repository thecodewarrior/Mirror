package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.type.AbstractArrayType
import com.teamwizardry.mirror.utils.lazyOrSet

/**
 * A mirror that represents an array type
 */
class ArrayMirror internal constructor(override val cache: MirrorCache, override val abstractType: AbstractArrayType): ConcreteTypeMirror() {
    override var java: Class<*> = abstractType.javaArrayClass

    /**
     * The component type of this mirror. `String` in `[String]`, `int` in `[int]`, `T` in `[T]`, etc.
     */
    var component: TypeMirror by lazyOrSet {
        cache.types.reflect(abstractType.componentType)
    }
        internal set

    var raw: ArrayMirror = this
        internal set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArrayMirror) return false

        if (cache != other.cache) return false
        if (component != other.component) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cache.hashCode()
        result = 31 * result + component.hashCode()
        return result
    }

    override fun toString(): String {
        return "[$component]"
    }
}