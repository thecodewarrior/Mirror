package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
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
        if(specialization?.enclosingMethod != null && specialization.enclosingMethod.java != java.enclosingMethod)
            throw InvalidSpecializationException("Invalid enclosing method ${specialization.enclosingMethod} " +
                "for type $java. Expected enclosing method is ${java.enclosingMethod}")
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
     * Specializes this class, replacing its enclosing method with the passed method and its enclosing class with the
     * passed method's enclosing class. If the passed method is null this method removes any enclosing method
     * specialization but leaves the enclosing class specialization alone. To remove the enclosing class
     * specialization, pass null to [enclose].
     *
     * @throws InvalidSpecializationException if the passed method is not equal to or a specialization of this class's
     * raw enclosing method, or if this class has no enclosing method and the passed method is not null
     * @return The specialized version of this class
     */
    fun specializeEnclosingMethod(enclosing: MethodMirror?): ClassMirror {
        if(enclosing == null) {
            val newSpecialization = (specialization ?: defaultSpecialization()).copy(enclosingMethod = null)
            return cache.types.specialize(raw, newSpecialization) as ClassMirror
        }
        if(enclosing.raw != raw.enclosingMethod)
            throw InvalidSpecializationException("Passed enclosing method ($enclosing) is not equal to or a " +
                "specialization of this class's enclosing method (${raw.enclosingMethod})")
        val newSpecialization = (specialization ?: defaultSpecialization()).copy(
            enclosingClass = enclosing.enclosingClass, enclosingMethod = enclosing)
        return cache.types.specialize(raw, newSpecialization) as ClassMirror
    }

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Class>(
            specialization,
            { (it.arguments == this.typeParameters || it.arguments == null ) &&
                (it.enclosingClass == raw.enclosingClass || it.enclosingClass == null) &&
                (it.enclosingMethod == raw.enclosingMethod || it.enclosingMethod == null)
            }
        ) {
            ClassMirror(cache, java, raw, it)
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
            cache.methods.reflect(it).enclose(this)
        }.unmodifiable()
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

    val enclosingClass: ClassMirror? by lazy {
        specialization?.enclosingClass ?: java.enclosingClass?.let { cache.types.reflect(it) as ClassMirror }
    }

    val enclosingMethod: MethodMirror? by lazy {
        specialization?.enclosingMethod ?: java.enclosingMethod?.let { cache.methods.reflect(it) }
    }

    val genericMapping: TypeMapping by lazy {
        TypeMapping(this.raw.typeParameters.zip(typeParameters).associate { it }) +
            enclosingClass?.genericMapping + enclosingMethod?.genericMapping
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

