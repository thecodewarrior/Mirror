/*
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or at <a href="http://www.apache.org/licenses/LICENSE-2">apache.org</a>.
 */

package com.teamwizardry.mirror.coretypes

/*
/**
 * Utility class for doing reflection on types.
 *
 * @author Wouter Coekaerts (wouter@coekaerts.be)
 * @author Bojan Tomic (veggen@gmail.com)
 */
object KtGenericTypeReflector {

    private val UNBOUND_WILDCARD = WildcardTypeImpl(arrayOf<Type>(Any::class.java), arrayOf())

    private val BOX_TYPES: Map<Class<*>, Class<*>>

    init {
        val boxTypes = HashMap<Class<*>, Class<*>>()
        boxTypes[Boolean::class.javaPrimitiveType!!] = Boolean::class.java
        boxTypes[Byte::class.javaPrimitiveType!!] = Byte::class.java
        boxTypes[Char::class.javaPrimitiveType!!] = Char::class.java
        boxTypes[Double::class.javaPrimitiveType!!] = Double::class.java
        boxTypes[Float::class.javaPrimitiveType!!] = Float::class.java
        boxTypes[Int::class.javaPrimitiveType!!] = Int::class.java
        boxTypes[Long::class.javaPrimitiveType!!] = Long::class.java
        boxTypes[Short::class.javaPrimitiveType!!] = Short::class.java
        boxTypes[Void.TYPE] = Void::class.java
        BOX_TYPES = Collections.unmodifiableMap(boxTypes)
    }

    /**
     * Returns the erasure of the given type.
     */
    fun erase(type: Type): Class<*> {
        if (type is Class<*>) {
            return type
        } else if (type is ParameterizedType) {
            return type.rawType as Class<*>
        } else if (type is TypeVariable<*>) {
            return if (type.bounds.size == 0)
                Any::class.java
            else
                erase(type.bounds[0])
        } else if (type is GenericArrayType) {
            return GenericArrayTypeImpl.createArrayType(erase(type.genericComponentType))
        } else if (type is WildcardType) {
            val lowerBounds = type.lowerBounds
            return erase(if (lowerBounds.size > 0) lowerBounds[0] else type.upperBounds[0])
        } else {
            throw RuntimeException("not supported: " + type.javaClass)
        }
    }

    fun box(type: Type): Type {
        val boxed = BOX_TYPES[type]
        return boxed ?: type
    }

    fun isFullyBound(type: Type): Boolean {
        if (type is Class<*>) {
            return true
        }
        if (type is ParameterizedType) {
            return type.actualTypeArguments.all { isFullyBound(it) }
        }
        return if (type is GenericArrayType) {
            isFullyBound(type.genericComponentType)
        } else false
    }

    private fun mapTypeParameters(toMapType: AnnotatedType, typeAndParams: AnnotatedType, mappingMode: VarMap.MappingMode = VarMap.MappingMode.EXACT): AnnotatedType {
        if (isMissingTypeParameters(typeAndParams.type)) {
            return AnnotatedTypeImpl(erase(toMapType.type), toMapType.annotations)
        } else {
            val varMap = VarMap()
            var handlingTypeAndParams: AnnotatedType? = typeAndParams
            while (handlingTypeAndParams is AnnotatedParameterizedType) {
                val pType = handlingTypeAndParams as AnnotatedParameterizedType?
                val clazz = (pType!!.type as ParameterizedType).rawType as Class<*> // getRawType should always be Class
                val vars = clazz.typeParameters
                varMap.addAll(vars, pType.annotatedActualTypeArguments)
                val owner = (pType.type as ParameterizedType).ownerType
                handlingTypeAndParams = if (owner == null) null else annotate(owner)
            }
            return varMap.map(toMapType, mappingMode)
        }
    }

    fun resolveExactType(unresolved: AnnotatedType, typeAndParams: AnnotatedType): AnnotatedType {
        return resolveType(unresolved, expandGenerics(typeAndParams), VarMap.MappingMode.EXACT)
    }

    fun resolveExactType(unresolved: Type, typeAndParams: Type): Type {
        return resolveType(annotate(unresolved), annotate(typeAndParams, true), VarMap.MappingMode.EXACT).type
    }

    fun resolveType(unresolved: AnnotatedType, typeAndParams: AnnotatedType): AnnotatedType {
        return resolveType(unresolved, expandGenerics(typeAndParams), VarMap.MappingMode.ALLOW_INCOMPLETE)
    }

    fun resolveType(unresolved: Type, typeAndParams: Type): Type {
        return resolveType(annotate(unresolved), annotate(typeAndParams, true), VarMap.MappingMode.ALLOW_INCOMPLETE).type
    }

    private fun resolveType(unresolved: AnnotatedType, typeAndParams: AnnotatedType, mappingMode: VarMap.MappingMode): AnnotatedType {
        if (unresolved is AnnotatedParameterizedType) {
            val params = unresolved.annotatedActualTypeArguments
                .map { p -> resolveType(p, typeAndParams, mappingMode) }
                .toTypedArray()
            return replaceParameters(unresolved, params)
        }
        if (unresolved is AnnotatedWildcardType) {
            val lower = unresolved.annotatedLowerBounds
                .map { b -> resolveType(b, typeAndParams, mappingMode) }
                .toTypedArray()
            val upper = unresolved.annotatedUpperBounds
                .map { b -> resolveType(b, typeAndParams, mappingMode) }
                .toTypedArray()
            return AnnotatedWildcardTypeImpl(unresolved.getType() as WildcardType, unresolved.getAnnotations(), lower, upper)
        }
        if (unresolved is AnnotatedTypeVariable) {
            val `var` = unresolved.getType() as TypeVariable<*>
            if (`var`.genericDeclaration is Class<*>) {

                val resolved = getTypeParameter(typeAndParams, `var`)
                if (resolved != null) {
                    return updateAnnotations(resolved, unresolved.getAnnotations())
                }
            }
            if (mappingMode == VarMap.MappingMode.ALLOW_INCOMPLETE) {
                return unresolved
            }
            throw IllegalArgumentException("Variable " + `var`.name + " is not declared by the given type "
                + typeAndParams.type.typeName + " or its super types")
        }
        if (unresolved is AnnotatedArrayType) {
            val componentType = resolveType(
                unresolved.annotatedGenericComponentType, typeAndParams, mappingMode)
            return AnnotatedArrayTypeImpl(KtTypeFactory.arrayOf(componentType.type), unresolved.getAnnotations(), componentType)
        }
        //TODO Deal with CaptureType somehow...
        return unresolved
    }

