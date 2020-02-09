package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMapping
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.unmodifiableView
import java.lang.reflect.Executable

abstract class ExecutableMirror internal constructor(
    cache: MirrorCache,
    internal val specialization: ExecutableSpecialization?
): MemberMirror(cache, specialization?.enclosing) {
    abstract override val java: Executable

    //todo: Also provide reference to what this overrides, if anything
    abstract val raw: ExecutableMirror

    /**
     * The method name. `<init>` for constructors
     */
    abstract val name: String

    val returnType: TypeMirror by lazy {
        java.annotatedReturnType.let {
            genericMapping[cache.types.reflect(it)]
        }
    }

    // * **Note: this value is immutable**
    val parameters: List<ParameterMirror> by lazy {
        java.parameters.map {
            cache.parameters.reflect(it).withDeclaringExecutable(this)
        }.unmodifiableView()
    }

    // * **Note: this value is immutable**
    val parameterTypes: List<TypeMirror> by lazy {
        parameters.map { it.type }.unmodifiableView()
    }

    // * **Note: this value is immutable**
    val exceptionTypes: List<TypeMirror> by lazy {
        java.annotatedExceptionTypes.map {
             genericMapping[cache.types.reflect(it)]
        }.unmodifiableView()
    }

    // * **Note: this value is immutable**
    val typeParameters: List<TypeMirror> by lazy {
        specialization?.arguments ?: java.typeParameters.map {
            cache.types.reflect(it)
        }.unmodifiableView()
    }

    //todo: Should this be a public API? It is in ClassMirror, should it be private there too?
    @Untested
    val genericMapping: TypeMapping by lazy {
        TypeMapping(this.raw.typeParameters.zip(typeParameters).associate { it }) + specialization?.enclosing?.genericMapping
    }

    /**
     * Returns annotations that are present on the executable this mirror represents.
     *
     * **Note: this value is immutable**
     *
     * @see Executable.getAnnotations
     */
    val annotations: List<Annotation> by lazy {
        java.annotations.toList().unmodifiableView()
    }

    open fun withTypeParameters(vararg parameters: TypeMirror): ExecutableMirror {
        if(parameters.isNotEmpty() && parameters.size != typeParameters.size)
            throw InvalidSpecializationException("Passed parameter count ${parameters.size} is different from actual " +
                "parameter count ${typeParameters.size}")
//        if(parameters.toList() == raw.typeParameters) return raw
        val newSpecialization = specialization?.copy(arguments = parameters.toList())
            ?: ExecutableSpecialization(null, parameters.toList())
        return cache.executables.specialize(raw, newSpecialization)
    }

    override fun withDeclaringClass(type: ClassMirror?): ExecutableMirror {
        if(type != null && type.java != java.declaringClass)
            throw InvalidSpecializationException("Invalid declaring class $type. " +
                "$this is declared in ${java.declaringClass}")
//        if(type == raw.declaringClass) return raw
        val newSpecialization = this.specialization?.copy(enclosing = type) ?: ExecutableSpecialization(type, null)
        return cache.executables.specialize(raw, newSpecialization)
    }

    /**
     * This method's [descriptor](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.3.3). The
     * descriptor uses the _erasures_ of the types, and is not affected by specialization. i.e. it always uses the types
     * from the _raw_ method.
     */
    @Untested
    val descriptor: Descriptor by lazy {
        if(raw == this)
            Descriptor(parameterTypes.map { it.erasure }, returnType.erasure)
        else
            raw.descriptor
    }

    class Descriptor(val parameterTypes: List<Class<*>>, val returnType: Class<*>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Descriptor) return false

            return returnType == other.returnType &&
                parameterTypes == other.parameterTypes
        }

        override fun hashCode(): Int {
            var result = returnType.hashCode()
            result = 31 * result + parameterTypes.hashCode()
            return result
        }
    }

    /**
     * This method's [signature](https://docs.oracle.com/javase/tutorial/java/javaOO/methods.html). Unlike the
     * [descriptor], the signature uses the specialized types, includes the method name, and does not include the
     * return type. One method will override another if its raw mirror has the same signature.
     */
    @Untested
    val signature: Signature by lazy { Signature(name, parameterTypes) }

    class Signature(val name: String, val parameterTypes: List<TypeMirror>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Signature) return false

            return name == other.name &&
                parameterTypes == other.parameterTypes
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + parameterTypes.hashCode()
            return result
        }
    }
}