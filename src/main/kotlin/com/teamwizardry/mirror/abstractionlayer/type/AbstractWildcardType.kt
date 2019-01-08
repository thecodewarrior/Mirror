package com.teamwizardry.mirror.abstractionlayer.type

import java.lang.reflect.AnnotatedType
import java.lang.reflect.WildcardType

internal class AbstractWildcardType(type: WildcardType, annotated: AnnotatedType?): AbstractType<WildcardType, AnnotatedType>(type, annotated) {
    val lowerBounds = type.lowerBounds.map { create(it) }
    val upperBounds = type.upperBounds.map { create(it) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractWildcardType) return false

        if (type != other.type) return false
        if (declaredAnnotations != other.declaredAnnotations) return false
        if (annotations != other.annotations) return false

        if (lowerBounds != other.lowerBounds) return false
        if (upperBounds != other.upperBounds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + declaredAnnotations.hashCode()
        result = 31 * result + annotations.hashCode()

        result = 31 * result + lowerBounds.hashCode()
        result = 31 * result + upperBounds.hashCode()
        return result
    }
}