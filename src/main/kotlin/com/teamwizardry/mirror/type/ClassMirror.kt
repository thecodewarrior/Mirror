package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.member.ConstructorMirror
import com.teamwizardry.mirror.member.ExecutableMirror
import com.teamwizardry.mirror.member.FieldMirror
import com.teamwizardry.mirror.member.MethodMirror
import com.teamwizardry.mirror.utils.unmodifiable
import io.leangen.geantyref.GenericTypeReflector
import io.leangen.geantyref.TypeFactory
import java.lang.reflect.AnnotatedType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
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
        if(params != null) {
            coreType = TypeFactory.parameterizedInnerClass(
                specialization.enclosingClass?.coreType, java,
                *params.map { it.coreType }.toTypedArray()
            )
        } else {
            coreType = TypeFactory.innerClass(specialization?.enclosingClass?.coreType, java)
        }

        if(coreType is ParameterizedType && params != null) {
           coreAnnotatedType = TypeFactory.parameterizedAnnotatedClass(
                specialization.enclosingClass?.coreType, java, typeAnnotations.toTypedArray(),
                *params.map { it.coreAnnotatedType }.toTypedArray()
            )
        } else {
            coreAnnotatedType = GenericTypeReflector.annotate(coreType, typeAnnotations.toTypedArray())
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
        }.unmodifiable()
    }

    /**
     * The list of type parameters defined by this mirror. These will be replaced when specializing, so you should use
     * [raw] to get the actual type parameters of the class as opposed to their specializations.
     */
    val typeParameters: List<TypeMirror> by lazy {
        specialization?.arguments ?: java.typeParameters.map { cache.types.reflect(it) }.unmodifiable()
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
     * supertypes/interfaces, method and field signatures, etc. Passing zero arguments will remove the current type
     * arguments without replacing them.
     *
     * @throws InvalidSpecializationException if the passed type list is not the same length as [typeParameters] or zero
     * @return A copy of this type with its type parameters replaced
     */
    fun withTypeArguments(vararg parameters: TypeMirror): ClassMirror {
        if(parameters.size != typeParameters.size && parameters.isEmpty())
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
        if(enclosing == null) {
            val newSpecialization = (specialization ?: defaultSpecialization()).copy(enclosingExecutable = null)
            return cache.types.specialize(raw, newSpecialization) as ClassMirror
        }
        if(enclosing.raw != raw.enclosingExecutable)
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
    val declaredClasses: List<ClassMirror> by lazy {
        java.declaredClasses.map {
            (cache.types.reflect(it) as ClassMirror).withEnclosingClass(this)
        }.unmodifiable()
    }

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
//endregion

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
}

