package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.utils.unmodifiableCopy

class TypeMapping(
    genericMapping: Map<TypeMirror, TypeMirror>
) {
    val typeMap: Map<TypeMirror, TypeMirror> = genericMapping.filter { it.key != it.value }.unmodifiableCopy()

    operator fun get(type: TypeMirror): TypeMirror {
        typeMap[type]?.let {
            return it
        }

        when (type) {
            is ArrayMirror -> {
                val component = this[type.component]
                if(component != type.component) {
                    return type.withComponent(component)
                }
            }
            is ClassMirror -> {
                val parameters = type.typeParameters.map { this[it] }
                if(parameters != type.typeParameters) {
                    return type.withTypeArguments(*parameters.toTypedArray())
                }
            }
        }

        return type
    }

    operator fun plus(other: TypeMapping?): TypeMapping {
        if(other == null) return this
        return TypeMapping(this.typeMap + other.typeMap)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeMapping) return false

        if (typeMap != other.typeMap) return false

        return true
    }

    override fun hashCode(): Int {
        return typeMap.hashCode()
    }
}