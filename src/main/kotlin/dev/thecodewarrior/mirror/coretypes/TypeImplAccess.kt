package dev.thecodewarrior.mirror.coretypes

import java.lang.reflect.AnnotatedArrayType
import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.AnnotatedTypeVariable
import java.lang.reflect.AnnotatedWildcardType
import java.lang.reflect.GenericArrayType
import java.lang.reflect.GenericDeclaration
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

/**
 * `internal` access to all the package-private type implementations
 */
@Suppress("unused")
internal object TypeImplAccess {

    fun createAnnotatedArrayTypeImpl(type: Type, annotations: Array<Annotation>, componentType: AnnotatedType): AnnotatedArrayType  =
        AnnotatedArrayTypeImpl(type, annotations, componentType)

    fun createAnnotatedParameterizedTypeImpl(rawType: ParameterizedType, annotations: Array<Annotation>, typeArguments: Array<AnnotatedType>): AnnotatedParameterizedType  =
        AnnotatedParameterizedTypeImpl(rawType, annotations, typeArguments)

    fun createAnnotatedTypeImpl(type: Type): AnnotatedType  =
        AnnotatedTypeImpl(type, emptyArray())

    fun createAnnotatedTypeImpl(type: Type, annotations: Array<Annotation>): AnnotatedType  =
        AnnotatedTypeImpl(type, annotations)

    fun createAnnotatedTypeVariableImpl(type: TypeVariable<*>): AnnotatedTypeVariable  =
        AnnotatedTypeVariableImpl(type, type.annotations)

    fun createAnnotatedTypeVariableImpl(type: TypeVariable<*>, annotations: Array<Annotation>): AnnotatedTypeVariable  =
        AnnotatedTypeVariableImpl(type, annotations)

    fun createAnnotatedWildcardTypeImpl(type: WildcardType, annotations: Array<Annotation>, lowerBounds: Array<AnnotatedType>?, upperBounds: Array<AnnotatedType>?): AnnotatedWildcardType  =
        AnnotatedWildcardTypeImpl(type, annotations, lowerBounds, upperBounds)

    fun createGenericArrayTypeImpl(componentType: Type): GenericArrayType  =
        GenericArrayTypeImpl(componentType)

    fun createArrayType(componentClass: Class<*>): Class<*> =
        GenericArrayTypeImpl.createArrayType(componentClass)

    fun createArrayType(componentType: Type): Type =
        GenericArrayTypeImpl.createArrayType(componentType)

    fun createArrayType(componentType: AnnotatedType, annotations: Array<Annotation>): AnnotatedArrayType =
        AnnotatedArrayTypeImpl.createArrayType(componentType, annotations)

    fun createParameterizedTypeImpl(rawType: Class<*>, actualTypeArguments: Array<Type>, ownerType: Type?): ParameterizedType  =
        ParameterizedTypeImpl(rawType, actualTypeArguments, ownerType)

    fun <D: GenericDeclaration> createTypeVariableImpl(variable: TypeVariable<D>, bounds: Array<AnnotatedType>): TypeVariable<D>  =
        TypeVariableImpl(variable, variable.annotations, bounds)

    fun <D: GenericDeclaration> createTypeVariableImpl(variable: TypeVariable<D>, annotations: Array<Annotation>, bounds: Array<AnnotatedType>?): TypeVariable<D>  =
       TypeVariableImpl(variable, annotations, bounds)

    fun createWildcardTypeImpl(upperBounds: Array<Type>, lowerBounds: Array<Type>): WildcardType  =
        WildcardTypeImpl(upperBounds, lowerBounds)
}