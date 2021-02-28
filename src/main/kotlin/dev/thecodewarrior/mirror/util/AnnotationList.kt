package dev.thecodewarrior.mirror.util

import java.lang.reflect.AnnotatedElement

/**
 * A list of annotations and methods to access them. This is used similarly to [AnnotatedElement], but exists to allow
 * multiple semantically distinct sets of annotations on a single object.
 *
 * Unlike [AnnotatedElement], which has methods for both "present" and "declared" annotations, `AnnotationList` models
 * that dichotomy using multiple `AnnotationList` objects. Though the documentation in this class uses the term
 * "present", it is not referring to the definition of "present" as laid out in `AnnotatedElement`, it is simply used
 * for brevity.
 *
 * This is an abstract class instead of an interface so it can have Kotlin inline methods that use reified types.
 *
 * **Note: when used as a `List`, this object is immutable.**
 *
 * @see AnnotatedElement
 */
public abstract class AnnotationList : List<Annotation> {

    /**
     * Returns true if an annotation for the specified type is present on this element, else false. This method is
     * designed primarily for convenient access to marker annotations.
     */
    public abstract fun isPresent(annotationClass: Class<out Annotation>): Boolean

    /**
     * Returns this element's annotation for the specified type if such an annotation is present, else null.
     */
    public abstract fun <T: Annotation> get(annotationClass: Class<T>): T?

    /**
     * Returns annotations that are *associated* with this element.
     *
     * If there are no annotations *associated* with this element, the return value is an array of length 0.
     *
     * The difference between this method and [get] is that this method detects if its argument is a *repeatable
     * annotation type* (JLS 9.6), and if so, attempts to find one or more annotations of that type by "looking
     * through" a container annotation.
     *
     * The caller of this method is free to modify the returned array; it will have no effect on the arrays returned to
     * other callers.
     */
    public abstract fun <T: Annotation> getAllByType(annotationClass: Class<T>): Array<T>

    // Kotlin inline methods:

    /**
     * Returns true if an annotation for the specified type is present on this element, else false. This method is
     * designed primarily for convenient access to marker annotations.
     */
    @JvmSynthetic
    @JvmName("isPresentInline")
    public inline fun <reified T: Annotation> isPresent(): Boolean = this.isPresent(T::class.java)

    /**
     * Returns this element's annotation for the specified type if such an annotation is present, else null.
     */
    @JvmSynthetic
    @JvmName("getInline")
    public inline fun <reified T: Annotation> get(): T? = this.get(T::class.java)

    /**
     * Returns annotations that are *associated* with this element.
     *
     * If there are no annotations *associated* with this element, the return value is an array of length 0.
     *
     * The difference between this method and [get] is that this method detects if its argument is a *repeatable
     * annotation type* (JLS 9.6), and if so, attempts to find one or more annotations of that type by "looking
     * through" a container annotation.
     *
     * The caller of this method is free to modify the returned array; it will have no effect on the arrays returned to
     * other callers.
     */
    @JvmSynthetic
    @JvmName("getAllByTypeInline")
    public inline fun <reified T: Annotation> getAllByType(): Array<T> = this.getAllByType(T::class.java)
}
