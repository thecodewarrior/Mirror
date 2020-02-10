package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.NoSuchMirrorException
import dev.thecodewarrior.mirror.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.coretypes.TypeImplAccess
import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.member.ExecutableMirror
import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.JvmModifier
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.member.Modifier
import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.checkedCast
import dev.thecodewarrior.mirror.utils.uniqueBy
import dev.thecodewarrior.mirror.utils.unmodifiableView
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility

@Suppress("NO_REFLECTION_IN_CLASS_PATH")
internal class ClassMirrorImpl internal constructor(
    override val cache: MirrorCache,
    override val java: Class<*>,
    raw: ClassMirror?,
    override val specialization: TypeSpecialization.Class?
): ClassMirror() {
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

    override val raw: ClassMirror = raw ?: this

//region Specialization =========================================================================================================

    override val superclass: ClassMirror? by lazy {
        java.annotatedSuperclass?.let {
            this.genericMapping[cache.types.reflect(it)] as ClassMirror
        }
    }

    override val interfaces: List<ClassMirror> by lazy {
        java.annotatedInterfaces.map {
            this.genericMapping[cache.types.reflect(it)] as ClassMirror
        }.unmodifiableView()
    }

    override val typeParameters: List<TypeMirror> by lazy {
        specialization?.arguments ?: java.typeParameters.map { cache.types.reflect(it) }.unmodifiableView()
    }

    override val enclosingClass: ClassMirror? by lazy {
        specialization?.enclosingClass ?: java.enclosingClass?.let { cache.types.reflect(it) as ClassMirror }
    }

    override val enclosingExecutable: ExecutableMirror? by lazy {
        specialization?.enclosingExecutable ?: (java.enclosingMethod ?: java.enclosingConstructor)?.let { cache.executables.reflect(it) }
    }

    override val genericMapping: TypeMapping by lazy {
        TypeMapping(this.raw.typeParameters.zip(typeParameters).associate { it }) +
            enclosingClass?.genericMapping + enclosingExecutable?.genericMapping
    }

    override fun defaultSpecialization() = TypeSpecialization.Class.DEFAULT

    override fun withTypeArguments(vararg parameters: TypeMirror): ClassMirror {
        if(parameters.size != typeParameters.size && parameters.isNotEmpty())
            throw InvalidSpecializationException("Passed parameter count ${parameters.size} is different from class type " +
                    "parameter count ${typeParameters.size}")
        val newSpecialization = (specialization ?: defaultSpecialization())
            .copy(arguments = if(parameters.isEmpty()) null else parameters.toList())
        return cache.types.specialize(raw, newSpecialization) as ClassMirror
    }

    override fun withEnclosingClass(enclosing: ClassMirror?): ClassMirror {
        if(enclosing != null && enclosing.raw != raw.enclosingClass)
            throw InvalidSpecializationException("Passed enclosing class ($enclosing) is not equal to or a " +
                "specialization of this class's enclosing class (${raw.enclosingClass})")
        val newSpecialization = (specialization ?: defaultSpecialization()).copy(enclosingClass = enclosing)
        return cache.types.specialize(raw, newSpecialization) as ClassMirror
    }

    override fun withEnclosingExecutable(enclosing: ExecutableMirror?): ClassMirror {
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
            ClassMirrorImpl(cache, java, raw, it)
        }
    }
//endregion =====================================================================================================================

//region Relationships ==========================================================================================================
    override fun findSuperclass(clazz: Class<*>): ClassMirror? {
        if(java == clazz) return this
        var supertype = superclass?.findSuperclass(clazz)
        for(it in interfaces) {
            val candidate = it.findSuperclass(clazz)
            if(candidate != null && (supertype == null || supertype.specificity < candidate.specificity))
                supertype = candidate
        }
        return supertype
    }

    override fun getSuperclass(clazz: Class<*>): ClassMirror {
        return findSuperclass(clazz)
            ?: throw NoSuchMirrorException("$this has no superclass of type ${clazz.simpleName}")
    }

//endregion =====================================================================================================================

