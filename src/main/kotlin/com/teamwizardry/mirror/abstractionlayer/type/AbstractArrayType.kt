package com.teamwizardry.mirror.abstractionlayer.type

import java.lang.reflect.AnnotatedArrayType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Type

internal class AbstractArrayType(type: Type, annotated: AnnotatedType?)
    : AbstractType<Type, AnnotatedArrayType>(type, annotated) {
    init {
        if (type !is GenericArrayType && type !is Class<*>)
            throw IllegalArgumentException("Object $type (${type.javaClass}) is not a GenericArrayType or Class")
    }

    val componentType: AbstractType<*, *> = create(
        when (type) {
            is GenericArrayType -> type.genericComponentType
            is Class<*> -> type.componentType
            else -> throw IllegalStateException("Object $type (${type.javaClass}) is not a GenericArrayType or Class")
        },
        this.annotated?.annotatedGenericComponentType
    )

    val javaArrayClass: Class<*> =
        when (type) {
            is GenericArrayType -> Array<Any>::class.java
            is Class<*> -> type
            else -> throw IllegalArgumentException("Object $type (${type.javaClass}) is not a GenericArrayType or Class")
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractArrayType) return false

        if (type != other.type) return false
        if (declaredAnnotations != other.declaredAnnotations) return false
        if (annotations != other.annotations) return false

        if (componentType != other.componentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + declaredAnnotations.hashCode()
        result = 31 * result + annotations.hashCode()

        result = 31 * result + componentType.hashCode()

        return result
    }
}