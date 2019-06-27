package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.utils.unmodifiableCopy

/**
 * Represents the cumulative generic mappings of nested elements.
 *
 * For example, in the following the TypeMapping for a fully specialized `someMethod` would contain mappings for
 * `A`, `B`, and `C`.
 * ```
 * class Outer<A> {
 *     class Inner<B> {
 *         <C> void someMethod() {
 *         }
 *     }
 * }
 * ```
 */
class TypeMapping(
    genericMapping: Map<TypeMirror, TypeMirror>
) {
    private val typeMap: Map<TypeMirror, TypeMirror> = genericMapping.filter { it.key != it.value }.unmodifiableCopy()

    /**
     * Gets the mapped type, applying the mapping to type parameters and array components where necessary.
     */
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

    /**
     * Creates a TypeMapping containing both the entries from this and [other]. When a mapping exists in both, [other]'s
     * mapping takes precedence.
     */
    @JvmName("join")
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