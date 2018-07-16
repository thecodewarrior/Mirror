package com.teamwizardry.mirror.reflection.abstractionlayer.type

class AbstractClass(type: Class<*>): AbstractType<Class<*>>(type) {
    val isArray = type.isArray
    val componentType by lazy { type.componentType?.let { AbstractClass(it) } }
    val genericSuperclass by lazy { type.genericSuperclass?.let { create(it) } }
    val genericInterfaces by lazy { type.genericInterfaces.map { create(it) } }
    val typeParameters by lazy { type.typeParameters.map { AbstractTypeVariable(it) } }
}