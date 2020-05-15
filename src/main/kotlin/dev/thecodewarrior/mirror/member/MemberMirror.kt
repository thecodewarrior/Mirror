package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.utils.Untested
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Member

/**
 * The abstract superclass representing any Java class member
 */
abstract class MemberMirror internal constructor(
    internal val cache: MirrorCache,
    _enclosing: ClassMirror?
): AnnotatedElement {
    /**
     * The Core Reflection object this mirror represents
     */
    abstract val java: Member

    protected abstract val annotatedElement: AnnotatedElement

    /**
     * The potentially specialized class this member is declared in
     */
    val declaringClass: ClassMirror by lazy {
        _enclosing ?: cache.types.reflect(java.declaringClass) as ClassMirror
    }

    /**
     * Returns a copy of this member with its enclosing class replaced with [enclosing]. Substituting type variables as
     * necessary. If the passed class is null this method removes any enclosing class specialization.
     *
     * **Note: A new mirror is only created if none already exist with the required specialization**
     *
     * @throws InvalidSpecializationException if [enclosing] is not equal to or a specialization of this
     * class's raw enclosing class
     * @return A copy of this member with the passed enclosing class, or with the raw enclosing class if [enclosing]
     * is null
     */
    abstract fun withDeclaringClass(enclosing: ClassMirror?): MemberMirror

    override fun <T: Annotation?> getAnnotation(annotationClass: Class<T>): T {
        return annotatedElement.getAnnotation(annotationClass)
    }

    override fun getAnnotations(): Array<Annotation> {
        return annotatedElement.annotations
    }

    override fun getDeclaredAnnotations(): Array<Annotation> {
        return annotatedElement.declaredAnnotations
    }

    /**
     * Returns true if the specified annotation is present on this member.
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
     * Returns the annotation of the specified type, or null if no such annotation is _directly_ present on this member.
     *
     * @see AnnotatedElement.getDeclaredAnnotation
     */
    inline fun <reified T: Annotation> getDeclaredAnnotation(): T? = this.getDeclaredAnnotation(T::class.java)
}