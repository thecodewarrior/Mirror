package com.teamwizardry.mirror.reflection.abstractionlayer.type

import java.lang.reflect.*

internal abstract class AbstractType<T: Type>(val type: T) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractType<*>) return false

        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    internal companion object {
        fun create(type: Type): AbstractType<*> {
            return when(type) {
                is Class<*> -> AbstractClass(type)
                is GenericArrayType -> AbstractGenericArrayType(type)
                is ParameterizedType -> AbstractParameterizedType(type)
                is TypeVariable<*> -> AbstractTypeVariable(type)
                is WildcardType -> AbstractWildcardType(type)
                else -> throw IllegalArgumentException("Unknown type $type")
            }
        }
    }
}
