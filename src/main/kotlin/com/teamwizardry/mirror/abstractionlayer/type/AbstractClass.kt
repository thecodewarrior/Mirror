package com.teamwizardry.mirror.abstractionlayer.type

import com.teamwizardry.mirror.abstractionlayer.field.AbstractField
import com.teamwizardry.mirror.abstractionlayer.method.AbstractMethod
import java.lang.reflect.AnnotatedType

internal class AbstractClass(type: Class<*>, annotated: AnnotatedType?)
    : AbstractType<Class<*>, AnnotatedType>(type, annotated) {
    val genericSuperclass by lazy { type.annotatedSuperclass?.let { create(it) } }
    val genericInterfaces by lazy { type.annotatedInterfaces.map { create(it) } }
    val typeParameters by lazy { type.typeParameters.map { createCast<AbstractTypeVariable>(it) } }

    val declaredFields by lazy { type.declaredFields.map { AbstractField(it) } }
    val declaredMethods by lazy { type.declaredMethods.map { AbstractMethod(it) } }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractWildcardType) return false

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