package com.teamwizardry.mirror.reflection.abstractionlayer.type

import java.lang.reflect.GenericArrayType

class AbstractGenericArrayType(type: GenericArrayType): AbstractType<GenericArrayType>(type) {
    val genericComponentType: AbstractType<*>; get() = create(type.genericComponentType)
}