package com.teamwizardry.mirror.abstractionlayer.type

import java.lang.reflect.GenericArrayType

internal class AbstractGenericArrayType(type: GenericArrayType): AbstractType<GenericArrayType>(type) {
    val genericComponentType: AbstractType<*>; get() = create(type.genericComponentType)
}