package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.member.ConstructorMirror
import com.teamwizardry.mirror.member.ExecutableMirror
import com.teamwizardry.mirror.member.FieldMirror
import com.teamwizardry.mirror.member.MethodMirror
import com.teamwizardry.mirror.utils.unmodifiable
import java.util.concurrent.ConcurrentHashMap

/**
 * A type mirror representing a Java class. Classes are the only type mirror that supports manual specialization as
 * they are the only types that have generic type parameters.
 *
 * @see TypeMirror
 */
class ClassMirror internal constructor(
    override val cache: MirrorCache,
    override val java: Class<*>,
    raw: ClassMirror?,
    override val specialization: TypeSpecialization.Class?
): ConcreteTypeMirror() {

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
    }

//region Supertypes
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
        }.unmodifiable()
    }

    /**
     * The list of type parameters defined by this mirror. These will be replaced when specializing, so you should use
     * [raw] to get the actual type parameters of the class as opposed to their specializations.
     */
    val typeParameters: List<TypeMirror> by lazy {
        specialization?.arguments ?: java.typeParameters.map { cache.types.reflect(it) }.unmodifiable()
    }

    /**
     * The raw, unspecialized version of this mirror.
     */
    override val raw: ClassMirror = raw ?: this

    override fun defaultSpecialization() = TypeSpecialization.Class.DEFAULT

    /**
     * Specializes this class, replacing its type parameters the given types. This will ripple the changes down to
     * supertypes/interfaces, method and field signatures, etc.
     *
     * @throws InvalidSpecializationException if the passed type list is not the same length as [typeParameters]
     * @return The specialized version of this type
     */
    fun specialize(vararg parameters: TypeMirror): ClassMirror {
        if(parameters.size != typeParameters.size)
            throw InvalidSpecializationException("Passed parameter count ${parameters.size} is different from class type " +
                    "parameter count ${typeParameters.size}")
        val newSpecialization = (specialization ?: defaultSpecialization()).copy(arguments = parameters.toList())
        return cache.types.specialize(raw, newSpecialization) as ClassMirror
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
     * Specializes this class, replacing its enclosing class with the passed class. If the passed class is null this
     * method removes any enclosing class specialization.
     *
     * @throws InvalidSpecializationException if the passed class is not equal to or a specialization of this class's
     * raw enclosing class, or if this class has no enclosing class and the passed class is not null
     * @return The specialized version of this class
     */
    fun enclose(enclosing: ClassMirror?): ClassMirror {
        if(enclosing == null) {
            val newSpecialization = (specialization ?: defaultSpecialization()).copy(enclosingClass = null)
            return cache.types.specialize(raw, newSpecialization) as ClassMirror
        }
        if(enclosing.raw != raw.enclosingClass)
            throw InvalidSpecializationException("Passed enclosing class ($enclosing) is not equal to or a " +
                "specialization of this class's enclosing class (${raw.enclosingClass})")
        val newSpecialization = (specialization ?: defaultSpecialization()).copy(enclosingClass = enclosing)
        return cache.types.specialize(raw, newSpecialization) as ClassMirror
    }

    /**
     * Specializes this class, replacing its enclosing method/constructor with the passed executable and its
     * enclosing class with the passed executable's enclosing class. If the passed executable is null this method
     * removes any enclosing executable specialization but leaves the enclosing class specialization alone. To remove
     * the enclosing class specialization, pass null to [enclose].
     *
     * @throws InvalidSpecializationException if the passed executable is not equal to or a specialization of this
     * class's raw enclosing method, or if this class has no enclosing executable and the passed method is not null
     * @return The specialized version of this class
     */
    fun specializeEnclosingExecutable(enclosing: ExecutableMirror?): ClassMirror {
        if(enclosing == null) {
            val newSpecialization = (specialization ?: defaultSpecialization()).copy(enclosingExecutable = null)
            return cache.types.specialize(raw, newSpecialization) as ClassMirror
        }
        if(enclosing.raw != raw.enclosingExecutable)
            throw InvalidSpecializationException("Passed enclosing executable ($enclosing) is not equal to or a " +
                "specialization of this class's enclosing executable (${raw.enclosingExecutable})")
        val newSpecialization = (specialization ?: defaultSpecialization()).copy(
            enclosingClass = enclosing.declaringClass, enclosingExecutable = enclosing)
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
//endregion

//region Inner Classes
    /**
     * The inner classes declared directly inside of this class.
     *
     * This list is created when it is first accessed and is thread safe.
     */
    val declaredClasses: List<ClassMirror> by lazy {
        java.declaredClasses.map {
            (cache.types.reflect(it) as ClassMirror).enclose(this)
        }.unmodifiable()
    }

    fun declaredClass(name: String): ClassMirror? {
        return declaredClasses.find { it.java.simpleName == name }
    }

    private val declaredClassCache = ConcurrentHashMap<String, List<ClassMirror>>()

    fun innerClasses(name: String): List<ClassMirror> {
        return declaredClassCache.getOrPut(name) {
            val list = mutableListOf<ClassMirror>()
            declaredClasses.find { it.java.simpleName == name }?.also { list.add(it) }
            superclass?.also { list.addAll(it.innerClasses(name)) }
            return@getOrPut list.unmodifiable()
        }
    }
//endregion

//region Fields
    /**
     * The fields declared directly inside of this class, any fields inherited from superclasses will not appear in
     * this list.
     *
     * This list is created when it is first accessed and is thread safe.
     */
    val declaredFields: List<FieldMirror> by lazy {
        java.declaredFields.map {
            cache.fields.reflect(it).specialize(this)
        }.unmodifiable()
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
//endregion

//region Methods
    /**
     * The methods declared directly inside of this class, any methods inherited from superclasses will not appear in
     * this list.
     *
     * This list is created when it is first accessed and is thread safe.
     */
    val declaredMethods: List<MethodMirror> by lazy {
        java.declaredMethods.map {
            cache.executables.reflect(it).enclose(this) as MethodMirror
        }.unmodifiable()
    }

    fun declaredMethods(name: String): List<MethodMirror> {
        return declaredMethods.filter { it.name == name }.unmodifiable()
    }

    private val methodNameCache = ConcurrentHashMap<String, List<MethodMirror>>()

    fun methods(name: String): List<MethodMirror> {
        return methodNameCache.getOrPut(name) {
            val methods = mutableListOf<MethodMirror>()
            methods.addAll(declaredMethods.filter { it.name == name })
            methods.addAll(superclass?.methods(name) ?: emptyList())
            return@getOrPut methods.unmodifiable()
        }
    }
//endregion

//region Constructors
    /**
     * The constructors declared directly inside this class
     *
     * This list is created when it is first accessed and is thread safe.
     */
    val declaredConstructors: List<ConstructorMirror> by lazy {
        java.declaredConstructors.map {
            cache.executables.reflect(it).enclose(this) as ConstructorMirror
        }.unmodifiable()
    }

    fun declaredConstructor(vararg params: TypeMirror): ConstructorMirror? {
        val match = params.toList()
        return declaredConstructors.find { it.parameterTypes == match }
    }
//endregion

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
}

