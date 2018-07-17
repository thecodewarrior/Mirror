package com.teamwizardry.mirror.reflection.type

import com.teamwizardry.mirror.reflection.MirrorCache
import com.teamwizardry.mirror.reflection.abstractionlayer.type.AbstractClass
import com.teamwizardry.mirror.reflection.abstractionlayer.type.AbstractGenericArrayType
import com.teamwizardry.mirror.reflection.abstractionlayer.type.AbstractType
import com.teamwizardry.mirror.reflection.utils.lazyOrSet

/**
 * Represents array types (`[String]`, `[int]`, `[T]`, etc.)
 */
class ArrayMirror internal constructor(override val cache: MirrorCache, override val abstractType: AbstractType<*>): ConcreteTypeMirror() {
    override var rawType: Class<*> = when(abstractType) {
        is AbstractClass -> abstractType.type
        is AbstractGenericArrayType -> Array<Any>::class.java
        else -> throw IllegalArgumentException("ArrayMirror type not a Class or GenericArrayType. It is a ${abstractType.javaClass.simpleName}")
    }

    /**
     * The component type of this mirror. `String` in `[String]`, `int` in `[int]`, `T` in `[T]`, etc.
     */
    var component: TypeMirror by lazyOrSet {
        when(abstractType) {
            is AbstractClass -> abstractType.componentType?.let { cache.reflect(it) }
                    ?: throw IllegalArgumentException("ArrayMirror type is a non-array Class")
            is AbstractGenericArrayType -> cache.reflect(abstractType.genericComponentType)
            else -> throw IllegalArgumentException("ArrayMirror type not a Class or GenericArrayType. It is a ${abstractType.javaClass.simpleName}")
        }
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