    /**
     * Checks if the given type is a class that is supposed to have type parameters, but doesn't.
     * In other words, if it's a really raw type.
     */
    fun isMissingTypeParameters(type: Type): Boolean {
        if (type is Class<*>) {
            if (Modifier.isStatic(type.modifiers)) {
                return type.typeParameters.size != 0
            }
            var enclosing: Class<*>? = type
            while (enclosing != null) {
                if (enclosing.typeParameters.size != 0)
                    return true
                enclosing = enclosing.enclosingClass
            }
            return false
        } else return if (type is ParameterizedType) {
            false
        } else {
            throw AssertionError("Unexpected type " + type.javaClass)
        }
    }

    /**
     * Returns a type representing the class, with all type parameters the unbound wildcard ("?").
     * For example, `addWildcardParameters(Map.class)` returns a type representing `Map<?,?>`.
     * @return
     *  * If clazz is a class or interface without type parameters, clazz itself is returned.
     *  * If clazz is a class or interface with type parameters, an instance of ParameterizedType is returned.
     *  * if clazz is an array type, an array type is returned with unbound wildcard parameters added in the the component type.
     *
     */
    fun addWildcardParameters(clazz: Class<*>): Type {
        if (clazz.isArray) {
            return GenericArrayTypeImpl.createArrayType(addWildcardParameters(clazz.componentType))
        } else if (isMissingTypeParameters(clazz)) {
            val vars = clazz.typeParameters
            val arguments = arrayOfNulls<Type>(vars.size)
            Arrays.fill(arguments, UNBOUND_WILDCARD)
            val owner = if (clazz.declaringClass == null) null else addWildcardParameters(clazz.declaringClass)
            return ParameterizedTypeImpl(clazz, arguments, owner)
        } else {
            return clazz
        }
    }

    /**
     * The equivalent of [.getExactSuperType] but works with [AnnotatedType]s
     *
     * @param subType The type whose supertype is to be searched for
     * @param searchSuperClass The class of the supertype to search for
     *
     * @return The annotated type representing `searchSuperClass` with type parameters from `subType`
     */
    fun getExactSuperType(subType: AnnotatedType, searchSuperClass: Class<*>): AnnotatedType? {
        if (subType is AnnotatedParameterizedType || subType.type is Class<*> || subType is AnnotatedArrayType) {
            val superClass = erase(subType.type)

            if (searchSuperClass == superClass) {
                return subType
            }

            if (!searchSuperClass.isAssignableFrom(superClass)) {
                return null
            }
        }

        for (superType in getExactDirectSuperTypes(subType)) {
            val result = getExactSuperType(superType, searchSuperClass)
            if (result != null)
                return result
        }

        return null
    }

    /**
     * Finds the most specific supertype of `subType` whose erasure is `searchSuperClass`.
     * In other words, returns a type representing the class `searchSuperClass` plus its exact type parameters in `subType`.
     *
     *
     *  * Returns an instance of [ParameterizedType] if `searchSuperClass` is a real class or interface and `subType` has parameters for it
     *  * Returns an instance of [GenericArrayType] if `searchSuperClass` is an array type, and `subType` has type parameters for it
     *  * Returns an instance of [Class] if `subType` is a raw type, or has no type parameters for `searchSuperClass`
     *  * Returns null if `searchSuperClass` is not a superclass of `subType`.
     *
     *
     *
     * For example, with `class StringList implements List<String>`, `getExactSuperType(StringList.class, Collection.class)`
     * returns a [ParameterizedType] representing `Collection<String>`.
     *
     *
     * @param subType The type whose supertype is to be searched for
     * @param searchSuperClass The class of the supertype to search for
     *
     * @return The type representing `searchSuperClass` with type parameters from `subType`
     */
    fun getExactSuperType(subType: Type, searchSuperClass: Class<*>): Type? {
        val superType = getExactSuperType(annotate(subType), searchSuperClass)
        return superType?.type
    }

    /**
     * The equivalent of [.getExactSubType] but works with [AnnotatedType]s
     *
     * @param superType The type whose subtype is to be searched for
     * @param searchSubClass The class of the subtype to search for
     *
     * @return The annotated type representing `searchSubClass` with type parameters from `superType`
     */
    fun getExactSubType(superType: AnnotatedType, searchSubClass: Class<*>): AnnotatedType? {
        var subType: Type = searchSubClass
        if (searchSubClass.typeParameters.size > 0) {
            subType = KtTypeFactory.parameterizedClass(searchSubClass, *searchSubClass.typeParameters as Array<Type>)
        }
        val annotatedSubType = annotate(subType)
        val rawSuperType = erase(superType.type)
        if (searchSubClass.isArray && superType is AnnotatedArrayType) {
            return if (rawSuperType.isAssignableFrom(searchSubClass)) {
                AnnotatedArrayTypeImpl.createArrayType(
                    getExactSubType(superType.annotatedGenericComponentType, searchSubClass.componentType),
                    emptyArray())
            } else {
                null
            }
        }
        if (searchSubClass.typeParameters.size == 0) {
            return annotatedSubType
        }
        if (superType !is AnnotatedParameterizedType) {
            return annotate(searchSubClass)
        }
        val matched = getExactSuperType(annotatedSubType, rawSuperType) as AnnotatedParameterizedType? ?: return null
        val varMap = VarMap()
        for (i in 0 until superType.annotatedActualTypeArguments.size) {
            val `var` = matched.annotatedActualTypeArguments[i].type
            if (`var` is TypeVariable<*> && `var`.genericDeclaration === searchSubClass) {
                varMap.add(`var`, superType.annotatedActualTypeArguments[i])
            }
        }
        try {
            return varMap.map(annotatedSubType)
        } catch (e: UnresolvedTypeVariableException) {
            return annotate(searchSubClass)
        }
    }

