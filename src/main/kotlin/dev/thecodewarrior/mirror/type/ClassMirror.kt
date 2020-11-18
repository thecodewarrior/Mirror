package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.NoSuchMirrorException
import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.member.ExecutableMirror
import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.member.Modifier
import dev.thecodewarrior.mirror.type.classmirror.MethodList
import dev.thecodewarrior.mirror.utils.Untested
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * The type of mirror used to represent classes, as opposed to arrays, type variables, wildcards, and `void`.
 *
 * @see ArrayMirror
 * @see TypeVariableMirror
 * @see VoidMirror
 * @see WildcardMirror
 */
public abstract class ClassMirror : ConcreteTypeMirror(), AnnotatedElement {

    /**
     * The raw, unspecialized version of this mirror.
     */
    abstract override val raw: ClassMirror

//region Specialization =========================================================================================================
    /**
     * The supertype of this class. This property is `null` if this reflect represents [Object], an interface,
     * a primitive, or `void`. The returned type will be specialized based on this type's specialization and any
     * explicit parameters set in the source code.
     */
    public abstract val superclass: ClassMirror?

    /**
     * The list of interfaces directly implemented by this type, in the order they appear in the source code.
     * The returned type will be specialized based on this type's specialization and any explicit parameters set in the
     * source code.
     *
     * **Note: this list is immutable**
     */
    public abstract val interfaces: List<ClassMirror>

    /**
     * The list of type parameters defined by this mirror. These will be replaced when specializing, so you should use
     * [raw] to get the actual type parameters of the class as opposed to their specializations.
     *
     * **Note: this list is immutable**
     */
    public abstract val typeParameters: List<TypeMirror>

    public abstract val enclosingClass: ClassMirror?

    public abstract val enclosingExecutable: ExecutableMirror?

    internal abstract val genericMapping: TypeMapping

    /**
     * Returns a copy of this mirror, replacing its type parameters the given types. This will ripple the changes to
     * supertypes/interfaces, method and field signatures, etc. Passing zero arguments will return a copy of this
     * mirror with the raw type arguments.
     *
     * **Note: A new `ClassMirror` is only created if none already exist with the required specialization**
     *
     * @throws InvalidSpecializationException if the passed type list is not the same length as [typeParameters] or zero
     * @return A copy of this type with its type parameters replaced, or the the existing mirror with those types
     */
    public abstract fun withTypeArguments(vararg parameters: TypeMirror): ClassMirror

    /**
     * Returns a copy of this class with its enclosing class replaced with [enclosing].
     * If the passed class is null this method removes any enclosing class specialization.
     *
     * **Note: A new `ClassMirror` is only created if none already exist with the required specialization**
     *
     * @throws InvalidSpecializationException if [enclosing] is not equal to or a specialization of this
     * class's raw enclosing class
     * @throws InvalidSpecializationException if this class has no enclosing class and [enclosing] is not null
     * @return A copy of this class with the passed enclosing class, or with the raw enclosing class if [enclosing]
     * is null
     */
    public abstract fun withEnclosingClass(enclosing: ClassMirror?): ClassMirror

    /**
     * Returns a copy of this class with its enclosing method/constructor replaced with [enclosing].
     * If the passed executable is null this method removes any enclosing executable specialization.
     *
     * **Note: A new `ClassMirror` is only created if none already exist with the required specialization**
     *
     * @throws InvalidSpecializationException if the passed executable is not equal to or a specialization of this
     * class's raw enclosing method
     * @throws InvalidSpecializationException if this class has no enclosing executable and [enclosing] is not null
     * @return A copy of this class with the passed enclosing executable, or with the raw enclosing executable if
     * [enclosing] is null
     */
    public abstract fun withEnclosingExecutable(enclosing: ExecutableMirror?): ClassMirror
//endregion =====================================================================================================================

//region Relationships ==========================================================================================================
    /**
     * Recursively searches through this class's supertypes to find the [most specific][specificity] superclass
     * with the specified type. If this class is the specified type this method returns this class. This method returns
     * null if no such superclass was found. Use [getSuperclass] if you want an exception thrown on failure instead.
     *
     * @return The specialized superclass with the passed type, or null if none were found.
     */
    public abstract fun findSuperclass(clazz: Class<*>): ClassMirror?

