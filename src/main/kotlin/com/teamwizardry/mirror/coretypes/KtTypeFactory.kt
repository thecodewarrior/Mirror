/*
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or at <a href="http://www.apache.org/licenses/LICENSE-2">apache.org</a>.
 */

package com.teamwizardry.mirror.coretypes

/*
/**
 * Utility class for creating instances of [Type]. These types can be used with the [ ] or anything else handling Java types.
 *
 * @author Wouter Coekaerts (wouter@coekaerts.be)
 */
object KtTypeFactory {
    private val UNBOUND_WILDCARD = WildcardTypeImpl(arrayOf<Type>(Any::class.java), arrayOf())

    /**
     * Creates a type of class `clazz` with `arguments` as type arguments.
     *
     *
     * For example: `parameterizedClass(Map.class, Integer.class, String.class)`
     * returns the type `Map<Integer, String>`.
     *
     * @param clazz     Type class of the type to create
     * @param arguments Type arguments for the variables of `clazz`, or null if these are not
     * known.
     * @return A [ParameterizedType], or simply `clazz` if `arguments` is
     * `null` or empty.
     */
    fun parameterizedClass(clazz: Class<*>, vararg arguments: Type): Type {
        return parameterizedInnerClass(null, clazz, *arguments)
    }

    fun parameterizedAnnotatedClass(owner: Type, clazz: Class<*>, annotations: Array<Annotation>, vararg arguments: AnnotatedType): AnnotatedType {
        if (arguments == null || arguments.size == 0) {
            return KtGenericTypeReflector.annotate(clazz, annotations)
        }
        val typeArguments = Arrays.stream(arguments).map<Type>(Function<AnnotatedType, Type> { it.getType() }).toArray<Type>(Type[]::new  /* Currently unsupported in Kotlin */)
        return AnnotatedParameterizedTypeImpl(parameterizedInnerClass(owner, clazz, *typeArguments) as ParameterizedType, annotations, arguments)
    }

    fun parameterizedAnnotatedType(type: ParameterizedType, typeAnnotations: Array<Annotation>, vararg argumentAnnotations: Array<Annotation>): AnnotatedParameterizedType {
        if (argumentAnnotations == null || argumentAnnotations.size == 0) {
            return KtGenericTypeReflector.annotate(type, typeAnnotations) as AnnotatedParameterizedType
        }
        val typeArguments = arrayOfNulls<AnnotatedType>(type.actualTypeArguments.size)
        for (i in typeArguments.indices) {
            val annotations = if (argumentAnnotations.size > i) argumentAnnotations[i] else null
            typeArguments[i] = KtGenericTypeReflector.annotate(type.actualTypeArguments[i], annotations)
        }
        return parameterizedAnnotatedClass(type.ownerType, KtGenericTypeReflector.erase(type), typeAnnotations, *typeArguments) as AnnotatedParameterizedType
    }

    /**
     * Creates a type of `clazz` nested in `owner`.
     *
     * @param owner The owner type. This should be a subtype of `clazz.getDeclaringClass()`,
     * or `null` if no owner is known.
     * @param clazz Type class of the type to create
     * @return A [ParameterizedType] if the class declaring `clazz` is generic and its
     * type parameters are known in `owner` and `clazz` itself has no type parameters.
     * Otherwise, just returns `clazz`.
     */
    fun innerClass(owner: Type, clazz: Class<*>): Type {
        return parameterizedInnerClass(owner, clazz, *null as Array<Type>?)
    }

