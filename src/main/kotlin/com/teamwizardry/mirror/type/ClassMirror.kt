package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.coretypes.CoreTypeUtils
import com.teamwizardry.mirror.coretypes.TypeImplAccess
import com.teamwizardry.mirror.member.ConstructorMirror
import com.teamwizardry.mirror.member.ExecutableMirror
import com.teamwizardry.mirror.member.FieldMirror
import com.teamwizardry.mirror.member.JvmModifier
import com.teamwizardry.mirror.member.MethodMirror
import com.teamwizardry.mirror.member.Modifier
import com.teamwizardry.mirror.utils.checkedCast
import com.teamwizardry.mirror.utils.uniqueBy
import com.teamwizardry.mirror.utils.unmodifiableView
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility



class ClassMirror internal constructor(
    override val cache: MirrorCache,
    override val java: Class<*>,
    raw: ClassMirror?,
    override val specialization: TypeSpecialization.Class?
): ConcreteTypeMirror() {
    override val coreType: Type
    override val coreAnnotatedType: AnnotatedType

    init {
        val specialization = specialization
        if(specialization?.arguments != null && specialization.arguments.size != java.typeParameters.size)
            throw InvalidSpecializationException("Invalid number of type arguments for ClassMirror ${raw ?: this}. " +
                "Expected ${java.typeParameters.size}, received ${specialization.arguments.size}")
        if(specialization?.enclosingClass != null && specialization.enclosingClass.java != java.enclosingClass)
            throw InvalidSpecializationException("Invalid enclosing class ${specialization.enclosingClass} " +
                "for type $java. Expected enclosing class is ${java.enclosingClass}")
        val enclosingExecutable = java.enclosingMethod ?: java.enclosingConstructor
        if(specialization?.enclosingExecutable != null && specialization.enclosingExecutable.java != enclosingExecutable) {
            throw InvalidSpecializationException("Invalid enclosing executable ${specialization.enclosingExecutable} " +
                "for type $java. Expected enclosing executable is $enclosingExecutable")
        }

        val params = specialization?.arguments
        val owner = specialization?.enclosingClass
        coreType = when {
            params != null ->
                TypeImplAccess.createParameterizedTypeImpl(
                    java,
                    params.map { it.coreType }.toTypedArray(),
                    owner?.coreType
                )
            owner != null ->
                @Suppress("UNCHECKED_CAST")
                TypeImplAccess.createParameterizedTypeImpl(java, java.typeParameters as Array<Type>, owner.coreType)
            else ->
                java
        }

        if(coreType is ParameterizedType && params != null) {
            coreAnnotatedType = TypeImplAccess.createAnnotatedParameterizedTypeImpl(
                coreType,
                typeAnnotations.toTypedArray(),
                params.map { it.coreAnnotatedType }.toTypedArray()
            )
        } else {
            coreAnnotatedType = CoreTypeUtils.annotate(coreType, typeAnnotations.toTypedArray())
        }
    }

//region Data
    /**
     * The raw, unspecialized version of this mirror.
     */
    override val raw: ClassMirror = raw ?: this

    /**
     * The supertype of this class. This property is `null` if this reflect represents [Object], an interface,
     * a primitive, or `void`. The returned type will be specialized based on this type's specialization and any
     * explicit parameters set in the source code.
     */
    val superclass: ClassMirror? by lazy {
        java.annotatedSuperclass?.let {
            this.genericMapping[cache.types.reflect(it)] as ClassMirror
        }
    }

    /**
     * The list of interfaces directly implemented by this type, in the order they appear in the source code.
     * The returned type will be specialized based on this type's specialization and any explicit parameters set in the
     * source code.
     */
    val interfaces: List<ClassMirror> by lazy {
        java.annotatedInterfaces.map {
            this.genericMapping[cache.types.reflect(it)] as ClassMirror
        }.unmodifiableView()
    }

    /**
     * The list of type parameters defined by this mirror. These will be replaced when specializing, so you should use
     * [raw] to get the actual type parameters of the class as opposed to their specializations.
     */
    val typeParameters: List<TypeMirror> by lazy {
        specialization?.arguments ?: java.typeParameters.map { cache.types.reflect(it) }.unmodifiableView()
    }

    val enclosingClass: ClassMirror? by lazy {
        specialization?.enclosingClass ?: java.enclosingClass?.let { cache.types.reflect(it) as ClassMirror }
    }

    val enclosingExecutable: ExecutableMirror? by lazy {
        specialization?.enclosingExecutable ?: (java.enclosingMethod ?: java.enclosingConstructor)?.let { cache.executables.reflect(it) }
    }

    val genericMapping: TypeMapping by lazy {
        TypeMapping(this.raw.typeParameters.zip(typeParameters).associate { it }) +
            enclosingClass?.genericMapping + enclosingExecutable?.genericMapping
    }

    override fun defaultSpecialization() = TypeSpecialization.Class.DEFAULT

    /**
     * Creates a copy of this mirror, replacing its type parameters the given types. This will ripple the changes to
     * supertypes/interfaces, method and field signatures, etc. Passing zero arguments will return a copy of this
     * mirror with the raw type arguments.
     *
     * @throws InvalidSpecializationException if the passed type list is not the same length as [typeParameters] or zero
     * @return A copy of this type with its type parameters replaced
     */
    fun withTypeArguments(vararg parameters: TypeMirror): ClassMirror {
        if(parameters.size != typeParameters.size && parameters.isNotEmpty())
            throw InvalidSpecializationException("Passed parameter count ${parameters.size} is different from class type " +
                    "parameter count ${typeParameters.size}")
        val newSpecialization = (specialization ?: defaultSpecialization())
            .copy(arguments = if(parameters.isEmpty()) null else parameters.toList())
        return cache.types.specialize(raw, newSpecialization) as ClassMirror
    }

    /**
     * Creates a copy of this class with its enclosing class replaced with [enclosing].
     * If the passed class is null this method removes any enclosing class specialization.
     *
     * @throws InvalidSpecializationException if [enclosing] is not equal to or a specialization of this
     * class's raw enclosing class
     * @throws InvalidSpecializationException if this class has no enclosing class and [enclosing] is not null
     * @return A copy of this class with the passed enclosing class, or with the raw enclosing class if [enclosing]
     * is null
     */
    fun withEnclosingClass(enclosing: ClassMirror?): ClassMirror {
        if(enclosing != null && enclosing.raw != raw.enclosingClass)
            throw InvalidSpecializationException("Passed enclosing class ($enclosing) is not equal to or a " +
                "specialization of this class's enclosing class (${raw.enclosingClass})")
        val newSpecialization = (specialization ?: defaultSpecialization()).copy(enclosingClass = enclosing)
        return cache.types.specialize(raw, newSpecialization) as ClassMirror
    }

    /**
     * Creates a copy of this class with its enclosing method/constructor replaced with [enclosing].
     * If the passed executable is null this method removes any enclosing executable specialization.
     *
     * @throws InvalidSpecializationException if the passed executable is not equal to or a specialization of this
     * class's raw enclosing method
     * @throws InvalidSpecializationException if this class has no enclosing executable and [enclosing] is not null
     * @return A copy of this class with the passed enclosing executable, or with the raw enclosing executable if
     * [enclosing] is null
     */
    fun withEnclosingExecutable(enclosing: ExecutableMirror?): ClassMirror {
        if(enclosing != null && enclosing.raw != raw.enclosingExecutable)
            throw InvalidSpecializationException("Passed enclosing executable ($enclosing) is not equal to or a " +
                "specialization of this class's enclosing executable (${raw.enclosingExecutable})")
        val newSpecialization = (specialization ?: defaultSpecialization()).copy(enclosingExecutable = enclosing)
        return cache.types.specialize(raw, newSpecialization) as ClassMirror
    }

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Class>(
            specialization,
            { (it.arguments == this.typeParameters || it.arguments == null ) &&
                (it.enclosingClass == raw.enclosingClass || it.enclosingClass == null) &&
                (it.enclosingExecutable == raw.enclosingExecutable || it.enclosingExecutable == null)
            }
        ) {
            ClassMirror(cache, java, raw, it)
        }
    }

    /**
     * The inner classes declared directly inside of this class.
     *
     * This list is created when it is first accessed and is thread safe.
     */
    val declaredMemberClasses: List<ClassMirror> by lazy {
        java.declaredClasses.map {
            (cache.types.reflect(it) as ClassMirror).withEnclosingClass(this)
        }.unmodifiableView()
    }

    /**
     * The fields declared directly inside of this class, any fields inherited from superclasses will not appear in
     * this list.
     *
     * This list is created when it is first accessed and is thread safe.
     */
    val declaredFields: List<FieldMirror> by lazy {
        java.declaredFields.map {
            cache.fields.reflect(it).withDeclaringClass(this)
        }.unmodifiableView()
    }

    /**
     * The methods declared directly inside of this class, any methods inherited from superclasses will not appear in
     * this list.
     *
     * This list is created when it is first accessed and is thread safe.
     */
    val declaredMethods: List<MethodMirror> by lazy {
        java.declaredMethods.map {
            cache.executables.reflect(it).withDeclaringClass(this) as MethodMirror
        }.unmodifiableView()
    }

    /**
     * The constructors declared directly inside this class
     *
     * This list is created when it is first accessed and is thread safe.
     */
    val declaredConstructors: List<ConstructorMirror> by lazy {
        java.declaredConstructors.map {
            cache.executables.reflect(it).withDeclaringClass(this) as ConstructorMirror
        }.unmodifiableView()
    }
