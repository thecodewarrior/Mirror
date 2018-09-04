package com.teamwizardry.mirror.abstractionlayer.type

import java.lang.reflect.GenericArrayType
import java.lang.reflect.Type

internal class AbstractArrayType(type: Type): AbstractType<Type>(type) {
    init {
        if (type !is GenericArrayType && type !is Class<*>)
            throw IllegalArgumentException("Object $type (${type.javaClass}) is not a GenericArrayType or Class")
    }

    val componentType: AbstractType<*>; get() = create(
        when (type) {
            is GenericArrayType -> type.genericComponentType
            is Class<*> -> type.componentType
            else -> throw IllegalStateException("Object $type (${type.javaClass}) is not a GenericArrayType or Class")
        }
    )

    val javaArrayClass: Class<*> =
        when (type) {
            is GenericArrayType -> Array<Any>::class.java
            is Class<*> -> type
            else -> throw IllegalArgumentException("Object $type (${type.javaClass}) is not a GenericArrayType or Class")
        }
}