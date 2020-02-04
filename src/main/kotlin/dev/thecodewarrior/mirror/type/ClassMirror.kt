package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.member.ExecutableMirror
import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.member.Modifier
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * The type of mirror used to represent classes, as opposed to arrays, type variables, wildcards, and `void`.
 *
 * @see ArrayMirror
 * @see VariableMirror
 * @see VoidMirror
 * @see WildcardMirror
 */
abstract class ClassMirror : ConcreteTypeMirror() {
    /**
     * The raw, unspecialized version of this mirror.
     */
    abstract override val raw: ClassMirror

    /**
     * The supertype of this class. This property is `null` if this reflect represents [Object], an interface,
     * a primitive, or `void`. The returned type will be specialized based on this type's specialization and any
     * explicit parameters set in the source code.
     */
    abstract val superclass: ClassMirror?

    /**
     * The list of interfaces directly implemented by this type, in the order they appear in the source code.
     * The returned type will be specialized based on this type's specialization and any explicit parameters set in the
     * source code.
     *
     * **Note: this value is immutable**
     */
    abstract val interfaces: List<ClassMirror>

    /**
     * The list of type parameters defined by this mirror. These will be replaced when specializing, so you should use
     * [raw] to get the actual type parameters of the class as opposed to their specializations.
     *
     * **Note: this value is immutable**
     */
    abstract val typeParameters: List<TypeMirror>

    abstract val enclosingClass: ClassMirror?

    abstract val enclosingExecutable: ExecutableMirror?

    abstract val genericMapping: TypeMapping

    /**
     * Creates a copy of this mirror, replacing its type parameters the given types. This will ripple the changes to
     * supertypes/interfaces, method and field signatures, etc. Passing zero arguments will return a copy of this
     * mirror with the raw type arguments.
     *
     * @throws InvalidSpecializationException if the passed type list is not the same length as [typeParameters] or zero
     * @return A copy of this type with its type parameters replaced
     */
    abstract fun withTypeArguments(vararg parameters: TypeMirror): ClassMirror

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
    abstract fun withEnclosingClass(enclosing: ClassMirror?): ClassMirror

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
    abstract fun withEnclosingExecutable(enclosing: ExecutableMirror?): ClassMirror


    /**
     * The inner classes declared directly inside of this class.
     *
     * This list is created when it is first accessed and is thread safe.
     *
     * **Note: this value is immutable**
     *
     * @see Class.getDeclaredClasses
     */
    abstract val declaredMemberClasses: List<ClassMirror>

    /**
     * The fields declared directly inside of this class, any fields inherited from superclasses will not appear in
     * this list.
     *
     * This list is created when it is first accessed and is thread safe.
     *
     * **Note: this value is immutable**
     *
     * @see Class.getDeclaredFields
     */
    abstract val declaredFields: List<FieldMirror>

    /**
     * The methods declared directly inside of this class, any methods inherited from superclasses will not appear in
     * this list.
     *
     * This list is created when it is first accessed and is thread safe.
     *
     * **Note: this value is immutable**
     *
     * @see Class.getDeclaredMethods
     */
    abstract val declaredMethods: List<MethodMirror>

    /**
     * The constructors declared directly inside this class
     *
     * This list is created when it is first accessed and is thread safe.
     *
     * **Note: this value is immutable**
     */
    abstract val declaredConstructors: List<ConstructorMirror>
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
    abstract val kClass: KClass<*>

    // * **Note: this value is immutable**
    abstract val modifiers: Set<Modifier>
    abstract val access: Modifier.Access
    /**
     * Returns true if this object represents a Kotlin class and that class has an `internal` visibility modifier
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath (kotlin-reflect is an
     * optional dependency for jar size reasons)
     */
    @Suppress("NO_REFLECTION_IN_CLASS_PATH")
    abstract val isInternalAccess: Boolean

    // * **Note: this value is immutable**
    abstract val flags: Set<Flag>

    /**
     * Returns true if this mirror represents an abstract class.
     */
    abstract val isAbstract: Boolean
    /**
     * Returns true if this mirror represents a static class.
     */
    abstract val isStatic: Boolean
    /**
     * Returns true if this mirror represents a final class.
     */
    abstract val isFinal: Boolean
    /**
     * Returns true if the class this mirror represents has the `strictfp` modifier.
     *
     * NOTE: For unknown reasons the strictfp modifier is not present in the Core Reflection modifiers, so this is
     * always false
     */
    abstract val isStrict: Boolean

    /**
     * Returns true if the class this mirror represents is not final.
     */
    abstract val isOpen: Boolean