//endregion

    /*

    Key:
    * not implemented
    - done
    ? untested

    // methods = publicly visible on this and subclasses
    // declaredMethods = publicly and privately visible on this class specifically
    // allMethods = publicly and privately visible on this class and subclasses (excluding overrides/shadows?)

    // returns the specialized version of the passed method. So
    // `List<String>.getMethod(List.getMethod("get", Any)) == .get(String)`
    * fun getMethod(other: MethodMirror): MethodMirror?
    * fun getField(other: FieldMirror): FieldMirror?
    * fun getConstructor(other: ConstructorMirror): ConstructorMirror?
    * fun getMemberClass(other: MemberClassMirror): MemberClassMirror?

    - val methods: List<MethodMirror>
    - val declaredMethods: List<MethodMirror>
    ? val allMethods: List<MethodMirror>
    * fun getMethod(name: String, vararg args: TypeMirror): MethodMirror?
    * fun getMethod(raw: Boolean, name: String, vararg args: TypeMirror): MethodMirror?
    * fun getDeclaredMethod(name: String, vararg args: TypeMirror): MethodMirror?
    * fun getDeclaredMethod(raw: Boolean, name: String, vararg args: TypeMirror): MethodMirror?
    * fun getAllMethods(name: String, vararg args: TypeMirror): List<MethodMirror>
    * fun getAllMethods(raw: Boolean, name: String, vararg args: TypeMirror): List<MethodMirror>

    ? val fields: List<FieldMirror>
    - val declaredFields: List<FieldMirror>
    ? val allFields: List<FieldMirror>
    * fun getField(name: String): FieldMirror?
    * fun getDeclaredField(name: String): FieldMirror?
    * fun getAllFields(name: String): List<FieldMirror>

    ? val constructors: List<ConstructorMirror>
    - val declaredConstructors: List<ConstructorMirror>
    * fun getConstructor(vararg args: TypeMirror): ConstructorMirror?
    * fun getConstructor(raw: Boolean, vararg args: TypeMirror): ConstructorMirror?

    ? val memberClasses: List<ClassMirror>
    - val declaredMemberClasses: List<ClassMirror>
    ? val allMemberClasses: List<ClassMirror>
    * fun getMemberClass(name: String): ClassMirror?
    * fun getDeclaredMemberClass(name: String): ClassMirror?
    * fun getAllMemberClasses(name: String): List<ClassMirror>

    /** the _declaration site_ annotations, as opposed to [typeAnnotations] */
    ? val annotations: List<Annotation>
    * val declaredAnnotations: List<Annotation>

    - val modifiers: Set<Modifier>
    - val access: Modifier.Access

    - val isAnnotation: Boolean
    - val isAnonymous: Boolean
    - val isAbstract: Boolean
    - val isStatic: Boolean
    - val isStrictfp: Boolean
    - val isEnum: Boolean
    - val isLocal: Boolean
    - val isMember: Boolean
    - val isPrimitive: Boolean
    - val isSynthetic: Boolean
    - val isSealed: Boolean
    - val isOpen: Boolean
    - val isData: Boolean
    - val isCompanion: Boolean

    ? val simpleName: String
    ? val name: String
    ? val canonicalName: String

    // returns the enum type of this class, either this mirror or its superclass in the case of anonymous subclass enum
    // elements
    - val enumType: ClassMirror?
    // if [isEnum] is true, this returns the array of enum constants in for this enum class.
    - val enumConstants: List<Object>?
    */

    //region Simple helpers
    val kClass: KClass<*>? = java.kotlin

    val modifiers: Set<Modifier> = Modifier.fromModifiers(java.modifiers).unmodifiableView()
    val access: Modifier.Access = Modifier.Access.fromModifiers(java.modifiers)
    /**
     * Returns true if this object represents a Kotlin class and that class has an `internal` visibility modifier
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath (kotlin-reflect is an
     * optional dependency for jar size reasons)
     */
    @Suppress("NO_REFLECTION_IN_CLASS_PATH")
    val isInternalAccess: Boolean get() = kClass?.visibility == KVisibility.INTERNAL

    val flags: Set<Flag> = listOf(
        Flag.ABSTRACT to (Modifier.ABSTRACT in modifiers),
        Flag.STATIC to (Modifier.STATIC in modifiers),
        Flag.FINAL to (Modifier.FINAL in modifiers),
        Flag.STRICT to (Modifier.STRICT in modifiers),

        Flag.ANNOTATION to java.isAnnotation,
        Flag.ANONYMOUS to java.isAnonymousClass,
        Flag.ENUM to java.isEnum,
        Flag.INTERFACE to java.isInterface,
        Flag.LOCAL to java.isLocalClass,
        Flag.MEMBER to java.isMemberClass,
        Flag.PRIMITIVE to java.isPrimitive,
        Flag.SYNTHETIC to java.isSynthetic
    ).filter { it.second }.mapTo(mutableSetOf()) { it.first }.unmodifiableView()

    /**
     * Returns true if this mirror represents an abstract class.
     */
    val isAbstract: Boolean = Flag.ABSTRACT in flags
    /**
     * Returns true if this mirror represents a static class.
     */
    val isStatic: Boolean = Flag.STATIC in flags
    /**
     * Returns true if this mirror represents a final class.
     */
    val isFinal: Boolean = Flag.FINAL in flags
    /**
     * Returns true if the class this mirror represents has the `strictfp` modifier.
     *
     * NOTE: For unknown reasons the strictfp modifier is not present in the Core Reflection modifiers, so this is
     * always false
     */
    val isStrict: Boolean = Flag.STRICT in flags

    /**
     * Returns true if the class this mirror represents is not final.
     */
    val isOpen: Boolean = !isFinal
    /**
     * Returns true if this object represents a Kotlin class and that class is a companion class
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath (kotlin-reflect is an
     * optional dependency for jar size reasons)
     */
    @Suppress("NO_REFLECTION_IN_CLASS_PATH")
    val isCompanion: Boolean get() = kClass?.isCompanion ?: false
    /**
     * Returns true if this object represents a Kotlin class and that class is a data class
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath (kotlin-reflect is an
     * optional dependency for jar size reasons)
     */
    @Suppress("NO_REFLECTION_IN_CLASS_PATH")
    val isData: Boolean get() = kClass?.isData ?: false
    /**
     * Returns true if this object represents a Kotlin class and that class is a sealed class
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath (kotlin-reflect is an
     * optional dependency for jar size reasons)
     */
    @Suppress("NO_REFLECTION_IN_CLASS_PATH")
    val isSealed: Boolean get() = kClass?.isSealed ?: false
    /**
     * Returns true if this mirror represents an annotation class.
     */
    val isAnnotation: Boolean = Flag.ANNOTATION in flags
    /**
     * Returns true if this mirror represents an anonymous class.
     */
    val isAnonymous: Boolean = Flag.ANONYMOUS in flags
    /**
     * Returns true if this mirror represents an enum class. This is false for anonymous enum subclasses, so for more
     * consistent behavior use [enumType].
     */
    val isEnum: Boolean = Flag.ENUM in flags
    /**
     * Returns true if the class this mirror represents is an interface.
     */
    val isInterface: Boolean = Flag.INTERFACE in flags
    /**
     * Returns true if this mirror represents a local class. Local classes are classes declared within a block of code
     * such as a method or constructor.
     */
    val isLocal: Boolean = Flag.LOCAL in flags
    /**
     * Returns true if the class this mirror represents is a member of another class. Member classes include TODO what?
     */
    val isMember: Boolean = Flag.MEMBER in flags
    /**
     * Returns true if this mirror represents a primitive class.
     */
    val isPrimitive: Boolean = Flag.PRIMITIVE in flags
    /**
     * Returns true if this mirror represents a synthetic class.
     */
    val isSynthetic: Boolean = Flag.SYNTHETIC in flags

    /**
     * Returns annotations that are present on the class this mirror represents. These are not the annotations
     * present on the use of the type, for those use [typeAnnotations]
     *
     * @see Class.getAnnotations
     */
    val annotations: List<Annotation> = java.annotations.toList().unmodifiableView()

    /**
     * Returns annotations that are directly present on the class this mirror represents. These are not the annotations
     * present on the use of the type, for those use [typeAnnotations]
     *
     * @see Class.getDeclaredAnnotations
     */
    val declaredAnnotations: List<Annotation> = java.declaredAnnotations.toList().unmodifiableView()

    /**
     * Returns the logical enum type of the class this mirror represents, taking into account anonymous enum subclasses,
     * or null if this mirror does not represent an enum type or enum subclass.
     *
     * Anonymous enum subclasses (any enum element that overrides or implements a method from the enum itself) aren't
     * enum classes themselves. This method will return the true enum class for both the actual enum and the subclasses.
     */
    val enumType: ClassMirror? by lazy {
        when {
            this.isEnum -> this
            this.superclass?.isEnum == true -> this.superclass
            else -> null
        }
    }

    /**
     * Returns the list of constants in the enum class this mirror represents, or null if this mirror does not
     * represent an enum class. If this mirror represents an anonymous subclass of an enum, this will return null.
     *
     * @see enumType
     * @see Class.enumConstants
     */
    val enumConstants: List<Enum<*>>? = java.enumConstants?.toList()?.checkedCast<Enum<*>>()?.unmodifiableView()

    /**
     * Returns the simple name of the class this mirror represents.
     *
     * See [Class.getSimpleName] for nuances.
     */
    val simpleName: String = java.simpleName
    /**
     * Returns the internal name of the class this mirror represents. (e.g. `boolean` = `Z`,
     * `com.example.Foo` = `Lcom.example.Foo;`)
     *
     * See [Class.getName] for nuances.
     */
    val name: String = java.name
    /**
     * Returns the simple name of the class this mirror represents.
     *
     * See [Class.getCanonicalName] for nuances.
     */
    val canonicalName: String? = java.canonicalName
    //endregion

    //region Member helpers

    // methods = publicly visible on this and subclasses
    // declaredMethods = publicly and privately visible on this class specifically
    // allMethods = publicly and privately visible on this class and subclasses (excluding overrides? including shadows)
    // returns the specialized version of the passed method. So
    // `List<String>.getMethod(List.getMethod("get", Any)) == .get(String)`

    //region Specializers

    /**
     * Gets the specialized mirror that represents the same method as [other], or null if this type has no
     * corresponding mirror.
     */
    fun getMethod(other: MethodMirror): MethodMirror? = getMethod(other.java)
    /**
     * Gets the specialized mirror that represents [other], or null if this type has no corresponding mirror.
     */
    fun getMethod(other: Method): MethodMirror? {
        if(other.declaringClass == this.java) {
            return declaredMethods.find { it.java == other }
        }
        return findSuperclass(other.declaringClass)?.getMethod(other)
    }

    /**
     * Gets the specialized mirror that represents the same field as [other], or null if this type has no
     * corresponding mirror.
     */
    fun getField(other: FieldMirror): FieldMirror? = getField(other.java)
    /**
     * Gets the specialized mirror that represents [other], or null if this type has no corresponding mirror.
     */
    fun getField(other: Field): FieldMirror? {
        if(other.declaringClass == this.java) {
            return declaredFields.find { it.java == other }
        }
        return findSuperclass(other.declaringClass)?.getField(other)
    }

    /**
     * Gets the specialized mirror that represents the same constructor as [other], or null if this type has no
     * corresponding mirror.
     */
    fun getConstructor(other: ConstructorMirror): ConstructorMirror? = getConstructor(other.java)
    /**
     * Gets the specialized mirror that represents [other], or null if this type has no corresponding mirror.
     */
    fun getConstructor(other: Constructor<*>): ConstructorMirror? {
        if(other.declaringClass == this.java) {
            return declaredConstructors.find { it.java == other }
        }
        return findSuperclass(other.declaringClass)?.getConstructor(other)
    }

    /**
     * Gets the specialized mirror that represents the same member class as [other], or null if this type has no
     * corresponding mirror.
     */
    fun getMemberClass(other: ClassMirror): ClassMirror? = getMemberClass(other.java)
    /**
     * Gets the specialized mirror that represents [other], or null if this type has no corresponding mirror.
     */
    fun getMemberClass(other: Class<*>): ClassMirror? {
        if(other.declaringClass == this.java) {
            return declaredMemberClasses.find { it.java == other }
        }
        return findSuperclass(other.declaringClass)?.getMemberClass(other)
    }

    //endregion

    //region Methods
    val methods: List<MethodMirror> by lazy {
        java.methods.map { this.getMethod(it)!! }
    }
    val allMethods: List<MethodMirror> by lazy {

        val allInterfaces = mutableSetOf<Class<*>>()
        val allClasses = mutableSetOf<Class<*>>()

        var current: Class<*>? = this.java
        while(current != null) {
            allClasses.add(current)
            allInterfaces.addAll(current.interfaces)
            current = current.superclass
        }

        // java.lang.Class.MethodArray#matchesNameAndDescriptor
        fun matchesNameAndDescriptor(m1: Method, m2: Method) =
            m1.returnType == m2.returnType &&
                m1.name == m2.name &&
                m1.parameterTypes!!.contentEquals(m2.parameterTypes)

        val allMethodsUnfiltered = mutableListOf<Method>()

        allClasses.forEach { clazz ->
            allMethodsUnfiltered.addAll(clazz.declaredMethods)
        }
        allInterfaces.forEach { iface ->
            allMethodsUnfiltered.addAll(iface.declaredMethods.filter { !JvmModifier.isStatic(it.modifiers) })
        }

        val allMethods = mutableListOf<Method>()

        allMethodsUnfiltered.forEach { method ->
            if(allMethods.none { matchesNameAndDescriptor(method, it) })
                allMethods.add(method)
        }

        return@lazy allMethods.map { this.getMethod(it)!! }.unmodifiableView()
    }
