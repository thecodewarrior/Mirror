package com.teamwizardry.mirror.abstractionlayer.type

import java.lang.reflect.WildcardType

internal class AbstractWildcardType(type: WildcardType): AbstractType<WildcardType>(type) {
    val lowerBounds = type.lowerBounds.map { AbstractType.create(it) }
    val upperBounds = type.upperBounds.map { AbstractType.create(it) }
}