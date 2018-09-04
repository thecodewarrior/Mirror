package com.teamwizardry.mirror.abstractionlayer.type

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

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
            if(type == Void.TYPE) return AbstractVoid
            return when(type) {
                is Class<*> ->
                    if(type.isArray)
                        AbstractArrayType(type)
                    else
                        AbstractClass(type)
                is GenericArrayType -> AbstractArrayType(type)
                is ParameterizedType -> AbstractParameterizedType(type)
                is TypeVariable<*> -> AbstractTypeVariable(type)
                is WildcardType -> AbstractWildcardType(type)
                else -> throw IllegalArgumentException("Unknown type $type")
            }
        }
    }
}
