package com.teamwizardry.mirror.abstractionlayer.type

import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type

internal class AbstractVoid(type: Type, annotated: AnnotatedType?)
    : AbstractType<Class<*>, AnnotatedType>(Void.TYPE, annotated) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractVoid) return false

        if (type != other.type) return false
        if (declaredAnnotations != other.declaredAnnotations) return false
        if (annotations != other.annotations) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + declaredAnnotations.hashCode()
        result = 31 * result + annotations.hashCode()

        return result
    }
}