    /**
     * Returns true if this object represents a Kotlin class and that class is a companion class
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath (kotlin-reflect is an
     * optional dependency for jar size reasons)
     */
    @Suppress("NO_REFLECTION_IN_CLASS_PATH")
    abstract val isCompanion: Boolean

    /**
     * Returns true if this object represents a Kotlin class and that class is a data class
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath (kotlin-reflect is an
     * optional dependency for jar size reasons)
     */
    @Suppress("NO_REFLECTION_IN_CLASS_PATH")
    abstract val isData: Boolean

    /**
     * Returns true if this object represents a Kotlin class and that class is a sealed class
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath (kotlin-reflect is an
     * optional dependency for jar size reasons)
     */
    @Suppress("NO_REFLECTION_IN_CLASS_PATH")
    abstract val isSealed: Boolean

    /**
     * Returns true if this mirror represents an annotation class.
     */
    abstract val isAnnotation: Boolean
    /**
     * Returns true if this mirror represents an anonymous class.
     */
    abstract val isAnonymous: Boolean
    /**
     * Returns true if this mirror represents an enum class. This is false for anonymous enum subclasses, so for more
     * consistent behavior use [enumType].
     */
    abstract val isEnum: Boolean
    /**
     * Returns true if the class this mirror represents is an interface.
     */
    abstract val isInterface: Boolean
    /**
     * Returns true if this mirror represents a local class. Local classes are classes declared within a block of code
     * such as a method or constructor.
     */
    abstract val isLocal: Boolean
    /**
     * Returns true if the class this mirror represents is a member of another class. Member classes include TODO what?
     */
    abstract val isMember: Boolean
    /**
     * Returns true if this mirror represents a primitive class.
     */
    abstract val isPrimitive: Boolean
    /**
     * Returns true if this mirror represents a synthetic class.
     */
    abstract val isSynthetic: Boolean

    /**
     * Returns annotations that are present on the class this mirror represents. These are not the annotations
     * present on the use of the type, for those use [typeAnnotations]
     *
     * **Note: this value is immutable**
     *
     * @see Class.getAnnotations
     */
    abstract val annotations: List<Annotation>

    /**
     * Returns annotations that are directly present on the class this mirror represents. These are not the annotations
     * present on the use of the type, for those use [typeAnnotations]
     *
     * **Note: this value is immutable**
     *
     * @see Class.getDeclaredAnnotations
     */
    abstract val declaredAnnotations: List<Annotation>

    /**
     * Returns the logical enum type of the class this mirror represents, taking into account anonymous enum subclasses,
     * or null if this mirror does not represent an enum type or enum subclass.
     *
     * Anonymous enum subclasses (any enum element that overrides or implements a method from the enum itself) aren't
     * enum classes themselves. This method will return the true enum class for both the actual enum and the subclasses.
     */
    abstract val enumType: ClassMirror?

    /**
     * Returns the list of constants in the enum class this mirror represents, or null if this mirror does not
     * represent an enum class. If this mirror represents an anonymous subclass of an enum, this will return null.
     *
     * **Note: this value is immutable**
     *
     * @see enumType
     * @see Class.enumConstants
     */
    abstract val enumConstants: List<Enum<*>>?

    /**
     * Returns the simple name of the class this mirror represents.
     *
     * See [Class.getSimpleName] for nuances.
     */
    abstract val simpleName: String
    /**
     * Returns the internal name of the class this mirror represents. (e.g. `boolean` = `Z`,
     * `com.example.Foo` = `Lcom.example.Foo;`)
     *
     * See [Class.getName] for nuances.
     */
    abstract val name: String
    /**
     * Returns the simple name of the class this mirror represents.
     *
     * See [Class.getCanonicalName] for nuances.
     */
    abstract val canonicalName: String?
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
    abstract fun getMethod(other: MethodMirror): MethodMirror?
    /**
     * Gets the specialized mirror that represents [other], or null if this type has no corresponding mirror.
     */
    abstract fun getMethod(other: Method): MethodMirror?

    /**
     * Gets the specialized mirror that represents the same field as [other], or null if this type has no
     * corresponding mirror.
     */
    abstract fun getField(other: FieldMirror): FieldMirror?
    /**
     * Gets the specialized mirror that represents [other], or null if this type has no corresponding mirror.
     */
    abstract fun getField(other: Field): FieldMirror?

    /**
     * Gets the specialized mirror that represents the same constructor as [other], or null if this type has no
     * corresponding mirror.
     */
    abstract fun getConstructor(other: ConstructorMirror): ConstructorMirror?
    /**
     * Gets the specialized mirror that represents [other], or null if this type has no corresponding mirror.
     */
    abstract fun getConstructor(other: Constructor<*>): ConstructorMirror?