    /**
     * Recursively searches through this class's supertypes to find the [most specific][specificity] superclass
     * with the specified type. If this class is the specified type this method returns this class. This method returns
     * null if no such superclass was found. Use [getSuperclass] if you want an exception thrown on failure instead.
     *
     * @return The specialized superclass with the passed type, or null if none were found.
     */
    @Untested
    @JvmSynthetic
    @JvmName("findSuperclassInline")
    public inline fun <reified T> findSuperclass(): ClassMirror? {
        return findSuperclass(T::class.java)
    }

    /**
     * Recursively searches through this class's supertypes to find the [most specific][specificity] superclass
     * with the specified type. If this class is the specified type this method returns this class. This method throws
     * an exception if no such superclass was found. Use [findSuperclass] if you want a null on failure instead.
     *
     * @return The specialized superclass with the passed type
     * @throws NoSuchMirrorException if this class has no superclass of the passed type
     */
    @Untested
    public abstract fun getSuperclass(clazz: Class<*>): ClassMirror

    /**
     * Recursively searches through this class's supertypes to find the [most specific][specificity] superclass
     * with the specified type. If this class is the specified type this method returns this class. This method throws
     * an exception if no such superclass was found. Use [findSuperclass] if you want a null on failure instead.
     *
     * @return The specialized superclass with the passed type
     * @throws NoSuchMirrorException if this class has no superclass of the passed type
     */
    @Untested
    @JvmSynthetic
    @JvmName("getSuperclassInline")
    public inline fun <reified T> getSuperclass(): ClassMirror {
        return getSuperclass(T::class.java)
    }
//endregion =====================================================================================================================

//region Simple helpers =========================================================================================================
    /**
     * The Kotlin `KClass` instance associated with this class
     */
    public abstract val kClass: KClass<*>

    /**
     * The modifiers present on this class. (e.g. `public`, `abstract`, `final`, etc.)
     *
     * **Note: this list is immutable**
     */
    public abstract val modifiers: Set<Modifier>

    /**
     * The access modifier present on this class
     */
    public abstract val access: Modifier.Access

    /**
     * Returns true if this object represents a Kotlin class and that class has an `internal` visibility modifier
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath
     */
    public abstract val isInternalAccess: Boolean

    /**
     * A set of flags used to store properties such as whether the class is static, abstract, an enum, etc.
     *
     * **Note: this list is immutable**
     */
    public abstract val flags: Set<Flag>

    /**
     * Returns true if this mirror represents an abstract class.
     */
    @Untested
    public abstract val isAbstract: Boolean

    /**
     * Returns true if this mirror represents a static class.
     */
    @Untested
    public abstract val isStatic: Boolean

    /**
     * Returns true if this mirror represents a final class.
     */
    @Untested
    public abstract val isFinal: Boolean

    /**
     * Returns true if the class this mirror represents has the `strictfp` modifier.
     *
     * NOTE: For unknown reasons the strictfp modifier seems to not be present in the Core Reflection modifiers, so
     * this is always false
     */
    @Untested
    public abstract val isStrict: Boolean

    /**
     * Returns true if the class this mirror represents is not final.
     */
    @Untested
    public abstract val isOpen: Boolean

    /**
     * Returns true if this object represents a Kotlin class and that class is a companion class
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath
     */
    public abstract val isCompanion: Boolean

    /**
     * Returns true if this object represents a Kotlin class and that class is a data class
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath
     */
    public abstract val isData: Boolean