    /**
     * Finds the most specific subtype of `superType` whose erasure is `searchSubClass`.
     * In other words, returns a type representing the class `searchSubClass` plus its exact type parameters in `superType`,
     * if they are possible to resolve.
     *
     *
     *  * Returns an instance of [AnnotatedParameterizedType] if `searchSubClass` is a real class or interface and `superType` has parameters for it
     *  * Returns an instance of [AnnotatedArrayType] if `searchSubClass` is an array type, and `superType` has type parameters for it
     *  * Returns an instance of [AnnotatedType] if `superType` is a raw type, or has no type parameters for `searchSubClass`
     *  * Returns null if `searchSubClass` is not a subclass of `superType`.
     *
     *
     *
     * For example, with `getExactSubType(new TypeToken<List<String>>(){}.getAnnotatedType(), ArrayList.class)`
     * returns a [AnnotatedParameterizedType] representing `ArrayList<String>`.
     *
     */
    fun getExactSubType(superType: Type, searchSubClass: Class<*>): Type? {
        val resolvedSubtype = getExactSubType(annotate(superType), searchSubClass)
        return resolvedSubtype?.type
    }

    /**
     * Gets the type parameter for a given type that is the value for a given type variable.
     * For example, with `class StringList implements List<String>`,
     * `getTypeParameter(StringList.class, Collection.class.getTypeParameters()[0])`
     * returns `String`.
     *
     * @param type The type to inspect.
     * @param variable The type variable to find the value for.
     * @return The type parameter for the given variable. Or null if type is not a subtype of the
     * type that declares the variable, or if the variable isn't known (because of raw types).
     */
    fun getTypeParameter(type: AnnotatedType, variable: TypeVariable<out Class<*>>): AnnotatedType? {
        val clazz = variable.genericDeclaration
        val superType = getExactSuperType(type, clazz)
        if (superType is AnnotatedParameterizedType) {
            val index = Arrays.asList<TypeVariable<Class<*>>>(*clazz.getTypeParameters()).indexOf(variable)
            val resolvedVarType = superType.annotatedActualTypeArguments[index]
            return updateAnnotations(resolvedVarType, variable.annotations)
        } else {
            return null
        }
    }

    fun getTypeParameter(type: Type, variable: TypeVariable<out Class<*>>): Type? {
        val typeParameter = getTypeParameter(annotate(type), variable)
        return typeParameter?.type
    }

    /**
     * Checks if the capture of subType is a subtype of superType
     */
    fun isSuperType(superType: Type, subType: Type): Boolean {
        if (superType is ParameterizedType || superType is Class<*> || superType is GenericArrayType) {
            val superClass = erase(superType)
            val annotatedMappedSubType = getExactSuperType(capture(annotate(subType)), superClass)
            val mappedSubType = annotatedMappedSubType?.type
            if (mappedSubType == null) {
                return false
            } else if (superType is Class<*>) {
                return true
            } else if (mappedSubType is Class<*>) {
                // TODO treat supertype by being raw type differently ("supertype, but with warnings")
                return true // class has no parameters, or it's a raw type
            } else if (mappedSubType is GenericArrayType) {
                val superComponentType = getArrayComponentType(superType)!!
                val mappedSubComponentType = getArrayComponentType(mappedSubType)!!
                return isSuperType(superComponentType, mappedSubComponentType)
            } else {
                assert(mappedSubType is ParameterizedType)
                assert(superType is ParameterizedType)
                val pMappedSubType = mappedSubType as ParameterizedType?
                assert(pMappedSubType!!.rawType === superClass)
                val pSuperType = superType as ParameterizedType

                val superTypeArgs = pSuperType.actualTypeArguments
                val subTypeArgs = pMappedSubType!!.actualTypeArguments
                assert(superTypeArgs.size == subTypeArgs.size)
                for (i in superTypeArgs.indices) {
                    if (!contains(superTypeArgs[i], subTypeArgs[i])) {
                        return false
                    }
                }
                // params of the class itself match, so if the owner types are supertypes too, it's a supertype.
                return pSuperType.ownerType == null || isSuperType(pSuperType.ownerType, pMappedSubType.ownerType)
            }
        } else if (superType is CaptureType) {
            if (superType == subType)
                return true
            for (lowerBound in superType.lowerBounds) {
                if (isSuperType(lowerBound, subType)) {
                    return true
                }
            }
            return false
            //		} else if (superType instanceof GenericArrayType) {
            //			return isArraySupertype(superType, subType);
        } else {
            throw RuntimeException("Type not supported: " + superType.javaClass)
        }
    }

    private fun isArraySupertype(arraySuperType: Type, subType: Type): Boolean {
        val superTypeComponent = getArrayComponentType(arraySuperType)!!
        val subTypeComponent = getArrayComponentType(subType)
        return subTypeComponent?.let { isSuperType(superTypeComponent, it) } ?: false
    }

    /**
     * If type is an array type, returns the annotated type of the component of the array.
     * Otherwise, returns null.
     */
    fun getArrayComponentType(type: AnnotatedType): AnnotatedType? {
        if (type.type is Class<*>) {
            val clazz = type.type as Class<*>
            return AnnotatedTypeImpl(clazz.componentType, emptyArray())
        } else return if (type is AnnotatedArrayType) {
            type.annotatedGenericComponentType
        } else {
            null
        }
    }

