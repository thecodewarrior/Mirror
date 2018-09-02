package com.teamwizardry.mirror.abstractionlayer.method

import com.teamwizardry.mirror.abstractionlayer.type.AbstractType
import com.teamwizardry.mirror.abstractionlayer.type.AbstractTypeVariable
import java.lang.reflect.Method

internal class AbstractMethod(val method: Method) {
    val name = method.name
    val returnType by lazy { AbstractType.create(method.genericReturnType) }
    val parameters by lazy { method.parameters.map { AbstractParameter(it) } }
    val exceptionTypes by lazy { method.genericExceptionTypes.map { AbstractType.create(it) } }
    val typeParameters by lazy { method.typeParameters.map { AbstractTypeVariable(it) }}

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