    /**
     * Creates a type of `clazz` with `arguments` as type arguments, nested in
     * `owner`.
     *
     * In the ideal case, this returns a [ParameterizedType] with all
     * generic information in it. If some type arguments are missing or if the resulting type simply
     * doesn't need any type parameters, it returns the raw `clazz`. Note that types with
     * some parameters specified and others not, don't exist in Java.
     *
     * If the caller does not
     * know the exact `owner` type or `arguments`, `null` should be given (or
     * [.parameterizedClass] or [.innerClass] could be
     * used). If they are not needed (non-generic owner and/or `clazz` has no type
     * parameters), they will be filled in automatically. If they are needed but are not given, the
     * raw `clazz` is returned.
     *
     * The specified `owner` may be any subtype of
     * `clazz.getDeclaringClass()`. It is automatically converted into the right
     * parameterized version of the declaring class. If `clazz` is a `static` (nested)
     * class, the owner is not used.
     *
     * @param owner     The owner type. This should be a subtype of `clazz.getDeclaringClass()`,
     * or `null` if no owner is known.
     * @param clazz     Type class of the type to create
     * @param arguments Type arguments for the variables of `clazz`, or null if these are not
     * known.
     * @return A [ParameterizedType] if `clazz` or the class declaring `clazz`
     * is generic, and all the needed type arguments are specified in `owner` and
     * `arguments`. Otherwise, just returns `clazz`.
     * @throws IllegalArgumentException if `arguments` (is non-null and) has an incorrect
     * length, or if one of the `arguments` is not within
     * the bounds declared on the matching type variable, or if
     * owner is non-null but `clazz` has no declaring class
     * (e.g. is a top-level class), or if owner is not a a subtype
     * of `clazz.getDeclaringClass()`.
     * @throws NullPointerException     if `clazz` or one of the elements in
     * `arguments` is null.
     */
    fun parameterizedInnerClass(owner: Type?, clazz: Class<*>, vararg arguments: Type): Type {
        var arguments = arguments
        // never allow an owner on a class that doesn't have one
        if (clazz.declaringClass == null && owner != null) {
            throw IllegalArgumentException("Cannot specify an owner type for a top level class")
        }

        val realOwner = transformOwner(owner, clazz)

        if (arguments == null) {
            if (clazz.typeParameters.size == 0) {
                // no arguments known, but no needed so just use an empty argument list.
                // (we can still end up with a generic type if the owner is generic)
                arguments = arrayOfNulls(0)
            } else {
                // missing type arguments, return the raw type
                return clazz
            }
        } else {
            if (arguments.size != clazz.typeParameters.size) {
                throw IllegalArgumentException("Incorrect number of type arguments for [" + clazz + "]: " +
                    "expected " + clazz.typeParameters.size + ", but got " + arguments.size)
            }
        }

        // if the class and its owner simply have no parameters at all, this is not a parameterized type
        if (!KtGenericTypeReflector.isMissingTypeParameters(clazz)) {
            return clazz
        }

        // if the owner type is missing type parameters and clazz is non-static, this is a raw type
        if (realOwner != null && !Modifier.isStatic(clazz.modifiers)
            && KtGenericTypeReflector.isMissingTypeParameters(realOwner)) {
            return clazz
        }

        val result = ParameterizedTypeImpl(clazz, arguments, realOwner)
        checkParametersWithinBound(result)
        return result
    }

    /**
     * Check if the type arguments of the given type are within the bounds declared on the type
     * parameters. Only the type arguments of the type itself are checked, the possible owner type
     * is assumed to be valid.
     *
     * It does not follow the checks defined in the [JLS](http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.5)
     * because there are several problems with those (see http://stackoverflow.com/questions/7003009
     * for one). Instead, this applies some intuition and follows what Java compilers seem to do.
     *
     * @param type possibly inconsistent type to check.
     * @throws IllegalArgumentException if the type arguments are not within the bounds
     */
    private fun checkParametersWithinBound(type: ParameterizedType) {
        val arguments = type.actualTypeArguments
        val typeParameters = (type.rawType as Class<*>).typeParameters

        // a map of type arguments in the type, to fill in variables in the bounds
        val varMap = VarMap(type)

        // for every bound on every parameter
        for (i in arguments.indices) {
            for (bound in typeParameters[i].bounds) {
                // replace type variables in the bound by their value
                val replacedBound = varMap.map(bound)


                if (arguments[i] is WildcardType) {
                    val wildcardTypeParameter = arguments[i] as WildcardType

                    // Check if a type satisfying both the bounds of the variable and of the wildcard could exist

                    // upper bounds must not be mutually exclusive
                    for (wildcardUpperBound in wildcardTypeParameter.upperBounds) {
                        if (!couldHaveCommonSubtype(replacedBound, wildcardUpperBound)) {
                            throw TypeArgumentNotInBoundException(arguments[i], typeParameters[i], bound)
                        }
                    }
                    // a lowerbound in the wildcard must satisfy every upperbound
                    for (wildcardLowerBound in wildcardTypeParameter.lowerBounds) {
                        if (!KtGenericTypeReflector.isSuperType(replacedBound, wildcardLowerBound)) {
                            throw TypeArgumentNotInBoundException(arguments[i], typeParameters[i], bound)
                        }
                    }
                } else {
                    if (!KtGenericTypeReflector.isSuperType(replacedBound, arguments[i])) {
                        throw TypeArgumentNotInBoundException(arguments[i], typeParameters[i], bound)
                    }
                }
            }
        }
    }

    /**
     * Checks if the intersection of two types is not empty.
     */
    private fun couldHaveCommonSubtype(type1: Type, type2: Type): Boolean {
        // this is an optimistically naive implementation.
        // if they are parameterized types their parameters need to be checked,...
        // so we're just a bit too lenient here

        val erased1 = KtGenericTypeReflector.erase(type1)
        val erased2 = KtGenericTypeReflector.erase(type2)
        // if they are both classes
        if (!erased1.isInterface && !erased2.isInterface) {
            // then one needs to be a subclass of another
            if (!erased1.isAssignableFrom(erased2) && !erased2.isAssignableFrom(erased1)) {
                return false
            }
        }
        return true
    }

