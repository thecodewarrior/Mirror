package com.teamwizardry.mirror.abstractionlayer.type

import java.lang.reflect.AnnotatedType
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

internal abstract class AbstractType<T: Type, A: AnnotatedType>(val type: T, annotated: AnnotatedType?) {
    @Suppress("UNCHECKED_CAST")
    val annotated: A? = annotated as A?
    val annotations: List<Annotation> = annotated?.annotations?.toList() ?: emptyList()
    val declaredAnnotations: List<Annotation> = annotated?.declaredAnnotations?.toList() ?: emptyList()

    internal companion object {
        fun create(annotated: AnnotatedType): AbstractType<*, *> = create(annotated.type, annotated)

        @JvmOverloads
        fun create(type: Type, annotated: AnnotatedType? = null): AbstractType<*, *> {
            if(type == Void.TYPE) return AbstractVoid(type, annotated)
            return when(type) {
                is Class<*> ->
                    if(type.isArray)
                        AbstractArrayType(type, annotated)
                    else
                        AbstractClass(type, annotated)
                is GenericArrayType -> AbstractArrayType(type, annotated)
                is ParameterizedType -> AbstractParameterizedType(type, annotated)
                is TypeVariable<*> -> AbstractTypeVariable(type, annotated)
                is WildcardType -> AbstractWildcardType(type, annotated)
                else -> throw IllegalArgumentException("Unknown type $type")
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun <T: AbstractType<*, *>> createCast(annotated: AnnotatedType)
            : T = create(annotated) as T
        @Suppress("UNCHECKED_CAST")
        fun <T: AbstractType<*, *>> createCast(type: Type, annotated: AnnotatedType? = null)
            : T = create(type, annotated) as T
    }
}