    /**
     * Returns true if this object represents a Kotlin class and that class is a sealed class
     *
     * @throws KotlinReflectionNotSupportedError if `kotlin-reflect.jar` is not on the classpath
     */
    public abstract val isSealed: Boolean

    /**
     * Returns true if this mirror represents an annotation class.
     */
    @Untested
    public abstract val isAnnotation: Boolean

    /**
     * Returns true if this mirror represents an anonymous class.
     */
    @Untested
    public abstract val isAnonymous: Boolean

    /**
     * Returns true if this mirror represents an enum class. This is false for anonymous enum subclasses, so for more
     * consistent behavior check if [enumType] is non-null.
     */
    @Untested
    public abstract val isEnum: Boolean

    /**
     * Returns true if the class this mirror represents is an interface.
     */
    @Untested
    public abstract val isInterface: Boolean

    /**
     * Returns true if this mirror represents a local class. Local classes are classes declared within a block of code
     * such as a method or constructor.
     */
    @Untested
    public abstract val isLocal: Boolean

    /**
     * Returns true if the class this mirror represents is a member of another class. Member classes are non-static
     * classes defined inside another class.
     *
     * ```java
     * public class Foo {
     *     public class Member {}
     * }
     * ```
     */
    @Untested
    public abstract val isMember: Boolean

    /**
     * Returns true if this mirror represents a primitive class.
     */
    @Untested
    public abstract val isPrimitive: Boolean

    /**
     * Returns true if this mirror represents a synthetic class.
     */
    @Untested
    public abstract val isSynthetic: Boolean

    /**
     * Returns annotations that are present on the class this mirror represents. These are not the annotations
     * present on the use of the type, for those use [typeAnnotations]
     *
     * **Note: this list is immutable**
     *
     * @see Class.getAnnotations
     */
    @Untested
    public abstract val annotations: List<Annotation>

    /**
     * Returns annotations that are directly present on the class this mirror represents. These are not the annotations
     * present on the use of the type, for those use [typeAnnotations]
     *
     * **Note: this list is immutable**
     *
     * @see Class.getDeclaredAnnotations
     */
    @Untested
    public abstract val declaredAnnotations: List<Annotation>

    /**
     * Returns the logical enum type of the class this mirror represents, taking into account anonymous enum subclasses,
     * or null if this mirror does not represent an enum type or enum subclass.
     *
     * Anonymous enum subclasses (any enum element that overrides or implements a method from the enum itself) aren't
     * enum classes themselves. This method will return the true enum class for both the actual enum and the subclasses.
     */
    public abstract val enumType: ClassMirror?

    /**
     * Returns the list of constants in the enum class this mirror represents, or null if this mirror does not
     * represent an enum class. If this mirror represents an anonymous subclass of an enum, this will return null.
     *
     * **Note: this list is immutable**
     *
     * @see enumType
     * @see Class.enumConstants
     */
    public abstract val enumConstants: List<Enum<*>>?

    /**
     * Returns the simple name of the class this mirror represents. e.g. `"String"`
     *
     * @see [Class.getSimpleName]
     */
    @Untested
    public abstract val simpleName: String

    /**
     * Returns an informative string describing this type. e.g. `"java.lang.String"` or `"com.example.Foo$1"`
     *
     * @see [Class.getTypeName]
     */
    @Untested
    public abstract val name: String

    /**
     * Returns the simple fully qualified name of the class this mirror represents. e.g. `"java.lang.String"`. Returns
     * null if the underlying class does not have a canonical name (i.e., if it is a local or anonymous class)
     *
     * @see [Class.getCanonicalName]
     */
    @Untested
    public abstract val canonicalName: String?

    /**
     * Returns the internal name of the class this mirror represents. (e.g. `boolean` = `"Z"`,
     * `com.example.Foo` = `"Lcom/example/Foo;"`)
     */
    @Untested
    public abstract val jvmName: String
//endregion =====================================================================================================================

//region Methods ================================================================================================================
    /**
     * The methods declared directly in this class.
     * The returned list is in an arbitrary stable order.
     *
     * **Note: this list is immutable**
     *
     * @see Class.getDeclaredMethods
     */
    public abstract val declaredMethods: MethodList