    /**
     * If type is an array type, returns the type of the component of the array.
     * Otherwise, returns null.
     */
    fun getArrayComponentType(type: Type): Type? {
        val componentType = getArrayComponentType(annotate(type))
        return componentType?.type
    }

    private fun contains(containingType: Type, containedType: Type): Boolean {
        if (containingType is WildcardType) {
            for (upperBound in containingType.upperBounds) {
                if (!isSuperType(upperBound, containedType)) {
                    return false
                }
            }
            for (lowerBound in containingType.lowerBounds) {
                if (!isSuperType(containedType, lowerBound)) {
                    return false
                }
            }
            return true
        } else {
            return containingType == containedType
        }
    }

    /**
     * Returns the direct supertypes of the given type. Resolves type parameters.
     */
    private fun getExactDirectSuperTypes(type: AnnotatedType?): Array<AnnotatedType> {
        if (type is AnnotatedParameterizedType || type != null && type.type is Class<*>) {
            val clazz: Class<*>
            if (type is AnnotatedParameterizedType) {
                clazz = (type.type as ParameterizedType).rawType as Class<*>
            } else {
                // TODO primitive types?
                clazz = type.type as Class<*>
                if (clazz.isArray)
                    return getArrayExactDirectSuperTypes(annotate(clazz))
            }

            val superInterfaces = clazz.annotatedInterfaces
            val superClass = clazz.annotatedSuperclass

            // the only supertype of an interface without superinterfaces is Object
            if (superClass == null && superInterfaces.size == 0 && clazz.isInterface) {
                return arrayOf(AnnotatedTypeImpl(Any::class.java))
            }

            val result: Array<AnnotatedType>
            var resultIndex: Int
            if (superClass == null) {
                result = arrayOfNulls(superInterfaces.size)
                resultIndex = 0
            } else {
                result = arrayOfNulls(superInterfaces.size + 1)
                resultIndex = 1
                result[0] = mapTypeParameters(superClass, type)
            }
            for (superInterface in superInterfaces) {
                result[resultIndex++] = mapTypeParameters(superInterface, type)
            }

            return result
        } else if (type is AnnotatedTypeVariable) {
            val tv = type as AnnotatedTypeVariable?
            return tv!!.annotatedBounds
        } else if (type is AnnotatedWildcardType) {
            // This should be a rare case: normally this wildcard is already captured.
            // But it does happen if the upper bound of a type variable contains a wildcard
            // TODO shouldn't upper bound of type variable have been captured too? (making this case impossible?)
            return type.annotatedUpperBounds
        } else if (type is AnnotatedCaptureTypeImpl) {
            return type.annotatedUpperBounds
        } else if (type is AnnotatedArrayType) {
            return getArrayExactDirectSuperTypes(type)
        } else if (type == null) {
            throw NullPointerException()
        } else {
            throw RuntimeException("not implemented type: $type")
        }
    }

    private fun getArrayExactDirectSuperTypes(arrayType: AnnotatedType): Array<AnnotatedType> {
        // see http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.10.3
        val typeComponent = getArrayComponentType(arrayType)

        val result: Array<AnnotatedType>
        var resultIndex: Int
        if (typeComponent != null && typeComponent.type is Class<*> && (typeComponent.type as Class<*>).isPrimitive) {
            resultIndex = 0
            result = arrayOfNulls(3)
        } else {
            val componentSupertypes = getExactDirectSuperTypes(typeComponent)
            result = arrayOfNulls(componentSupertypes.size + 3)
            resultIndex = 0
            while (resultIndex < componentSupertypes.size) {
                result[resultIndex] = AnnotatedArrayTypeImpl.createArrayType(componentSupertypes[resultIndex], emptyArray())
                resultIndex++
            }
        }
        result[resultIndex++] = AnnotatedTypeImpl(Any::class.java)
        result[resultIndex++] = AnnotatedTypeImpl(Cloneable::class.java)
        result[resultIndex++] = AnnotatedTypeImpl(Serializable::class.java)
        return result
    }

    /**
     * Resolves the exact return type of the given method in the given type.
     * This may be different from `m.getAnnotatedReturnType()` when the method was declared in a superclass,
     * or `declaringType` has a type parameter that is used in the return type, or `declaringType` is a raw type.
     */
    fun getExactReturnType(m: Method, declaringType: AnnotatedType): AnnotatedType {
        return getReturnType(m, declaringType, VarMap.MappingMode.EXACT)
    }

    /**
     * Resolves the exact return type of the given method in the given type.
     * This may be different from `m.getGenericReturnType()` when the method was declared in a superclass,
     * or `declaringType` has a type parameter that is used in the return type, or `declaringType` is a raw type.
     */
    fun getExactReturnType(m: Method, declaringType: Type): Type {
        return getExactReturnType(m, annotate(declaringType)).type
    }

    /**
     * Resolves the return type of the given method in the given type. Any unresolvable variables will be kept
     * (in contrast to [.getExactReturnType].
     * This may be different from `m.getAnnotatedReturnType()` when the method was declared in a superclass,
     * or `declaringType` has a type parameter that is used in the return type, or `declaringType` is a raw type.
     */
    fun getReturnType(m: Method, declaringType: AnnotatedType): AnnotatedType {
        return getReturnType(m, declaringType, VarMap.MappingMode.ALLOW_INCOMPLETE)
    }

    /**
     * Resolves the return type of the given method in the given type. Any unresolvable variables will be kept
     * (in contrast to [.getExactReturnType].
     * This may be different from `m.getGenericReturnType()` when the method was declared in a superclass,
     * or `declaringType` has a type parameter that is used in the return type, or `declaringType` is a raw type.
     */
    fun getReturnType(m: Method, declaringType: Type): Type {
        return getReturnType(m, annotate(declaringType)).type
    }

