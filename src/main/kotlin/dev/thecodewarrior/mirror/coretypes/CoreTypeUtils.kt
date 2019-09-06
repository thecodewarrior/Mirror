/*
 * License: Apache License, Version 2.0
 * See the NOTICE file in the root directory or at <a href="http://www.apache.org/licenses/LICENSE-2">apache.org</a>.
 */

package dev.thecodewarrior.mirror.coretypes

import java.lang.reflect.AnnotatedArrayType
import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.AnnotatedTypeVariable
import java.lang.reflect.AnnotatedWildcardType
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.util.Arrays
import java.util.Collections.emptyMap

internal object CoreTypeUtils {
    /**
     * Returns the display name of a Type.
     */
    @JvmStatic
    fun getTypeName(type: Type): String {
        return if (type is Class<*>) {
            if (type.isArray) getTypeName(type.componentType) + "[]" else type.name
        } else {
            type.toString()
        }
    }

    @JvmStatic
    fun typeArraysEqual(t1: Array<AnnotatedType>?, t2: Array<AnnotatedType>?): Boolean {
        if (t1 === t2) return true
        if (t1 == null) return false
        if (t2 == null) return false
        if (t1.size != t2.size) return false

        for (i in t1.indices) {
            if (t1[i].type != t2[i].type || !Arrays.equals(t1[i].annotations, t2[i].annotations)) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun hashCode(vararg types: AnnotatedType): Int {
        val typeHash = types
            .map { it.type.hashCode() }
            .fold(0) { acc, it -> 127 * acc xor it }
        val annotationHash = hashCode(types.flatMap { it.annotations.toList() })
        return 31 * typeHash xor annotationHash
    }

    @JvmStatic
    fun hashCode(annotations: List<Annotation>): Int {
        return annotations
            .map { 31 * it.annotationClass.hashCode() xor it.hashCode() }
            .fold(0) { acc, it -> 127 * acc xor it }
    }

    /**
     * Checks whether the two provided types are of the same structure and annotations on all levels.
     *
     * @param t1 The first type to be compared
     * @param t2 The second type to be compared
     *
     * @return True if both types have the same structure and annotations on all levels
     */
    @JvmStatic
    fun equals(t1: AnnotatedType, t2: AnnotatedType): Boolean {
        return (t1 as? AnnotatedTypeImpl ?: toCanonical(t1)) == (t2 as? AnnotatedTypeImpl ?: toCanonical(t2))
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(AnnotationFormatException::class)
    fun <A: Annotation> createAnnotation(annotationType: Class<A>, values: Map<String, Any>?): A {
        return Proxy.newProxyInstance(
            annotationType.classLoader,
            arrayOf<Class<*>>(annotationType),
            AnnotationInvocationHandler(annotationType, values ?: emptyMap())
        ) as A
    }

    @JvmStatic
    fun annotate(type: Type, annotations: Array<Annotation>): AnnotatedType {
        return replaceAnnotations(annotate(type), annotations)
    }

    @JvmStatic
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
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T: AnnotatedType> replaceAnnotations(original: T, annotations: Array<Annotation>): T {
        if (original is AnnotatedParameterizedType) {
            return AnnotatedParameterizedTypeImpl(original.getType() as ParameterizedType, annotations,
                (original as AnnotatedParameterizedType).annotatedActualTypeArguments) as T
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

    /**
     * Returns an [AnnotatedType] functionally identical to the given one, but in a canonical form that
     * implements `equals` and `hashCode`.
     *
     * @param type The type to turn into the canonical form
     *
     * @return A type functionally equivalent to the given one, but in the canonical form
     */
    @JvmStatic
    fun <T: AnnotatedType> toCanonical(type: T): T {
        return toCanonical(type) { it }
    }

    /**
     * This is the method underlying [.toCanonical].
     * If goes recursively through the structure of the provided [AnnotatedType] turning all type parameters,
     * bounds etc encountered into their canonical forms
     *
     * @param type The type to annotate
     * @param leafTransformer The transformer function to apply to leaf types (e.g. to box primitives)
     *
     * @return Type whose structure has been recursively annotated
     *
     *
     * See [CaptureType]
     */
    @JvmStatic
    private fun <T: AnnotatedType> toCanonical(type: T, leafTransformer: (Type) -> Type): T {
        return transform(type, object: TypeVisitor() {
            override fun visitClass(type: AnnotatedType): AnnotatedType {
                return AnnotatedTypeImpl(leafTransformer(type.type), type.annotations)
            }

            override fun visitArray(type: AnnotatedArrayType): AnnotatedType {
                return AnnotatedArrayTypeImpl(leafTransformer(type.type), type.annotations,
                    transform(type.annotatedGenericComponentType, this))
            }
        }) as T
    }

    @JvmStatic
    fun transform(type: AnnotatedType, visitor: TypeVisitor): AnnotatedType {
        return when {
            type is AnnotatedParameterizedType -> visitor.visitParameterizedType(type)
            type is AnnotatedWildcardType -> visitor.visitWildcardType(type)
            type is AnnotatedTypeVariable -> visitor.visitVariable(type)
            type is AnnotatedArrayType -> visitor.visitArray(type)
            type.type is Class<*> -> visitor.visitClass(type)
            else -> visitor.visitUnmatched(type)
        }
    }

    /**
     * Returns the erasure of the given type.
     */
    @JvmStatic
    fun erase(type: Type): Class<*> {
        return when (type) {
            is Class<*> -> type
            is ParameterizedType -> type.rawType as Class<*>
            is TypeVariable<*> -> if (type.bounds.isEmpty()) Any::class.java else erase(type.bounds[0])
            is GenericArrayType -> TypeImplAccess.createArrayType(erase(type.genericComponentType))
            is WildcardType -> erase(type.lowerBounds.getOrNull(0) ?: type.upperBounds[0])
            else -> throw RuntimeException("not supported: " + type.javaClass)
        }
    }
}