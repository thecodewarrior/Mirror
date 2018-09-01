package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.type.AbstractWildcardType
import com.teamwizardry.mirror.utils.lazyOrSet
import java.lang.reflect.WildcardType

class WildcardMirror internal constructor(override val cache: MirrorCache, override val abstractType: AbstractWildcardType): TypeMirror() {
    override val rawType: WildcardType = abstractType.type
    /**
     * `? super T` or `out T`. The lowermost type in the hierarchy that is valid. Any valid type must be a supertype
     * of `T`.
     *
     * When referring to something with this type you will be able to store an instance of the lower bounds.
     *
     * ```
     * For `? super AbstractList`
     * - Object          - Valid
     * - List<T>         - Valid
     * * AbstractList<T> - Valid
     * - ArrayList<>     - Invalid. `anArrayList = superAbstractListVariable` will throw
     */
    var lowerBounds: List<TypeMirror> by lazyOrSet {
        abstractType.lowerBounds.map { cache.types.reflect(it) }
    }
        internal set

    /**
     * `? extends T` or `in T`. The uppermost type in the hierarchy that is valid. Any valid type must be a subclass
     * of or implement the classes in [upperBounds]
     *
     * When referring to something with this type you will get something that is an instance of the upper bounds.
     *
     * ```
     * For `? extends List`
     * - Object          - Invalid. `extendsListVariable = anObject` will throw
     * * List<T>         - Valid
     * - AbstractList<T> - Valid
     * - ArrayList<T>    - Valid
     * ```
     */
    var upperBounds: List<TypeMirror> by lazyOrSet {
        abstractType.upperBounds.map { cache.types.reflect(it) }
    }
        internal set

    var raw: WildcardMirror = this
        internal set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WildcardMirror) return false

        if (cache != other.cache) return false
        if (abstractType != other.abstractType) return false
        if (upperBounds != other.upperBounds) return false
        if (lowerBounds != other.lowerBounds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cache.hashCode()
        result = 31 * result + abstractType.hashCode()
        result = 31 * result + upperBounds.hashCode()
        result = 31 * result + lowerBounds.hashCode()
        return result
    }

    override fun toString(): String {
        var str = "?"
        if(upperBounds.isNotEmpty()) {
            // java spec doesn't have multi-bounded wildcards, but we don't want to throw away data, so join to ` & `
            str += " super ${upperBounds.joinToString(" & ")}"
        }
        if(lowerBounds.isNotEmpty()) {
            // java spec doesn't have multi-bounded wildcards, but we don't want to throw away data, so join to ` & `
            str += " extends ${lowerBounds.joinToString(" & ")}"
        }
        return str
    }
}