package com.teamwizardry.mirror.abstractionlayer.type

import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.ParameterizedType

internal class AbstractParameterizedType(type: ParameterizedType, annotated: AnnotatedType?)
    : AbstractType<ParameterizedType, AnnotatedParameterizedType>(type, annotated) {
    val rawType = createCast<AbstractClass>(type.rawType)
    val actualTypeArguments =
        this.annotated?.annotatedActualTypeArguments?.map { create(it) }
        ?: type.actualTypeArguments.map { create(it) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractParameterizedType) return false

        if (type != other.type) return false
        if (declaredAnnotations != other.declaredAnnotations) return false
        if (annotations != other.annotations) return false

        if (actualTypeArguments != other.actualTypeArguments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + declaredAnnotations.hashCode()
        result = 31 * result + annotations.hashCode()

        result = 31 * result + actualTypeArguments.hashCode()

        return result
    }
}