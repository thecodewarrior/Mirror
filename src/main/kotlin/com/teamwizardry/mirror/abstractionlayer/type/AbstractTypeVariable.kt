package com.teamwizardry.mirror.abstractionlayer.type

import java.lang.reflect.AnnotatedType
import java.lang.reflect.TypeVariable

internal class AbstractTypeVariable(type: TypeVariable<*>, annotated: AnnotatedType?)
    : AbstractType<TypeVariable<*>, AnnotatedType>(type, annotated) {
    val bounds = type.annotatedBounds.map { create(it) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractTypeVariable) return false

        if (type != other.type) return false
        if (declaredAnnotations != other.declaredAnnotations) return false
        if (annotations != other.annotations) return false

        if (bounds != other.bounds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + declaredAnnotations.hashCode()
        result = 31 * result + annotations.hashCode()

        result = 31 * result + bounds.hashCode()

        return result
    }
}