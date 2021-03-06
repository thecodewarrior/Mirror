package dev.thecodewarrior.mirror.impl.type

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.NoSuchMirrorException
import dev.thecodewarrior.mirror.impl.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.impl.coretypes.TypeImplAccess
import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.member.ExecutableMirror
import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.member.Modifier
import dev.thecodewarrior.mirror.type.*
import dev.thecodewarrior.mirror.type.ClassMirror.Flag
import dev.thecodewarrior.mirror.impl.TypeMapping
import dev.thecodewarrior.mirror.impl.member.ExecutableMirrorImpl
import dev.thecodewarrior.mirror.impl.util.ElementBackedAnnotationListImpl
import dev.thecodewarrior.mirror.type.MethodList
import dev.thecodewarrior.mirror.impl.utils.Untested
import dev.thecodewarrior.mirror.impl.utils.checkedCast
import dev.thecodewarrior.mirror.impl.utils.jvmName
import dev.thecodewarrior.mirror.impl.utils.unique
import dev.thecodewarrior.mirror.impl.utils.uniqueBy
import dev.thecodewarrior.mirror.impl.utils.unmodifiableView
import dev.thecodewarrior.mirror.util.AnnotationList
import dev.thecodewarrior.mirror.util.MirrorUtils
import java.lang.reflect.AnnotatedElement
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
): TypeMirrorImpl(), ClassMirror {
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

    val genericMapping: TypeMapping by lazy {
        TypeMapping(this.raw.typeParameters.zip(typeParameters).associate { it }) +
                (enclosingClass as ClassMirrorImpl?)?.genericMapping +
                (enclosingExecutable as ExecutableMirrorImpl?)?.genericMapping
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

        val stripped = enclosing?.withTypeAnnotations(emptyList())
        if(this.isStatic && stripped != raw.enclosingClass)
            throw InvalidSpecializationException("Static member classes can't be specialized for an enclosing class")

        val newSpecialization = (specialization ?: defaultSpecialization()).copy(enclosingClass = stripped)
        return cache.types.specialize(raw, newSpecialization) as ClassMirror
    }

    override fun withEnclosingExecutable(enclosing: ExecutableMirror?): ClassMirror {
        if(enclosing != null && enclosing.raw != raw.enclosingExecutable)
            throw InvalidSpecializationException("Passed enclosing executable ($enclosing) is not equal to or a " +
                "specialization of this class's enclosing executable (${raw.enclosingExecutable})")
        val newSpecialization = (specialization ?: defaultSpecialization()).copy(enclosingExecutable = enclosing)
        return cache.types.specialize(raw, newSpecialization) as ClassMirror
    }

    override fun withTypeAnnotations(annotations: List<Annotation>): ClassMirror {
        return withTypeAnnotationsImpl(annotations) as ClassMirror
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
            if(candidate != null && (supertype == null || TypeSpecificityComparator.compare(supertype, candidate) < 0))
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
    @Untested
    override val isKotlinClass: Boolean by lazy {
        declaredAnnotations.isPresent<Metadata>()
    }

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

    override val annotations: AnnotationList by lazy {
        ElementBackedAnnotationListImpl(java, false)
    }

    override val declaredAnnotations: AnnotationList by lazy {
        ElementBackedAnnotationListImpl(java, false)
    }


    override val enumType: ClassMirror? by lazy {
        when {
            this.isEnum -> this
            this.superclass?.isEnum == true -> this.superclass
            else -> null
        }
    }
    override val enumConstants: List<Enum<*>>? = java.enumConstants?.toList()?.checkedCast<Enum<*>>()?.unmodifiableView()

    override val simpleName: String = java.simpleName
    override val name: String = java.typeName
    override val jvmName: String = java.jvmName
    override val canonicalName: String? = java.canonicalName

//endregion

//region Methods ================================================================================================================
    override val declaredMethods: MethodList by lazy {
        MethodListImpl(this, "declared", MirrorUtils.stableSort(java.declaredMethods).map {
            cache.executables.reflect(it).withDeclaringClass(this) as MethodMirror
        })
    }

    /**
     * # Java Language Specification [§8.4.8](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-8.4.8)
     *
     * A class C inherits from its direct superclass all concrete methods m (both static and instance) of the superclass
     * for which all of the following are true:
     *
     * - m is a member of the direct superclass of C.
     * - m is public, protected, or declared with package access in the same package as C.
     * - No method declared in C has a signature that is a subsignature
     * ([§8.4.2](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-8.4.2)) of the signature of m.
     *
     * A class C inherits from its direct superclass and direct superinterfaces all abstract and default (§9.4) methods
     * m for which all of the following are true:
     *
     * - m is a member of the direct superclass or a direct superinterface, D, of C.
     * - m is public, protected, or declared with package access in the same package as C.
     * - No method declared in C has a signature that is a subsignature
     * ([§8.4.2](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-8.4.2)) of the signature of m.
     * - No concrete method inherited by C from its direct superclass has a signature that is a subsignature of the
     * signature of m.
     * - There exists no method m' that is a member of the direct superclass or a direct superinterface, D', of C
     * (m distinct from m', D distinct from D'), such that m' overrides from D'
     * ([§8.4.8.1](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-8.4.8.1),
     * [§9.4.1.1](https://docs.oracle.com/javase/specs/jls/se13/html/jls-8.html#jls-9.4.1.1)) the declaration of the
     * method m.
     *
     * A class does not inherit private or static methods from its superinterfaces.
     */
    override val inheritedMethods: MethodList by lazy {
        fun overrides(method: MethodMirror, base: MethodMirror)
            = base !== method && base.name == method.name &&
            base.declaringClass.isAssignableFrom(method.declaringClass) &&
            base.erasedParameterTypes == method.erasedParameterTypes

        val supertypeMethods = superclass?.visibleMethods.orEmpty() + interfaces.flatMap { it.visibleMethods }.filter { !it.isStatic }
        val abstractInherited = supertypeMethods.unique().filter { method ->
            if(method.access == Modifier.Access.PRIVATE)
                return@filter false
            if(method.access == Modifier.Access.DEFAULT && java.`package` != method.declaringClass.java.`package`)
                return@filter false
            if(declaredMethods.any { overrides(it, method) })
                return@filter false
            if(method.isAbstract || method.isDefault) {
                if(supertypeMethods.any { overrides(it, method) })
                    return@filter false
            }
            true
        }

        return@lazy MethodListImpl(this, "inherited", abstractInherited)
    }

    override val publicMethods: MethodList by lazy {
        MethodListImpl(this, "public", (declaredMethods + inheritedMethods).filter { it.access == Modifier.Access.PUBLIC })
    }

    override val visibleMethods: MethodList by lazy {
        MethodListImpl(this, "visible", declaredMethods + inheritedMethods)
    }

    override val methods: MethodList by lazy {
        val allMethods = declaredMethods + superclass?.methods.orEmpty() + interfaces.flatMap { it.methods }
        val list = allMethods.filter { s -> allMethods.none { it.doesOverride(s.java) } }.unique()
        return@lazy MethodListImpl(this, "any", list)
    }

    private val declaredMethodsByJava: Map<Method, MethodMirror> by lazy {
        declaredMethods.associateBy { it.java }
    }

    override fun getMethod(other: Method): MethodMirror {
        if(other.declaringClass == this.java) {
            return declaredMethodsByJava[other]
                ?: throw NoSuchMirrorException("Could not find method $other in $this")
        }
        val superclass = findSuperclass(other.declaringClass)
            ?: throw NoSuchMirrorException("Could not find superclass ${other.declaringClass.simpleName} for method " +
                "$other in $this")
        try {
            return superclass.getMethod(other)
        } catch (e: NoSuchMirrorException) {
            throw NoSuchMirrorException("Could not find method $other in $this", e)
        }
    }

    override fun findMethods(name: String): List<MethodMirror> = methods.findAll(name)

    override fun findMethod(name: String, vararg params: TypeMirror): MethodMirror? = methods.find(name, *params)

    override fun findMethodRaw(name: String, vararg params: Class<*>): MethodMirror? = methods.findRaw(name, *params)

    override fun getMethod(name: String, vararg params: TypeMirror): MethodMirror = methods.get(name, *params)

    override fun getMethodRaw(name: String, vararg params: Class<*>): MethodMirror = methods.getRaw(name, *params)
//endregion =====================================================================================================================

//region Fields =================================================================================================================
    override val declaredFields: List<FieldMirror> by lazy {
        MirrorUtils.stableSort(java.declaredFields).map {
            cache.fields.reflect(it).withDeclaringClass(this)
        }.unmodifiableView()
    }
    override val publicFields: List<FieldMirror> by lazy {
        MirrorUtils.stableSort(java.fields).mapNotNull { getField(it) }.unmodifiableView()
    }
    override val fields: List<FieldMirror> by lazy {
        (declaredFields + superclass?.fields.orEmpty()).unmodifiableView()
    }

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

    override fun findField(name: String): FieldMirror? {
        return declaredFields.find { it.name == name }
    }

    override fun getDeclaredField(name: String): FieldMirror {
        return findDeclaredField(name)
            ?: throw NoSuchMirrorException("Could not find field with name $name declared in $this")
    }

    override fun getPublicField(name: String): FieldMirror {
        return findPublicField(name)
            ?: throw NoSuchMirrorException("Could not find public field with name $name in $this")
    }

    override fun getField(name: String): FieldMirror {
        return findField(name)
            ?: throw NoSuchMirrorException("Could not find field with name $name in $this")
    }
//endregion =====================================================================================================================

//region Constructors ===========================================================================================================
    override val declaredConstructors: List<ConstructorMirror> by lazy {
        MirrorUtils.stableSort(java.declaredConstructors).map {
            cache.executables.reflect(it).withDeclaringClass(this) as ConstructorMirror
        }.unmodifiableView()
    }
    override val publicConstructors: List<ConstructorMirror> by lazy {
        MirrorUtils.stableSort(java.constructors).mapNotNull { getConstructor(it) }.unmodifiableView()
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
        MirrorUtils.stableSort(java.declaredClasses).map {
            val mirror = cache.types.reflect(it) as ClassMirror
            if(mirror.isStatic)
                mirror
            else
                mirror.withEnclosingClass(this)
        }.unmodifiableView()
    }
    override val publicMemberClasses: List<ClassMirror> by lazy { java.classes.mapNotNull { getMemberClass(it) }.unmodifiableView() }
    override val memberClasses: List<ClassMirror> by lazy {
        sequenceOf(
            declaredMemberClasses,
            superclass?.memberClasses.orEmpty(),
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

    override fun getDeclaredMemberClass(name: String): ClassMirror {
        return findDeclaredMemberClass(name)
            ?: throw NoSuchMirrorException("Could not find member class with name $name declared in $this")
    }

    override fun getPublicMemberClass(name: String): ClassMirror {
        return findPublicMemberClass(name)
            ?: throw NoSuchMirrorException("Could not find public member class with name $name in $this")
    }

    override fun getMemberClass(name: String): ClassMirror {
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
        if(other is TypeVariableMirror)
            return other.bounds.any { this.isAssignableFrom(it) }
        if(other !is ClassMirror)
            return false

        return isAssignableCache.getOrPut(other) {
            if(other.raw == this.raw) {
                if(this == this.raw)
                    return@getOrPut true // ignore type parameters when raw
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

    @Untested
    override fun toString(): String {
        return toJavaString()
    }

    @Untested
    override fun toDeclarationString(): String {
        return if(isKotlinClass) {
            toKotlinDeclarationString()
        } else {
            toJavaDeclarationString()
        }
    }

    @Untested
    override fun toJavaString(): String {
        var str = ""
        str += typeAnnotations.toJavaString(joiner = " ", trailing = " ")
        str += enclosingClass?.let { enclosing ->
            enclosing.toJavaString() + java.canonicalName.removePrefix(enclosing.java.canonicalName)
        } ?: java.canonicalName
        if(specialization?.arguments != null) {
            str += "<${typeParameters.joinToString(", ") { it.toJavaString() }}>"
        }
        return str
    }

    @Untested
    override fun toKotlinString(): String {
        TODO("Not yet implemented")
    }

    @Untested
    override fun toJavaDeclarationString(): String {
        var str = ""
        str += declaredAnnotations.toJavaString(joiner = "\n", trailing = "\n")
        str += modifiers.joinToString { "$it " }
        str += when {
            isAnnotation -> "@interface "
            isInterface -> "interface "
            else -> "class "
        }
        str += java.simpleName
        if(typeParameters.isNotEmpty()) {
            if(specialization?.annotations != null) {
                str += "<${typeParameters.joinToString(", ") { it.toJavaString() }}>"
            } else {
                str += "<${typeParameters.joinToString(", ") { 
                    (it as? TypeVariableMirror)?.toJavaDeclarationString() ?: "!ERR!"
                }}>"
            }
        }
        superclass?.also {
            if(it.java != Any::class.java) {
                str += " extends ${it.toJavaString()}"
            }
        }
        if(interfaces.isNotEmpty()) {
            str += if(isInterface) " extends " else " implements "
            str += interfaces.joinToString(", ") { it.toJavaString() }
        }
        return str
    }

    @Untested
    override fun toKotlinDeclarationString(): String {
        TODO("Not yet implemented")
    }
}
