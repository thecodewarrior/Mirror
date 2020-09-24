package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.annotationString
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.AnnotatedTypeVariable
import java.lang.reflect.TypeVariable

/**
 * The type of mirror used to represent [type variables](https://docs.oracle.com/javase/tutorial/java/generics/types.html).
 *
 * **Note:** Type variables' bounds will never be specialized, as doing so would require a significant increase in
 * complexity in order to avoid infinite recursion and/or deadlocks. However, should a pressing enough need come up in
 * the future to outweigh this increase in complexity, it is likely possible it could be added.
 *
 * @see ArrayMirror
 * @see ClassMirror
 * @see VoidMirror
 * @see WildcardMirror
 */
class TypeVariableMirror internal constructor(
    override val cache: MirrorCache,
    override val coreType: TypeVariable<*>,
    raw: TypeVariableMirror?,
    override val specialization: TypeSpecialization.Common?
): TypeMirror(), AnnotatedElement by coreType {

    override val coreAnnotatedType: AnnotatedTypeVariable
        = CoreTypeUtils.annotate(coreType, typeAnnotations.toTypedArray()) as AnnotatedTypeVariable

    override val raw: TypeVariableMirror = raw ?: this

    /**
     * The bounds of this type variable. Types specializing this type variable must extend all of these.
     *
     * By default it contains the [Object] mirror.
     */
    val bounds: List<TypeMirror> by lazy {
        coreType.annotatedBounds.map { cache.types.reflect(it) }
    }

    override fun defaultSpecialization() = TypeSpecialization.Common.DEFAULT

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Common>(
            specialization,
            { true }
        ) {
            TypeVariableMirror(cache, coreType, raw, it)
        }
    }

    override fun isAssignableFrom(other: TypeMirror): Boolean {
        return when(other) {
            this -> true
            is TypeVariableMirror -> this in other.bounds
            is WildcardMirror -> this in other.upperBounds
            else -> false
        }
    }

    /**
     * Returns true if the specified annotation is present on this type variable.
     *
     * @see AnnotatedElement.isAnnotationPresent
     */
    inline fun <reified T: Annotation> isAnnotationPresent(): Boolean = this.isAnnotationPresent(T::class.java)

    /**
     * Returns the annotation of the specified type, or null if no such annotation is present.
     *
     * @see AnnotatedElement.getAnnotation
     */
    inline fun <reified T: Annotation> getAnnotation(): T? = this.getAnnotation(T::class.java)

    /**
     * Returns the annotation of the specified type, or null if no such annotation is _directly_ present on this type
     * variable.
     *
     * @see AnnotatedElement.getDeclaredAnnotation
     */
    inline fun <reified T: Annotation> getDeclaredAnnotation(): T? = this.getDeclaredAnnotation(T::class.java)

    override fun toString(): String {
        var str = ""
        str += typeAnnotations.annotationString()
        str += coreType.name
        return str
    }

    /**
     * The declaration string for this type. e.g. `T extends X`
     */
    fun toDeclarationString(): String {
        var str = ""
        str += coreType.annotations.annotationString()
        str += coreType.name
        if(bounds.isNotEmpty() && !(bounds.size == 1 && bounds[0] == cache.types.reflect(Any::class.java))) {
            str += " extends ${bounds.joinToString(" & ")}"
        }
        return str
    }
}
