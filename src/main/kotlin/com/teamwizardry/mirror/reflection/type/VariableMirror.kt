package com.teamwizardry.mirror.reflection.type

import com.teamwizardry.mirror.reflection.MirrorCache
import com.teamwizardry.mirror.reflection.abstractionlayer.type.AbstractTypeVariable
import java.lang.reflect.TypeVariable

/**
 * Represents type variables (the `T` in `List<T>` and `public T theField`)
 */
class VariableMirror internal constructor(override val cache: MirrorCache, override val abstractType: AbstractTypeVariable): TypeMirror() {
    override val rawType: TypeVariable<*> = abstractType.type

    /**
     * The bounds of this variable. Types specializing this variable must extend all of these.
     *
     * By default it contains the [Object] mirror.
     */
    val bounds: List<TypeMirror> = abstractType.bounds.map { cache.types.reflect(it) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VariableMirror) return false

        if (cache != other.cache) return false
        if (abstractType != other.abstractType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cache.hashCode()
        result = 31 * result + abstractType.hashCode()
        return result
    }

    override fun toString(): String {
        var str = ""
        str += abstractType.type.name
        if(bounds.isNotEmpty()) {
            str += " extends ${bounds.joinToString(" & ")}"
        }
        return str
    }
}