    private fun getReturnType(m: Method, declaringType: AnnotatedType, mappingMode: VarMap.MappingMode): AnnotatedType {
        val returnType = m.annotatedReturnType
        val exactDeclaringType = getExactSuperType(capture(declaringType), m.declaringClass)
            ?: // capture(type) is not a subtype of m.getDeclaringClass()
            throw IllegalArgumentException("The method $m is not a member of type $declaringType")
        return mapTypeParameters(returnType, exactDeclaringType, mappingMode)
    }

    /**
     * Resolves the exact type of the given field in the given type.
     * This may be different from `f.getAnnotatedType()` when the field was declared in a superclass,
     * or `declaringType` has a type parameter that is used in the type of the field, or `declaringType` is a raw type.
     */
    fun getExactFieldType(f: Field, declaringType: AnnotatedType): AnnotatedType {
        return getFieldType(f, declaringType, VarMap.MappingMode.EXACT)
    }

    /**
     * Resolves the exact type of the given field in the given type.
     * This may be different from `f.getGenericType()` when the field was declared in a superclass,
     * or `declaringType` has a type parameter that is used in the type of the field, or `declaringType` is a raw type.
     */
    fun getExactFieldType(f: Field, type: Type): Type {
        return getExactFieldType(f, annotate(type)).type
    }

    /**
     * Resolves the type of the given field in the given type. Any unresolvable variables will be kept
     * (in contrast to [.getExactFieldType]).
     * This may be different from `f.getAnnotatedType()` when the field was declared in a superclass,
     * or `declaringType` has a type parameter that is used in the type of the field, or `declaringType` is a raw type.
     */
    fun getFieldType(f: Field, declaringType: AnnotatedType): AnnotatedType {
        return getFieldType(f, declaringType, VarMap.MappingMode.ALLOW_INCOMPLETE)
    }

    /**
     * Resolves the type of the given field in the given type. Any unresolvable variables will be kept
     * (in contrast to [.getExactFieldType]).
     * This may be different from `f.getGenericType()` when the field was declared in a superclass,
     * or `declaringType` has a type parameter that is used in the type of the field, or `declaringType` is a raw type.
     */
    fun getFieldType(f: Field, type: Type): Type {
        return getFieldType(f, annotate(type)).type
    }

    private fun getFieldType(f: Field, declaringType: AnnotatedType, mappingMode: VarMap.MappingMode): AnnotatedType {
        val returnType = f.annotatedType
        val exactDeclaringType = getExactSuperType(capture(declaringType), f.declaringClass)
            ?: // capture(type) is not a subtype of f.getDeclaringClass()
            throw IllegalArgumentException("The field $f is not a member of type $declaringType")
        return mapTypeParameters(returnType, exactDeclaringType, mappingMode)
    }

    /**
     * Resolves the exact annotated parameter types of the given method/constructor in the given type.
     * This may be different from `exe.getAnnotatedParameterTypes()` when the method was declared in a superclass,
     * or `declaringType` has a type parameter that is used in one of the parameters, or `declaringType` is a raw type.
     */
    fun getExactParameterTypes(exe: Executable, declaringType: AnnotatedType): Array<AnnotatedType> {
        return getParameterTypes(exe, declaringType, VarMap.MappingMode.EXACT)
    }

    /**
     * Resolves the exact parameter types of the given method/constructor in the given type.
     * This may be different from `exe.getParameterTypes()` when the method was declared in a superclass,
     * or `declaringType` has a type parameter that is used in one of the parameters, or `declaringType` is a raw type.
     */
    fun getExactParameterTypes(exe: Executable, declaringType: Type): Array<Type> {
        return getExactParameterTypes(exe, annotate(declaringType)).map { it.type }.toTypedArray()
    }

    fun getParameterTypes(exe: Executable, declaringType: AnnotatedType): Array<AnnotatedType> {
        return getParameterTypes(exe, declaringType, VarMap.MappingMode.ALLOW_INCOMPLETE)
    }

    fun getParameterTypes(exe: Executable, declaringType: Type): Array<Type> {
        return getParameterTypes(exe, annotate(declaringType)).map { it.type }.toTypedArray()
    }

    private fun getParameterTypes(exe: Executable, declaringType: AnnotatedType, mappingMode: VarMap.MappingMode): Array<AnnotatedType> {
        val parameterTypes = exe.annotatedParameterTypes
        val exactDeclaringType = getExactSuperType(capture(declaringType), exe.declaringClass)
            ?: // capture(type) is not a subtype of exe.getDeclaringClass()
            throw IllegalArgumentException("The method/constructor $exe is not a member of type $declaringType")

        val result = arrayOfNulls<AnnotatedType>(parameterTypes.size)
        for (i in parameterTypes.indices) {
            result[i] = mapTypeParameters(parameterTypes[i], exactDeclaringType, mappingMode)
        }
        return result
    }

    /**
     * Applies capture conversion to the given type.
     */
    fun capture(type: AnnotatedType): AnnotatedType {
        return (type as? AnnotatedParameterizedType)?.let { capture(it) } ?: type
    }
    //
    /**
     * Applies capture conversion to the given type.
     * @see .capture
     */
    fun capture(type: AnnotatedParameterizedType): AnnotatedParameterizedType {
        // the map from parameters to their captured equivalent

        val varMap = VarMap()
        // list of CaptureTypes we've created but aren't fully initialized yet
        // we can only initialize them *after* we've fully populated varMap
        val toInit = ArrayList<AnnotatedCaptureTypeImpl>()

        val clazz = (type.type as ParameterizedType).rawType as Class<*>
        val arguments = type.annotatedActualTypeArguments
        val vars = clazz.typeParameters
        val capturedArguments = arrayOfNulls<AnnotatedType>(arguments.size)

        assert(arguments.size == vars.size) // NICE throw an explaining exception

        for (i in arguments.indices) {
            var argument = arguments[i]
            if (argument is AnnotatedWildcardType) {
                val captured = AnnotatedCaptureTypeImpl(argument, AnnotatedTypeVariableImpl(vars[i]))
                argument = captured
                toInit.add(captured)
            }
            capturedArguments[i] = argument
            varMap.add(vars[i], argument)
        }
        for (captured in toInit) {
            captured.init(varMap)
        }
        val inner = type.type as ParameterizedType
        val ownerType = if (inner.ownerType == null) null else capture(annotate(inner.ownerType))
        val rawArgs = stream<AnnotatedType>(capturedArguments).map { it.type }.toTypedArray()
        val nn = ParameterizedTypeImpl(clazz, rawArgs, ownerType?.type)
        return AnnotatedParameterizedTypeImpl(nn, type.annotations, capturedArguments)
    }

