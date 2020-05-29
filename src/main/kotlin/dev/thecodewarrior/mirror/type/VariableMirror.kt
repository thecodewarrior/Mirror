package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.utils.Untested
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
// TODO: rename to TypeVariableMirror
class VariableMirror internal constructor(
    override val cache: MirrorCache,
    override val coreType: TypeVariable<*>,
    raw: VariableMirror?,
    override val specialization: TypeSpecialization.Common?
): TypeMirror(), AnnotatedElement by coreType {

    override val coreAnnotatedType: AnnotatedTypeVariable
        = CoreTypeUtils.annotate(coreType, typeAnnotations.toTypedArray()) as AnnotatedTypeVariable

    override val raw: VariableMirror = raw ?: this

    /**
     * The bounds of this variable. Types specializing this variable must extend all of these.
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
            VariableMirror(cache, coreType, raw, it)
        }
    }

    override fun isAssignableFrom(other: TypeMirror): Boolean {
        if(other == this) return true
        if(other is VariableMirror)
            return this.bounds.zip(other.bounds).all { (ours, theirs) -> ours.isAssignableFrom(theirs) }
        return bounds.all {
            it.isAssignableFrom(other)
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

    @Untested
    override fun toString(): String {
        var str = ""
        str += typeAnnotationString()
        str += coreType.name
        return str
    }

    @Untested
    fun toDeclarationString(): String {
        var str = ""
        str += coreType.name
        if(bounds.isNotEmpty() && !(bounds.size == 1 && bounds[0] == cache.types.reflect(Any::class.java))) {
            str += " extends ${bounds.joinToString(" & ")}"
        }
        return str
    }
}
