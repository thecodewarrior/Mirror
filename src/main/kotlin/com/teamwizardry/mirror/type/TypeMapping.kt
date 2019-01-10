package com.teamwizardry.mirror.type

class TypeMapping(
    private val genericMapping: Map<TypeMirror, TypeMirror>
) {
    operator fun get(type: TypeMirror): TypeMirror {
        genericMapping[type]?.let {
            return it
        }

        when (type) {
            is ArrayMirror -> {
                val component = this[type.component]
                if(component != type.component) {
                    return type.specialize(component)
                }
            }
            is ClassMirror -> {
                val parameters = type.typeParameters.map { this[it] }
                if(parameters != type.typeParameters) {
                    return type.specialize(*parameters.toTypedArray())
                }
            }
        }

        return type
    }

}