    /**
     * Transforms the given owner type into an appropriate one when constructing a parameterized
     * type.
     */
    private fun transformOwner(givenOwner: Type?, clazz: Class<*>): Type? {
        if (givenOwner == null) {
            // be lenient: if this is an inner class but no owner was specified, assume a raw owner type
            // (or if there is no owner just return null)
            return clazz.declaringClass
        } else {
            // If the specified owner is not of the declaring class' type, but instead a subtype,
            // transform it into the declaring class with the exact type parameters.
            // For example with "class StringOuter extends GenericOuter<String>", transform
            // "StringOuter.Inner" into "GenericOuter<String>.Inner", just like the Java compiler does.
            val transformedOwner = KtGenericTypeReflector.getExactSuperType(KtGenericTypeReflector.annotate(givenOwner).type,
                clazz.declaringClass)
                ?: // null means it's not a supertype
                throw IllegalArgumentException("Given owner type [" + givenOwner + "] is not appropriate for ["
                    + clazz + "]: it should be a subtype of " + clazz.declaringClass)

            return if (Modifier.isStatic(clazz.modifiers)) {
                // for a static inner class, the owner shouldn't have type parameters
                KtGenericTypeReflector.erase(transformedOwner)
            } else {
                transformedOwner
            }
        }
    }

    /**
     * Returns the wildcard type without bounds.
     * This is the '`?`' in for example `List<?>`.
     *
     * @return The unbound wildcard type
     */
    fun unboundWildcard(): WildcardType {
        return UNBOUND_WILDCARD
    }

    /**
     * Creates a wildcard type with arbitrary types. This can be used to make types that aren't valid in
     * the Java spec, such as ones with multiple upper and lower bounds.
     *
     * @param upperBounds Upper bounds of the wildcard
     * @param lowerBounds Lower bounds of the wildcard
     * @return A wildcard type
     */
    fun wildcardCustom(upperBounds: Array<Type>?, lowerBounds: Array<Type>?): WildcardType {
        if (upperBounds == null || lowerBounds == null) {
            throw NullPointerException()
        }
        return WildcardTypeImpl(upperBounds, lowerBounds)
    }

    /**
     * Creates a wildcard type with an upper bound.
     *
     * For example `wildcardExtends(String.class)`
     * returns the type `? extends String`.
     *
     * @param upperBound Upper bound of the wildcard
     * @return A wildcard type
     */
    fun wildcardExtends(upperBound: Type?): WildcardType {
        if (upperBound == null) {
            throw NullPointerException()
        }
        return WildcardTypeImpl(arrayOf<Type>(upperBound), arrayOf())
    }

    /**
     * Creates a wildcard type with a lower bound.
     *
     *
     * For example `wildcardSuper(String.class)` returns the type `? super String`.
     *
     * @param lowerBound Lower bound of the wildcard
     * @return A wildcard type
     */
    fun wildcardSuper(lowerBound: Type?): WildcardType {
        if (lowerBound == null) {
            throw NullPointerException()
        }
        return WildcardTypeImpl(arrayOf<Type>(Any::class.java), arrayOf<Type>(lowerBound))
    }

    /**
     * Creates a array type.
     *
     * If `componentType` is not a generic type but a [Class]
     * object, this returns the [Class] representing the non-generic array type. Otherwise,
     * returns a [GenericArrayType].
     *
     * For example:   * `arrayOf(String.class)`
     * returns `String[].class`  * `arrayOf(parameterizedClass(List.class,
     * String.class))` returns the [GenericArrayType] for `List<String>[]`
     *
     *
     * @param componentType The type of the components of the array.
     * @return An array type.
     */
    fun arrayOf(componentType: Type): Type {
        return GenericArrayTypeImpl.createArrayType(componentType)
    }

    /**
     * Creates an [AnnotatedArrayType] wrapped around an array types created by [.arrayOf]
     *
     * @param componentType The type of the components of the array.
     * @param annotations The annotations to be added to the array type itself.
     * @return An array type.
     */
    fun arrayOf(componentType: AnnotatedType, annotations: Array<Annotation>): AnnotatedArrayType {
        return AnnotatedArrayTypeImpl.createArrayType(componentType, annotations)
    }

    /**
     * Creates an instance of an annotation.
     *
     * @param annotationType The [Class] representing the type of the annotation to be created.
     * @param values A map of values to be assigned to the annotation elements.
     * @param <A> The type of the annotation.
     * @return An [Annotation] instanceof matching `annotationType`
     * @throws AnnotationFormatException Thrown if incomplete or invalid `values` are provided
    </A> */
    @Throws(AnnotationFormatException::class)
    fun <A: Annotation> annotation(annotationType: Class<A>, values: Map<String, Any>?): A {
        return Proxy.newProxyInstance(annotationType.classLoader,
            arrayOf<Class<*>>(annotationType),
            AnnotationInvocationHandler(annotationType, values ?: emptyMap())) as A
    }
}
*/
