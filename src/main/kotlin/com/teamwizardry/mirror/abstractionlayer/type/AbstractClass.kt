package com.teamwizardry.mirror.abstractionlayer.type

import com.teamwizardry.mirror.abstractionlayer.field.AbstractField
import com.teamwizardry.mirror.abstractionlayer.method.AbstractMethod

internal class AbstractClass(type: Class<*>): AbstractType<Class<*>>(type) {
    val isArray = type.isArray
    val componentType by lazy { type.componentType?.let { AbstractClass(it) } }
    val genericSuperclass by lazy { type.genericSuperclass?.let { create(it) } }
    val genericInterfaces by lazy { type.genericInterfaces.map { create(it) } }
    val typeParameters by lazy { type.typeParameters.map { AbstractTypeVariable(it) } }

    val declaredFields by lazy { type.declaredFields.map { AbstractField(it) } }
    val declaredMethods by lazy { type.declaredMethods.map { AbstractMethod(it) } }
}