//    fun getMethod(name: String, vararg args: TypeMirror): MethodMirror?
//    fun getMethod(raw: Boolean, name: String, vararg args: TypeMirror): MethodMirror?
//    fun getDeclaredMethod(name: String, vararg args: TypeMirror): MethodMirror?
//    fun getDeclaredMethod(raw: Boolean, name: String, vararg args: TypeMirror): MethodMirror?
//    fun getAllMethods(name: String, vararg args: TypeMirror): List<MethodMirror>
//    fun getAllMethods(raw: Boolean, name: String, vararg args: TypeMirror): List<MethodMirror>
    //endregion

    //region Fields
    //TODO test
    val fields: List<FieldMirror> by lazy { java.fields.mapNotNull { getField(it) }.unmodifiableView() }
    val allFields: List<FieldMirror> by lazy {
        (declaredFields + (superclass?.allFields ?: emptyList())).uniqueBy { it.name }.unmodifiableView()
    }
//    fun getField(name: String): FieldMirror?
//    fun getDeclaredField(name: String): FieldMirror?
//    fun getAllFields(name: String): List<FieldMirror>
    //endregion

    //region Constructors
    //TODO test
    val constructors: List<ConstructorMirror> by lazy { java.constructors.mapNotNull { getConstructor(it) }.unmodifiableView() }
