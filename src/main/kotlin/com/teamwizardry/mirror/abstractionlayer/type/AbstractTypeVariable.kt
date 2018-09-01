package com.teamwizardry.mirror.abstractionlayer.type

import java.lang.reflect.TypeVariable

internal class AbstractTypeVariable(type: TypeVariable<*>): AbstractType<TypeVariable<*>>(type) {
    val bounds = type.bounds.map { AbstractType.create(it) }
}