    /**
     * The methods [inherited](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-8.4.8) from the
     * supertypes of this class.
     * The returned list is in an arbitrary stable order.
     *
     * **Note: this list is immutable**
     */
    public abstract val inheritedMethods: MethodList

    /**
     * The public methods declared in this class and inherited from its superclasses.
     * The returned list is in an arbitrary stable order.
     *
     * **Note: this list is immutable**
     *
     * @see Class.getMethods
     */
    public abstract val publicMethods: MethodList

    /**
     * The methods that would be visible inside of this class. This includes public and private methods from this class,
     * as well as any methods inherited from the supertypes of this class. This list will include hidden public static
     * methods, since they can't be overridden, only hidden.
     * The returned list is in an arbitrary stable order.
     *
     * **Note: This list is immutable.**
     *
     * @see inheritedMethods
     */
    @Untested
    public abstract val visibleMethods: MethodList

    /**
     * All the methods declared in this class and its supertypes, excluding overridden methods. This includes public and
     * private methods from this class and its superclasses/interfaces, as well as any methods from the supertypes of
     * this class. This list will include hidden private and static methods, since they can't be overridden, only
     * hidden.
     * The returned list is in an arbitrary stable order.
     *
     * **Note: This list is immutable.**
     */
    public abstract val methods: MethodList

    /**
     * Returns the specialized mirror that represents [other].
     * @throws NoSuchMirrorException if this type has no corresponding mirror
     */
    public abstract fun getMethod(other: Method): MethodMirror

    /**
     * Returns the public and private methods declared in this class or its superclasses that have the specified name,
     * excluding overridden methods. Since private methods can't be overridden, there may still be multiple methods with
     * the same signature in the returned list.
     *
     * **Note: The returned list is immutable.**
     *
     * @see methods
     */
    @Untested
    public abstract fun findMethods(name: String): List<MethodMirror>

    /**
     * Returns the public or private method declared in this class or its superclasses that has the specified signature,
     * excluding overridden methods, or null if no such method exists.
     *
     * @see methods
     */
    @Untested
    public abstract fun findMethod(name: String, vararg params: TypeMirror): MethodMirror?

    /**
     * Returns the public or private method declared in this class or its superclasses that has the specified raw
     * signature, excluding overridden methods, or null if no such method exists.
     *
     * @see methods
     */
    @Untested
    public abstract fun findMethodRaw(name: String, vararg params: Class<*>): MethodMirror?

    /**
     * Returns the public or private method declared in this class or its superclasses that has the specified signature,
     * excluding overridden methods, or throws if no such method exists.
     *
     * @see methods
     * @see findMethod
     * @throws NoSuchMirrorException if no method with the specified signature exists
     */
    @Untested
    public abstract fun getMethod(name: String, vararg params: TypeMirror): MethodMirror

    /**
     * Returns the public or private method declared in this class or its superclasses that has the specified raw
     * signature, excluding overridden methods, or throws if no such method exists.
     *
     * @see methods
     * @see findMethod
     * @throws NoSuchMirrorException if no method with the specified signature exists
     */
    @Untested
    public abstract fun getMethodRaw(name: String, vararg params: Class<*>): MethodMirror
//endregion =====================================================================================================================

//region Fields =================================================================================================================
    /**
     * The fields declared directly in this class.
     *
     * **Note: this list is immutable**
     *
     * @see Class.getDeclaredFields
     */
    public abstract val declaredFields: List<FieldMirror>

    /**
     * The public methods declared in this class and inherited from its superclasses.
     *
     * **Note: this list is immutable**
     *
     * @see Class.getFields
     */
    @Untested
    public abstract val publicFields: List<FieldMirror>