    /**
     * Returns the display name of a Type.
     */
    fun getTypeName(type: Type): String {
        return if (type is Class<*>) {
            if (type.isArray) getTypeName(type.componentType) + "[]" else type.name
        } else {
            type.toString()
        }
    }

    /**
     * Returns list of classes and interfaces that are supertypes of the given type.
     * For example given this class:
     * `class { Foo<A extends Number & Iterable<A>, B extends A>}`<br></br>
     * calling this method on type parameters `B` (`Foo.class.getTypeParameters()[1]`)
     * returns a list containing `Number` and `Iterable`.
     *
     *
     * This is mostly useful if you get a type from one of the other methods in `GenericTypeReflector`,
     * but you don't want to deal with all the different sorts of types,
     * and you are only really interested in concrete classes and interfaces.
     *
     *
     * @return A List of classes, each of them a supertype of the given type.
     * If the given type is a class or interface itself, returns a List with just the given type.
     * The list contains no duplicates, and is ordered in the order the upper bounds are defined on the type.
     */
    fun getUpperBoundClassAndInterfaces(type: Type): List<Class<*>> {
        val result = LinkedHashSet<Class<*>>()
        buildUpperBoundClassAndInterfaces(type, result)
        return ArrayList(result)
    }

    /**
     * Recursively wraps a [Type] into an [AnnotatedType] using the annotations found on
     * the erasure classes.
     *
     * @param type Type to annotate
     * @return Type whose structure has been recursively annotated
     */
    fun annotate(type: Type): AnnotatedType {
        return annotate(type, false)
    }

    /**
     * Recursively wraps a [Type] into an [AnnotatedType] using the annotations found on
     * the erasure classes, plus adding the provided annotations to the top level [Type] only.
     *
     * @param type Type to annotate
     * @return Type whose structure has been recursively annotated, plus the provided annotation added
     * at the top level
     *
     *
     * See [.annotate]
     */
    fun annotate(type: Type, annotations: Array<Annotation>): AnnotatedType {
        return updateAnnotations(annotate(type), annotations)
    }