//    fun getConstructor(vararg args: TypeMirror): ConstructorMirror?
//    fun getConstructor(raw: Boolean, vararg args: TypeMirror): ConstructorMirror?
    //endregion

    //region Member classes
    //TODO test
    val memberClasses: List<ClassMirror> by lazy { java.classes.mapNotNull { getMemberClass(it) }.unmodifiableView() }
    val allMemberClasses: List<ClassMirror> by lazy {
        (declaredMemberClasses + (superclass?.allMemberClasses ?: emptyList()) +
            interfaces.flatMap { it.allMemberClasses }).uniqueBy { it.simpleName }.unmodifiableView()
    }
//    fun getMemberClass(name: String): ClassMirror?
//    fun getDeclaredMemberClass(name: String): ClassMirror?
//    fun getAllMemberClasses(name: String): List<ClassMirror>
    //endregion

    //endregion

    fun declaredClass(name: String): ClassMirror? {
        return declaredMemberClasses.find { it.java.simpleName == name }
    }

    private val declaredClassCache = ConcurrentHashMap<String, List<ClassMirror>>()

    fun innerClasses(name: String): List<ClassMirror> {
        return declaredClassCache.getOrPut(name) {
            val list = mutableListOf<ClassMirror>()
            declaredMemberClasses.find { it.java.simpleName == name }?.also { list.add(it) }
            superclass?.also { list.addAll(it.innerClasses(name)) }
            return@getOrPut list.unmodifiableView()
        }
    }

    fun declaredField(name: String): FieldMirror? {
        return declaredFields.find { it.name == name }
    }

    private val fieldNameCache = ConcurrentHashMap<String, FieldMirror?>()

    fun field(name: String): FieldMirror? {
        return fieldNameCache.getOrPut(name) {
            var field: FieldMirror? = null
            field = field ?: declaredFields.find { it.name == name }
            field = field ?: superclass?.field(name)
            return@getOrPut field
        }
    }

    fun declaredMethods(name: String): List<MethodMirror> {
        return declaredMethods.filter { it.name == name }.unmodifiableView()
    }

    private val methodNameCache = ConcurrentHashMap<String, List<MethodMirror>>()

    fun methods(name: String): List<MethodMirror> {
        return methodNameCache.getOrPut(name) {
            val methods = mutableListOf<MethodMirror>()
            methods.addAll(declaredMethods.filter { it.name == name })
            methods.addAll(superclass?.methods(name) ?: emptyList())
            return@getOrPut methods.unmodifiableView()
        }
    }

    fun declaredConstructor(vararg params: TypeMirror): ConstructorMirror? {
        val match = params.toList()
        return declaredConstructors.find { it.parameterTypes == match }
    }

    private val isAssignableCache = ConcurrentHashMap<TypeMirror, Boolean>()

    override fun isAssignableFrom(other: TypeMirror): Boolean {
        if(other == this) return true
        if(this.java == Any::class.java) {
            if(other is VoidMirror) return false
            if(other is ClassMirror && other.java.isPrimitive) return false
            return true
        }
        if(other !is ClassMirror)
            return false

        return isAssignableCache.getOrPut(other) {
            if(other.raw == this.raw) {
                return@getOrPut this.typeParameters.zip(other.typeParameters)
                    .all { (ours, theirs) -> ours.isAssignableFrom(theirs) }
            }

            if(other.superclass?.let { this.isAssignableFrom(it) } == true)
                return@getOrPut true

            if(other.interfaces.any { this.isAssignableFrom(it) })
                return@getOrPut true

            return@getOrPut false
        }
    }

    /**
     * Recursively searches through this class's supertypes to find the [most specific][specificity] superclass
     * with the specified type. If this class is the specified type this method returns this class.
     *
     * @return The specialized superclass with the passed type, or null if none were found.
     */
    fun findSuperclass(clazz: Class<*>): ClassMirror? {
        if(java == clazz) return this
        var supertype = superclass?.findSuperclass(clazz)
        for(it in interfaces) {
            val candidate = it.findSuperclass(clazz)
            if(candidate != null && (supertype == null || supertype.specificity < candidate.specificity))
                supertype = candidate
        }
        return supertype
    }

    /**
     * Returns a string representing the declaration of this type with type parameters substituted in,
     * as opposed to [toString] which returns the string representing the usage of this type
     */
    val declarationString: String
        get() {
            var str = ""
            str += java.simpleName
            if(typeParameters.isNotEmpty()) {
                str += "<${typeParameters.joinToString(", ")}>"
            }
            superclass?.let { superclass ->
                str += " extends $superclass"
            }
            if(interfaces.isNotEmpty()) {
                str += " implements ${interfaces.joinToString(", ")}"
            }

            return str
        }

    override fun toString(): String {
        val specialization = this.specialization
        var str = ""
        if(specialization?.annotations?.isNotEmpty() == true) {
            str += specialization.annotations.joinToString(" ") + " "
        }
        str += java.canonicalName
        if(typeParameters.isNotEmpty()) {
            str += "<${typeParameters.joinToString(", ")}>"
        }
        return str
    }

    enum class Flag {
        ABSTRACT,
        STATIC,
        FINAL,
        STRICT,

        ANNOTATION,
        ANONYMOUS,
        ENUM,
        INTERFACE,
        LOCAL,
        MEMBER,
        PRIMITIVE,
        SYNTHETIC
    }
}