    /**
     * The fields declared in this class and its superclasses. Since fields can be shadowed but not overridden, there
     * may be multiple fields with the same name in this list.
     *
     * **Note: This list is immutable.**
     */
    @Untested
    public abstract val fields: List<FieldMirror>

    /**
     * Returns the specialized mirror that represents [other].
     * @throws NoSuchMirrorException if this type has no corresponding mirror
     */
    @Untested
    public abstract fun getField(other: Field): FieldMirror

    /**
     * Returns the field declared directly in this class that has the specified name, or null if no such field
     * exists.
     *
     * @see getDeclaredField
     */
    @Untested
    public abstract fun findDeclaredField(name: String): FieldMirror?

    /**
     * Returns the public field declared in this class or inherited from its superclasses that has the specified
     * name, or null if no such field exists.
     *
     * @see getPublicField
     */
    @Untested
    public abstract fun findPublicField(name: String): FieldMirror?

    /**
     * Returns the field declared in this class or inherited from its superclasses that has the specified name,
     * or null if no such field exists.
     *
     * @see getField
     */
    @Untested
    public abstract fun findField(name: String): FieldMirror?

    /**
     * Returns the field declared directly in this class that has the specified signature, or throws if no such field
     * exists.
     *
     * @see findDeclaredField
     * @throws NoSuchMirrorException if no field with the specified name exists
     */
    @Untested
    public abstract fun getDeclaredField(name: String): FieldMirror

    /**
     * Returns the public field declared in this class or inherited from its superclasses that has the specified name,
     * or throws if no such field exists.
     *
     * @see findPublicField
     * @throws NoSuchMirrorException if no field with the specified name exists
     */
    @Untested
    public abstract fun getPublicField(name: String): FieldMirror

    /**
     * Returns the field declared in this class or inherited from its superclasses that has the specified name, or
     * throws if no such field exists.
     *
     * @see findField
     * @throws NoSuchMirrorException if no field with the specified name exists
     */
    @Untested
    public abstract fun getField(name: String): FieldMirror

//endregion =====================================================================================================================

//region Constructors ===========================================================================================================
    /**
     * The constructors declared in this class.
     *
     * **Note: this list is immutable**
     *
     * @see Class.getDeclaredConstructors
     */
    public abstract val declaredConstructors: List<ConstructorMirror>

    /**
     * The public constructors declared in this class.
     *
     * **Note: this list is immutable**
     *
     * @see Class.getConstructors
     */
    @Untested
    public abstract val publicConstructors: List<ConstructorMirror>

    /**
     * Returns the specialized mirror that represents the same constructor as [other].
     * @throws NoSuchMirrorException if this type has no corresponding mirror
     */
    @Untested
    public abstract fun getConstructor(other: ConstructorMirror): ConstructorMirror

    /**
     * Gets the specialized mirror that represents [other].
     * @throws NoSuchMirrorException if this type has no corresponding mirror
     */
    @Untested
    public abstract fun getConstructor(other: Constructor<*>): ConstructorMirror

    /**
     * Returns the constructor declared in this class that has the specified parameter types, or null if no such
     * constructor exists.
     *
     * @see getDeclaredConstructor
     */
    @Untested
    public abstract fun findDeclaredConstructor(vararg params: TypeMirror): ConstructorMirror?

    /**
     * Returns the constructor declared in this class that has the specified parameter types, or throws if no such
     * constructor exists.
     *
     * @throws NoSuchMirrorException if no constructor with the specified parameter types exists
     */
    @Untested
    public abstract fun getDeclaredConstructor(vararg params: TypeMirror): ConstructorMirror
//endregion =====================================================================================================================

//region Member classes =========================================================================================================
    /**
     * The member classes declared directly in this class.
     *
     * **Note: this list is immutable**
     *
     * @see Class.getDeclaredClasses
     */
    public abstract val declaredMemberClasses: List<ClassMirror>

