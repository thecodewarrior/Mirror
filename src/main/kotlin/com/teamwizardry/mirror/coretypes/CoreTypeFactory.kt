package com.teamwizardry.mirror.coretypes

import java.lang.reflect.AnnotatedArrayType
import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.AnnotatedTypeVariable
import java.lang.reflect.AnnotatedWildcardType
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

internal object CoreTypeFactory {
    fun annotate(type: Type, annotations: Array<Annotation>): AnnotatedType {
        return replaceAnnotations(annotate(type), annotations)
    }

    fun annotate(type: Type): AnnotatedType {
        if (type is ParameterizedType) {
            return AnnotatedParameterizedTypeImpl(
                type, emptyArray(), type.actualTypeArguments.map(::annotate).toTypedArray()
            )
        }
        if (type is WildcardType) {
            return AnnotatedWildcardTypeImpl(
                type, emptyArray(),
                type.lowerBounds.map(::annotate).toTypedArray(),
                type.upperBounds.map(::annotate).toTypedArray()
            )
        }
        if (type is TypeVariable<*>) {
            return AnnotatedTypeVariableImpl(type)
        }
        if (type is GenericArrayType) {
            return AnnotatedArrayTypeImpl(type, emptyArray(), annotate(type.genericComponentType))
        }
        if (type is Class<*>) {
            if (type.isArray) {
                val componentClass = type.componentType
                return AnnotatedArrayTypeImpl.createArrayType(
                    AnnotatedTypeImpl(componentClass, emptyArray()), emptyArray())
            }
            return AnnotatedTypeImpl(type, emptyArray())
        }
        throw IllegalArgumentException("Unrecognized type: " + type.typeName)
    }

    /**
     * Creates a new [AnnotatedType] of the same type as the original, but with its annotations
     * replaced with the provided ones.
     *
     * @param original The type whose structure is to be copied
     * @param annotations Annotations to use instead of the ones found on the `original`
     *
     * @return A type of the same structure as the original but with replaced annotations
     */
    @Suppress("UNCHECKED_CAST")
    fun <T: AnnotatedType> replaceAnnotations(original: T, annotations: Array<Annotation>): T {
        if (original is AnnotatedParameterizedType) {
            return AnnotatedParameterizedTypeImpl(original.getType() as ParameterizedType, annotations,
                (original as AnnotatedParameterizedType).annotatedActualTypeArguments) as T
        }
        if (original is AnnotatedCaptureType) {
            val capture = original as AnnotatedCaptureType
            return AnnotatedCaptureTypeImpl(
                capture.type as CaptureType,
                capture.annotatedWildcardType,
                capture.annotatedTypeVariable,
                capture.annotatedUpperBounds,
                annotations) as T
        }
        if (original is AnnotatedWildcardType) {
            return AnnotatedWildcardTypeImpl(original.getType() as WildcardType, annotations,
                (original as AnnotatedWildcardType).annotatedLowerBounds,
                (original as AnnotatedWildcardType).annotatedUpperBounds) as T
        }
        if (original is AnnotatedTypeVariable) {
            return AnnotatedTypeVariableImpl(original.getType() as TypeVariable<*>, annotations) as T
        }
        return if (original is AnnotatedArrayType) {
            AnnotatedArrayTypeImpl(original.getType(), annotations,
                (original as AnnotatedArrayType).annotatedGenericComponentType) as T
        } else AnnotatedTypeImpl(original.type, annotations) as T
    }

}