    /**
     * This is the method underlying both [.annotate] and [.annotate].
     * If goes recursively through the structure of the provided [Type] wrapping all type parameters,
     * bounds etc encountered into [AnnotatedType]s using annotations found directly on the
     * corresponding erasure class, with a special treatment for [CaptureType] which can have
     * infinitely recursive structure by having itself as its upper bound.
     *
     * @param type The type to annotate
     * @param cache The cache for already encountered [CaptureType]s. Necessary because
     * [CaptureType]s can have infinitely recursive structure.
     *
     * @return Type whose structure has been recursively annotated
     *
     *
     * See [.annotate]
     *
     * See [.annotate]
     *
     * See [CaptureCacheKey]
     *
     * See [CaptureType]
     */
    private fun annotate(type: Type, expandGenerics: Boolean, cache: MutableMap<CaptureCacheKey, AnnotatedType> = HashMap()): AnnotatedType {
        if (type is ParameterizedType) {
            val params = arrayOfNulls<AnnotatedType>(type.actualTypeArguments.size)
            for (i in params.indices) {
                params[i] = annotate(type.actualTypeArguments[i], expandGenerics, cache)
            }
            return AnnotatedParameterizedTypeImpl(type, emptyArray(), params)
        }
        if (type is CaptureType) {
            val key = CaptureCacheKey(type)
            if (cache.containsKey(key)) {
                return cache[key]
            }
            val annotatedCapture = AnnotatedCaptureTypeImpl(type,
                annotate(type.wildcardType, expandGenerics, cache) as AnnotatedWildcardType,
                annotate(type.typeVariable, expandGenerics, cache) as AnnotatedTypeVariable)

            cache[CaptureCacheKey(type)] = annotatedCapture
            val upperBounds = type.upperBounds
                .map { bound -> annotate(bound, expandGenerics, cache) }
                .toTypedArray()
            annotatedCapture.annotatedUpperBounds = upperBounds //complete the type
            return annotatedCapture
        }
        if (type is WildcardType) {
            val lowerBounds = type.lowerBounds
                .map { bound -> annotate(bound, expandGenerics, cache) }
                .toTypedArray()
            val upperBounds = type.upperBounds
                .map { bound -> annotate(bound, expandGenerics, cache) }
                .toTypedArray()
            return AnnotatedWildcardTypeImpl(type, emptyArray(), lowerBounds, upperBounds)
        }
        if (type is TypeVariable<*>) {
            return AnnotatedTypeVariableImpl(type)
        }
        if (type is GenericArrayType) {
            return AnnotatedArrayTypeImpl(type, emptyArray(), annotate(type.genericComponentType, expandGenerics, cache))
        }
        if (type is Class<*>) {
            if (type.isArray) {
                val componentClass = type.componentType
                return AnnotatedArrayTypeImpl.createArrayType(
                    AnnotatedTypeImpl(componentClass, emptyArray()), emptyArray())
            }
            return if (type.typeParameters.size > 0 && expandGenerics) {
                expandClassGenerics(type)
            } else AnnotatedTypeImpl(type, emptyArray())
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

    /**
     * Creates a new [AnnotatedType] of the same structure as the original, but with its annotations
     * replaced with the provided ones.
     *
     * @param original The type whose structure is to be copied
     * @param annotations Extra annotations to be added on top of the ones found on the `original`
     *
     * @return A type of the same structure as the original but with replaced annotations
     */
    fun <T: AnnotatedType> updateAnnotations(original: T, annotations: Array<Annotation>?): T {
        return if (annotations == null || annotations.size == 0 || Arrays.equals(original.annotations, annotations)) {
            original
        } else replaceAnnotations(original, merge(original.annotations, annotations))
    }

    fun <T: AnnotatedType> mergeAnnotations(t1: T, t2: T): T {
        val merged = merge(t1.annotations, t2.annotations)
        if (t1 is AnnotatedParameterizedType) {
            val p1 = (t1 as AnnotatedParameterizedType).annotatedActualTypeArguments
            val p2 = (t2 as AnnotatedParameterizedType).annotatedActualTypeArguments
            val params = arrayOfNulls<AnnotatedType>(p1.size)
            for (i in p1.indices) {
                params[i] = mergeAnnotations(p1[i], p2[i])
            }
            return AnnotatedParameterizedTypeImpl(t1.getType() as ParameterizedType, merged, params) as T
        }
        if (t1 is AnnotatedWildcardType) {
            val l1 = (t1 as AnnotatedWildcardType).annotatedLowerBounds
            val l2 = (t2 as AnnotatedWildcardType).annotatedLowerBounds
            val lowerBounds = arrayOfNulls<AnnotatedType>(l1.size)
            for (i in l1.indices) {
                lowerBounds[i] = mergeAnnotations(l1[i], l2[i])
            }
            val u1 = (t1 as AnnotatedWildcardType).annotatedUpperBounds
            val u2 = (t2 as AnnotatedWildcardType).annotatedUpperBounds
            val upperBounds = arrayOfNulls<AnnotatedType>(u1.size)
            for (i in u1.indices) {
                upperBounds[i] = mergeAnnotations(u1[i], u2[i])
            }
            return AnnotatedWildcardTypeImpl(t1.getType() as WildcardType, merged, lowerBounds, upperBounds) as T
        }
        if (t1 is AnnotatedTypeVariable) {
            return AnnotatedTypeVariableImpl(t1.getType() as TypeVariable<*>, merged) as T
        }
        if (t1 is AnnotatedArrayType) {
            val componentType = mergeAnnotations(
                (t1 as AnnotatedArrayType).annotatedGenericComponentType,
                (t2 as AnnotatedArrayType).annotatedGenericComponentType)
            return AnnotatedArrayTypeImpl(t1.getType(), merged, componentType) as T
        }
        return AnnotatedTypeImpl(t1.type, merged) as T
    }

    /**
     * Creates a new [AnnotatedParameterizedType] of the same raw class as the provided `type`
     * by with all of its type parameters replaced by `typeParameters`.
     * @param type The original parameterized type from which the raw class is to be taken
     * @param typeParameters The new type parameters to use
     *
     * @return The new parameterized type
     */
    fun replaceParameters(type: AnnotatedParameterizedType, typeParameters: Array<AnnotatedType>): AnnotatedParameterizedType {
        val rawArguments = typeParameters.map { it.type }.toTypedArray()
        val inner = type.type as ParameterizedType
        val rawType = KtTypeFactory.parameterizedInnerClass(inner.ownerType, erase(inner), *rawArguments) as ParameterizedType
        return AnnotatedParameterizedTypeImpl(rawType, type.annotations, typeParameters)
    }

    /**
     * Returns an [AnnotatedType] functionally identical to the given one, but in a canonical form that
     * implements `equals` and `hashCode`.
     *
     * @param type The type to turn into the canonical form
     *
     * @return A type functionally equivalent to the given one, but in the canonical form
     */
    fun <T: AnnotatedType> toCanonical(type: T): T {
        return toCanonical(type, Function.identity(), HashMap())
    }

    /**
     * Returns an [AnnotatedType] functionally identical to the given one, but in a canonical form that
     * implements `equals` and `hashCode` and has all the primitives replaced by their boxed form.
     *
     * @param type The type to turn into the canonical form
     *
     * @return A type functionally equivalent to the given one, but in the canonical form
     */
    fun <T: AnnotatedType> toCanonicalBoxed(type: T): T {
        return toCanonical(type, Function { box(it) }, HashMap())
    }

    /**
     * This is the method underlying [.toCanonical].
     * If goes recursively through the structure of the provided [AnnotatedType] turning all type parameters,
     * bounds etc encountered into their canonical forms, with a special treatment for [AnnotatedCaptureType]
     * which can have infinitely recursive structure by having itself as its upper bound.
     *
     * @param type The type to annotate
     * @param leafTransformer The transformer function to apply to leaf types (e.g. to box primitives)
     * @param cache The cache for already encountered [AnnotatedCaptureType]s. Necessary because
     * [AnnotatedCaptureType]s can have infinitely recursive structure.
     *
     * @return Type whose structure has been recursively annotated
     *
     *
     * See [.toCanonical]
     *
     * See [AnnotatedCaptureCacheKey]
     *
     * See [AnnotatedCaptureType]
     *
     * See [CaptureType]
     */
    private fun <T: AnnotatedType> toCanonical(type: T, leafTransformer: Function<Type, Type>, cache: Map<AnnotatedCaptureCacheKey, AnnotatedType>): T {
        return transform(type, object: TypeVisitor() {
            override fun visitClass(type: AnnotatedType): AnnotatedType {
                return AnnotatedTypeImpl(leafTransformer.apply(type.type), type.annotations)
            }

            override fun visitArray(type: AnnotatedArrayType): AnnotatedType {
                return AnnotatedArrayTypeImpl(leafTransformer.apply(type.type), type.annotations,
                    transform(type.annotatedGenericComponentType, this))
            }
        }) as T
    }

    private fun expandGenerics(type: AnnotatedType): AnnotatedType {
        return transform(type, object: TypeVisitor() {
            public override fun visitClass(type: AnnotatedType): AnnotatedType {
                val clazz = type.type as Class<*>
                return if (clazz.typeParameters.size > 0) {
                    expandClassGenerics(clazz)
                } else type
            }
        })
    }

    fun transform(type: AnnotatedType, visitor: TypeVisitor): AnnotatedType {
        if (type is AnnotatedParameterizedType) {
            return visitor.visitParameterizedType(type)
        }
        if (type is AnnotatedWildcardType) {
            return visitor.visitWildcardType(type)
        }
        if (type is AnnotatedTypeVariable) {
            return visitor.visitVariable(type)
        }
        if (type is AnnotatedArrayType) {
            return visitor.visitArray(type)
        }
        if (type is AnnotatedCaptureType) {
            return visitor.visitCaptureType(type)
        }
        return if (type.type is Class<*>) {
            visitor.visitClass(type)
        } else visitor.visitUnmatched(type)
    }

    private fun expandClassGenerics(type: Class<*>): AnnotatedParameterizedType {
        val inner = ParameterizedTypeImpl(type, type.typeParameters, type.declaringClass)
        val params = type.typeParameters.map { annotate(it) }.toTypedArray()
        return AnnotatedParameterizedTypeImpl(inner, emptyArray(), params)
    }

    /**
     * Merges an arbitrary number of annotations arrays, and removes duplicates.
     *
     * @param annotations Annotation arrays to merge and deduplicate
     *
     * @return An array containing all annotations from the given arrays, without duplicates
     */
    fun merge(vararg annotations: Array<Annotation>): Array<Annotation> {
        return annotations.reduce { acc, arr -> Stream.concat(acc, arr).distinct().toTypedArray() }
            .orElse(emptyArray())
    }

    internal fun typeArraysEqual(t1: Array<AnnotatedType>?, t2: Array<AnnotatedType>?): Boolean {
        if (t1 == t2) return true
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

    fun hashCode(vararg types: AnnotatedType): Int {
        val typeHash = Arrays.types
            .mapToInt { t -> t.type.hashCode() }
            .reduce(0) { x, y -> 127 * x xor y }
        val annotationHash = hashCode(Arrays.types
            .flatMap { t -> Arrays.t.annotations })
        return 31 * typeHash xor annotationHash
    }

    internal fun hashCode(annotations: Stream<Annotation>): Int {
        return annotations
            .mapToInt { a -> 31 * a.annotationType().hashCode() xor a.hashCode() }
            .reduce(0) { x, y -> 127 * x xor y }
    }

    /**
     * Checks whether the two provided types are of the same structure and annotations on all levels.
     *
     * @param t1 The first type to be compared
     * @param t2 The second type to be compared
     *
     * @return True if both types have the same structure and annotations on all levels
     */
    fun equals(t1: AnnotatedType, t2: AnnotatedType): Boolean {
        var t1 = t1
        var t2 = t2
        Objects.requireNonNull(t1)
        Objects.requireNonNull(t2)
        t1 = t1 as? AnnotatedTypeImpl ?: toCanonical(t1)
        t2 = t2 as? AnnotatedTypeImpl ?: toCanonical(t2)

        return t1 == t2
    }

    /**
     * Helper method for getUpperBoundClassAndInterfaces, adding the result to the given set.
     */
    private fun buildUpperBoundClassAndInterfaces(type: Type, result: MutableSet<Class<*>>) {
        if (type is ParameterizedType || type is Class<*>) {
            result.add(erase(type))
            return
        }

        for (superType in getExactDirectSuperTypes(annotate(type))) {
            buildUpperBoundClassAndInterfaces(superType.type, result)
        }
    }

    /**
     * A key representing a [CaptureType]. Used for caching incomplete [CaptureType]s
     * while recursively inspecting their structure. Necessary because [CaptureType] can have
     * infinitely recursive structure.
     *
     *
     * See [.annotate]
     */
    internal class CaptureCacheKey(var capture: CaptureType) {

        override fun hashCode(): Int {
            return 127 * capture.wildcardType.hashCode() xor capture.typeVariable.hashCode()
        }

        override fun equals(obj: Any?): Boolean {
            if (this === obj) return true
            if (obj !is CaptureCacheKey) return false

            val that = obj.capture
            return this.capture === that || (capture.wildcardType == that.wildcardType
                && capture.typeVariable == that.typeVariable
                && Arrays.equals(capture.upperBounds, that.upperBounds))
        }
    }

    private class AnnotatedCaptureCacheKey internal constructor(internal var capture: AnnotatedCaptureType) {
        internal var raw: CaptureType

        init {
            this.raw = capture.type as CaptureType
        }

        override fun hashCode(): Int {
            return 127 * raw.wildcardType.hashCode() xor raw.typeVariable.hashCode() xor KtGenericTypeReflector.hashCode(Arrays.capture.annotations)
        }

        override fun equals(obj: Any?): Boolean {
            if (this === obj) return true
            if (obj !is AnnotatedCaptureCacheKey) return false

            val that = obj as AnnotatedCaptureCacheKey?
            return this.capture === that.capture || CaptureCacheKey(raw) == CaptureCacheKey(that.raw) && Arrays.equals(capture.annotations, that.capture.annotations)
        }
    }
}
/**
 * Maps type parameters in a type to their values.
 * @param toMapType Type possibly containing type arguments
 * @param typeAndParams must be either ParameterizedType, or (in case there are no type arguments, or it's a raw type) Class
 * @return toMapType, but with type parameters from typeAndParams replaced.
 */

*/