    /**
     * The public member classes declared in this class and inherited from its superclasses.
     *
     * **Note: this list is immutable**
     *
     * @see Class.getClasses
     */
    @Untested
    public abstract val publicMemberClasses: List<ClassMirror>

    /**
     * The member classes declared in this class and inherited from its superclasses. Since member classes can be
     * shadowed but not overridden, there may be multiple classes with the same name in this list.
     *
     * **Note: This list is immutable.**
     */
    @Untested
    public abstract val memberClasses: List<ClassMirror>

    /**
     * Returns the specialized mirror that represents the same class as [other].
     * @throws NoSuchMirrorException if this type has no corresponding mirror
     */
    @Untested
    public abstract fun getMemberClass(other: ClassMirror): ClassMirror

    /**
     * Returns the specialized mirror that represents [other].
     * @throws NoSuchMirrorException if this type has no corresponding mirror
     */
    @Untested
    public abstract fun getMemberClass(other: Class<*>): ClassMirror

    /**
     * Returns the member class declared directly in this class that has the specified name, or null if no such class
     * exists.
     *
     * @see getDeclaredMemberClass
     */
    @Untested
    public abstract fun findDeclaredMemberClass(name: String): ClassMirror?

    /**
     * Returns the public member class declared in this class or inherited from its superclasses that has the specified
     * name, or null if no such class exists.
     *
     * @see getPublicMemberClass
     */
    @Untested
    public abstract fun findPublicMemberClass(name: String): ClassMirror?

    /**
     * Returns the member class declared in this class or inherited from its superclasses that has the specified name,
     * or null if no such class exists.
     *
     * @see getMemberClass
     */
    @Untested
    public abstract fun findMemberClass(name: String): ClassMirror?

    /**
     * Returns the member class declared directly in this class that has the specified signature, or throws if no such
     * class exists.
     *
     * @see findDeclaredMemberClass
     * @throws NoSuchMirrorException if no class with the specified name exists
     */
    @Untested
    public abstract fun getDeclaredMemberClass(name: String): ClassMirror?

    /**
     * Returns the public member class declared in this class or inherited from its superclasses that has the specified
     * name, or throws if no such class exists.
     *
     * @see findPublicMemberClass
     * @throws NoSuchMirrorException if no class with the specified name exists
     */
    @Untested
    public abstract fun getPublicMemberClass(name: String): ClassMirror?

    /**
     * Returns the member class declared in this class or inherited from its superclasses that has the specified name,
     * or throws if no such class exists.
     *
     * @see findMemberClass
     * @throws NoSuchMirrorException if no class with the specified name exists
     */
    @Untested
    public abstract fun getMemberClass(name: String): ClassMirror?
//endregion =====================================================================================================================

    /**
     * Returns true if the specified annotation is present on this class.
     *
     * @see AnnotatedElement.isAnnotationPresent
     */
    public inline fun <reified T: Annotation> isAnnotationPresent(): Boolean = this.isAnnotationPresent(T::class.java)

    /**
     * Returns the annotation of the specified type, or null if no such annotation is present.
     *
     * @see AnnotatedElement.getAnnotation
     */
    public inline fun <reified T: Annotation> getAnnotation(): T? = this.getAnnotation(T::class.java)

    /**
     * Returns the annotation of the specified type, or null if no such annotation is _directly_ present on this class.
     *
     * @see AnnotatedElement.getDeclaredAnnotation
     */
    public inline fun <reified T: Annotation> getDeclaredAnnotation(): T? = this.getDeclaredAnnotation(T::class.java)

    /**
     * Returns a string representing the declaration of this type with type parameters substituted in,
     * as opposed to [toString] which returns the string representing the usage of this type
     */
    @Untested
    public abstract val declarationString: String

    /**
     * A set of useful flags for classes, such as whether it is abstract, anonymous, primitive, etc.
     */
    public enum class Flag {
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