    /**
     * Gets the specialized mirror that represents the same member class as [other], or null if this type has no
     * corresponding mirror.
     */
    abstract fun getMemberClass(other: ClassMirror): ClassMirror?
    /**
     * Gets the specialized mirror that represents [other], or null if this type has no corresponding mirror.
     */
    abstract fun getMemberClass(other: Class<*>): ClassMirror?

    //endregion

    //region Methods
    // * **Note: this value is immutable**
    abstract val methods: List<MethodMirror>
    // * **Note: this value is immutable**
    abstract val allMethods: List<MethodMirror>
//    fun getMethod(name: String, vararg args: TypeMirror): MethodMirror?
//    fun getMethod(raw: Boolean, name: String, vararg args: TypeMirror): MethodMirror?
//    fun getDeclaredMethod(name: String, vararg args: TypeMirror): MethodMirror?
//    fun getDeclaredMethod(raw: Boolean, name: String, vararg args: TypeMirror): MethodMirror?
//    fun getAllMethods(name: String, vararg args: TypeMirror): List<MethodMirror>
//    fun getAllMethods(raw: Boolean, name: String, vararg args: TypeMirror): List<MethodMirror>
    //endregion

    //region Fields
    //TODO test
    // * **Note: this value is immutable**
    abstract val fields: List<FieldMirror>
    // * **Note: this value is immutable**
    abstract val allFields: List<FieldMirror>
//    fun getField(name: String): FieldMirror?
//    fun getDeclaredField(name: String): FieldMirror?
//    fun getAllFields(name: String): List<FieldMirror>
    //endregion

    //region Constructors
    //TODO test
    // * **Note: this value is immutable**
    abstract val constructors: List<ConstructorMirror>
//    fun getConstructor(vararg args: TypeMirror): ConstructorMirror?
//    fun getConstructor(raw: Boolean, vararg args: TypeMirror): ConstructorMirror?
    //endregion

    //region Member classes
    //TODO test
    // * **Note: this value is immutable**
    abstract val memberClasses: List<ClassMirror>
    // * **Note: this value is immutable**
    abstract val allMemberClasses: List<ClassMirror>
//    fun getMemberClass(name: String): ClassMirror?
//    fun getDeclaredMemberClass(name: String): ClassMirror?
//    fun getAllMemberClasses(name: String): List<ClassMirror>
    //endregion

    //endregion

    abstract fun declaredClass(name: String): ClassMirror?

    // * **Note: this value is immutable**
    abstract fun innerClasses(name: String): List<ClassMirror>

    abstract fun declaredField(name: String): FieldMirror?

    abstract fun field(name: String): FieldMirror?

    // * **Note: this value is immutable**
    abstract fun declaredMethods(name: String): List<MethodMirror>

    // * **Note: this value is immutable**
    abstract fun methods(name: String): List<MethodMirror>

    abstract fun declaredConstructor(vararg params: TypeMirror): ConstructorMirror?

    /**
     * Recursively searches through this class's supertypes to find the [most specific][specificity] superclass
     * with the specified type. If this class is the specified type this method returns this class.
     *
     * @return The specialized superclass with the passed type, or null if none were found.
     */
    abstract fun findSuperclass(clazz: Class<*>): ClassMirror?

    /**
     * Returns a string representing the declaration of this type with type parameters substituted in,
     * as opposed to [toString] which returns the string representing the usage of this type
     */
    abstract val declarationString: String

    /**
     * A set of useful flags for classes, such as whether it is abstract, anonymous, primitive, etc.
     */
    enum class Flag {
        /**
         * This flag is present on abstract classes
         */
        ABSTRACT,
        /**
         * This flag is present on static inner classes
         */
        STATIC,
        /**
         * This flag is present on final classes
         */
        FINAL,
        /**
         * This flag is present on classes with the `strictfp` modifier
         */
        STRICT,

        /**
         * This flag is present on annotation classes
         */
        ANNOTATION,
        /**
         * This flag is present on anonymous classes
         */
        ANONYMOUS,
        /**
         * This flag is present on enum classes, but not on anonymous enum element subclasses.
         */
        ENUM,
        /**
         * This flag is present on interfaces
         */
        INTERFACE,
        /**
         * This flag is present on local classes (classes declared within an expression body, such as a method)
         */
        LOCAL,
        /**
         * This flag is present on member classes. This includes inner and static inner classes
         */
        MEMBER,
        /**
         * This flag is present on the primitive classes: `boolean`, `byte`, `char`, `short`, `int`, `long`, `float`,
         * and `double`
         */
        PRIMITIVE,
        /**
         * This flag is present on synthetic classes (classes generated by the compiler, without a corresponding
         * construct in the source code)
         */
        SYNTHETIC
    }
}