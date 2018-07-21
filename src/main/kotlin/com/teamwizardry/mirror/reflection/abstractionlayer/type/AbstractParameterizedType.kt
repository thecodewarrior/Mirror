package com.teamwizardry.mirror.reflection.abstractionlayer.type

import java.lang.reflect.ParameterizedType

internal class AbstractParameterizedType(type: ParameterizedType): AbstractType<ParameterizedType>(type) {
    val rawType = AbstractClass(type.rawType as Class<*>)
    val actualTypeArguments = type.actualTypeArguments.map { create(it) }
}