//region Simple helpers =========================================================================================================
    override val kClass: KClass<*> = java.kotlin

    override val modifiers: Set<Modifier> = Modifier.fromModifiers(java.modifiers).unmodifiableView()
    override val access: Modifier.Access = Modifier.Access.fromModifiers(java.modifiers)
    override val isInternalAccess: Boolean get() = kClass.visibility == KVisibility.INTERNAL

    override val flags: Set<Flag> = sequenceOf(
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

    // java
    override val isAbstract: Boolean = Flag.ABSTRACT in flags
    override val isStatic: Boolean = Flag.STATIC in flags
    override val isFinal: Boolean = Flag.FINAL in flags
    override val isStrict: Boolean = Flag.STRICT in flags

    // kotlin
    override val isOpen: Boolean = !isFinal
    override val isCompanion: Boolean get() = kClass.isCompanion
    override val isData: Boolean get() = kClass.isData
    override val isSealed: Boolean get() = kClass.isSealed

    // class type
    override val isAnnotation: Boolean = Flag.ANNOTATION in flags
    override val isAnonymous: Boolean = Flag.ANONYMOUS in flags
    override val isEnum: Boolean = Flag.ENUM in flags
    override val isInterface: Boolean = Flag.INTERFACE in flags
    override val isLocal: Boolean = Flag.LOCAL in flags
    override val isMember: Boolean = Flag.MEMBER in flags
    override val isPrimitive: Boolean = Flag.PRIMITIVE in flags
    override val isSynthetic: Boolean = Flag.SYNTHETIC in flags

    override val annotations: List<Annotation> = java.annotations.toList().unmodifiableView()
    override val declaredAnnotations: List<Annotation> = java.declaredAnnotations.toList().unmodifiableView()

    override val enumType: ClassMirror? by lazy {
        when {
            this.isEnum -> this
            this.superclass?.isEnum == true -> this.superclass
            else -> null
        }
    }
    override val enumConstants: List<Enum<*>>? = java.enumConstants?.toList()?.checkedCast<Enum<*>>()?.unmodifiableView()

    override val simpleName: String = java.simpleName
    override val name: String = java.name
    override val canonicalName: String? = java.canonicalName

//endregion

//region Methods ================================================================================================================
    override val declaredMethods: List<MethodMirror> by lazy {
        java.declaredMethods.map {
            cache.executables.reflect(it).withDeclaringClass(this) as MethodMirror
        }.unmodifiableView()
    }

    override val publicMethods: List<MethodMirror> by lazy {
        java.methods.map { this.getMethod(it) }.unmodifiableView()
    }

    override val methods: List<MethodMirror> by lazy {
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

        return@lazy allMethods.map { this.getMethod(it) }.unmodifiableView()
    }

    override fun getMethod(other: MethodMirror): MethodMirror = getMethod(other.java)

    override fun getMethod(other: Method): MethodMirror {
        if(other.declaringClass == this.java) {
            return declaredMethods.find { it.java == other }
                ?: throw NoSuchMirrorException("Could not find method ${other.name}(${other.parameterTypes.joinToString(", ")}) " +
                    "in $this")
        }
        val superclass = findSuperclass(other.declaringClass)
            ?: throw NoSuchMirrorException("Could not find superclass ${other.declaringClass.simpleName} for method " +
                "${other.name}(${other.parameterTypes.joinToString(", ")}) in $this")
        return try {
            superclass.getMethod(other)
        } catch (e: NoSuchMirrorException) {
            throw NoSuchMirrorException("Could not find method ${other.declaringClass.simpleName}.${other.name}" +
                "(${other.parameterTypes.joinToString(", ")}) in $this", e)
        }
    }

    override fun findDeclaredMethods(name: String): List<MethodMirror> {
        return declaredMethods.filter { it.name == name }.unmodifiableView()
    }

    private val publicMethodNameCache = ConcurrentHashMap<String, List<MethodMirror>>()
    override fun findPublicMethods(name: String): List<MethodMirror> {
        return publicMethodNameCache.getOrPut(name) {
            val methods = mutableListOf<MethodMirror>()
            //todo: this shouldn't return private methods. write a test to fail this.
            methods.addAll(declaredMethods.filter { it.name == name })
            //todo: this should return methods from interfaces. write a test to fail this.
            methods.addAll(superclass?.findPublicMethods(name) ?: emptyList())
            return@getOrPut methods.unmodifiableView()
        }
    }

    private val methodNameCache = ConcurrentHashMap<String, List<MethodMirror>>()
    override fun findMethods(name: String): List<MethodMirror> {
        return methodNameCache.getOrPut(name) {
            val methods = mutableListOf<MethodMirror>()
            methods.addAll(declaredMethods.filter { it.name == name })
            //todo: this should return methods from interfaces. write a test to fail this.
            methods.addAll(superclass?.findMethods(name) ?: emptyList())
            return@getOrPut methods.unmodifiableView()
        }
    }

    override fun findDeclaredMethod(name: String, vararg params: TypeMirror): MethodMirror? {
        TODO("it")
    }

    override fun findPublicMethod(name: String, vararg params: TypeMirror): MethodMirror? {
        TODO("it")
    }

    override fun findMethod(name: String, vararg params: TypeMirror): MethodMirror? {
        TODO("it")
    }

    override fun getDeclaredMethod(name: String, vararg params: TypeMirror): MethodMirror? {
        TODO("it")
    }

    override fun getPublicMethod(name: String, vararg params: TypeMirror): MethodMirror? {
        TODO("it")
    }

    override fun getMethod(name: String, vararg params: TypeMirror): MethodMirror? {
        TODO("it")
    }

//endregion =====================================================================================================================

//region Fields =================================================================================================================
    override val declaredFields: List<FieldMirror> by lazy {
        java.declaredFields.map {
            cache.fields.reflect(it).withDeclaringClass(this)
        }.unmodifiableView()
    }
    override val publicFields: List<FieldMirror> by lazy { java.fields.mapNotNull { getField(it) }.unmodifiableView() }
    override val fields: List<FieldMirror> by lazy {
        (declaredFields + (superclass?.fields ?: emptyList())).uniqueBy { it.name }.unmodifiableView()
    }

    override fun getField(other: FieldMirror): FieldMirror = getField(other.java)
    override fun getField(other: Field): FieldMirror {
        if(other.declaringClass == this.java) {
            return declaredFields.find { it.java == other }
                ?: throw NoSuchMirrorException("Could not find field ${other.name} in $this")
        }
        val superclass = findSuperclass(other.declaringClass)
            ?: throw NoSuchMirrorException("Could not find superclass ${other.declaringClass.simpleName} for field ${other.name} in $this")
        return try {
            superclass.getField(other)
        } catch (e: NoSuchMirrorException) {
            throw NoSuchMirrorException("Could not find field ${other.declaringClass.simpleName}.${other.name} in $this", e)
        }
    }

    override fun findDeclaredField(name: String): FieldMirror? {
        return declaredFields.find { it.name == name }
    }

    private val publicFieldNameCache = ConcurrentHashMap<String, FieldMirror?>()
    override fun findPublicField(name: String): FieldMirror? {
        return publicFieldNameCache.getOrPut(name) {
            var field: FieldMirror? = null
            field = field ?: declaredFields.find { it.name == name }
            field = field ?: superclass?.findPublicField(name)
            return@getOrPut field
        }
    }

    private val fieldNameCache = ConcurrentHashMap<String, FieldMirror?>()
    override fun findField(name: String): FieldMirror? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDeclaredField(name: String): FieldMirror? {
        return findDeclaredField(name)
            ?: throw NoSuchMirrorException("Could not find field with name $name declared in $this")
    }

    override fun getPublicField(name: String): FieldMirror? {
        return findPublicField(name)
            ?: throw NoSuchMirrorException("Could not find public field with name $name in $this")
    }

    override fun getField(name: String): FieldMirror? {
        return findField(name)
            ?: throw NoSuchMirrorException("Could not find field with name $name in $this")
    }
//endregion =====================================================================================================================

//region Constructors ===========================================================================================================
    override val declaredConstructors: List<ConstructorMirror> by lazy {
        java.declaredConstructors.map {
            cache.executables.reflect(it).withDeclaringClass(this) as ConstructorMirror
        }.unmodifiableView()
    }
    override val publicConstructors: List<ConstructorMirror> by lazy {
        java.constructors.mapNotNull { getConstructor(it) }.unmodifiableView()
    }

    override fun getConstructor(other: ConstructorMirror): ConstructorMirror = getConstructor(other.java)
    override fun getConstructor(other: Constructor<*>): ConstructorMirror {
        if(other.declaringClass == this.java) {
            return declaredConstructors.find { it.java == other }
                ?: throw NoSuchMirrorException("Could not find constructor (${other.parameterTypes.joinToString(", ")}) " +
                    "in $this")
        }
        throw NoSuchMirrorException("Can't get constructor ${other.declaringClass.simpleName}" +
            "(${other.parameterTypes.joinToString(", ")}) from a superclass in $this")
    }

    override fun findDeclaredConstructor(vararg params: TypeMirror): ConstructorMirror? {
        val match = params.toList()
        return declaredConstructors.find { it.parameterTypes == match }
    }

    override fun getDeclaredConstructor(vararg params: TypeMirror): ConstructorMirror {
        return findDeclaredConstructor(*params)
            ?: throw NoSuchMirrorException("No constructor found in $this with parameters (${params.joinToString(", ")})")
    }
//endregion =====================================================================================================================

//region Member classes =========================================================================================================
    override val declaredMemberClasses: List<ClassMirror> by lazy {
        java.declaredClasses.map {
            (cache.types.reflect(it) as ClassMirror).withEnclosingClass(this)
        }.unmodifiableView()
    }
    override val publicMemberClasses: List<ClassMirror> by lazy { java.classes.mapNotNull { getMemberClass(it) }.unmodifiableView() }
    override val memberClasses: List<ClassMirror> by lazy {
        sequenceOf(
            declaredMemberClasses,
            superclass?.memberClasses ?: emptyList(),
            *interfaces.map { it.memberClasses }.toTypedArray()
        ).flatten().toList().uniqueBy { it.simpleName }.unmodifiableView()
    }

    override fun getMemberClass(other: ClassMirror): ClassMirror = getMemberClass(other.java)
    override fun getMemberClass(other: Class<*>): ClassMirror {
        if(other.declaringClass == this.java) {
            return declaredMemberClasses.find { it.java == other }
                ?: throw NoSuchMirrorException("Could not find member class ${other.name} in $this")
        }
        val superclass = findSuperclass(other.declaringClass)
            ?: throw NoSuchMirrorException("Could not find superclass ${other.declaringClass.simpleName} for member class ${other.name} in $this")
        return try {
            superclass.getMemberClass(other)
        } catch (e: NoSuchMirrorException) {
            throw NoSuchMirrorException("Could not find member class ${other.declaringClass.simpleName}.${other.name} in $this", e)
        }
    }

    override fun findDeclaredMemberClass(name: String): ClassMirror? {
        return declaredMemberClasses.find { it.java.simpleName == name }
    }

    private val publicMemberClassCache = ConcurrentHashMap<String, ClassMirror?>()
    override fun findPublicMemberClass(name: String): ClassMirror? {
        return publicMemberClassCache.getOrPut(name) {
            return@getOrPut publicMemberClasses.find { it.java.simpleName == name }
                ?: superclass?.also { it.findPublicMemberClass(name) }
                ?: interfaces.map { it.findPublicMemberClass(name) }.find { it != null }
        }
    }

    private val memberClassCache = ConcurrentHashMap<String, ClassMirror?>()
    override fun findMemberClass(name: String): ClassMirror? {
        return memberClassCache.getOrPut(name) {
            return@getOrPut memberClasses.find { it.java.simpleName == name } ?:
            superclass?.also { it.findMemberClass(name) } ?:
            interfaces.map { it.findMemberClass(name) }.find { it != null }
        }
    }

    override fun getDeclaredMemberClass(name: String): ClassMirror? {
        return findDeclaredMemberClass(name)
            ?: throw NoSuchMirrorException("Could not find member class with name $name declared in $this")
    }

    override fun getPublicMemberClass(name: String): ClassMirror? {
        return findPublicMemberClass(name)
            ?: throw NoSuchMirrorException("Could not find public member class with name $name in $this")
    }

    override fun getMemberClass(name: String): ClassMirror? {
        return findMemberClass(name)
            ?: throw NoSuchMirrorException("Could not find member class with name $name in $this")
    }
//endregion =====================================================================================================================


//region TypeMirror =============================================================================================================
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
//endregion =====================================================================================================================

    override val declarationString: String
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

    @Untested
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
