package com.teamwizardry.mirror.abstractionlayer.method

import com.teamwizardry.mirror.abstractionlayer.type.AbstractType
import java.lang.reflect.Method

internal class AbstractMethod(val method: Method) {
    val name = method.name
    val returnType by lazy { AbstractType.create(method.annotatedReturnType) }
    val parameters by lazy { method.parameters.map { AbstractParameter(it) } }
    val exceptionTypes by lazy { method.annotatedExceptionTypes.map { AbstractType.create(it) } }
    val typeParameters by lazy { method.typeParameters.map { AbstractType.create(it) }}

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractMethod) return false

        if (method != other.method) return false

        return true
    }

    override fun hashCode(): Int {
        return method.hashCode()
    }
}