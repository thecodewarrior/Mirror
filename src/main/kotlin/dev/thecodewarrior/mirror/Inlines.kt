/**
 * Inline extensions for the base interfaces, since Kotlin doesn't allow inline methods in interfaces.
 */
package dev.thecodewarrior.mirror

import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import java.lang.reflect.AnnotatedElement

//region AnnotatedElement
/**
 * Returns true if the specified annotation is present on this class.
 *
 * @see AnnotatedElement.isAnnotationPresent
 */
public inline fun <reified T: Annotation> AnnotatedElement.isAnnotationPresent(): Boolean = this.isAnnotationPresent(T::class.java)

/**
 * Returns the annotation of the specified type, or null if no such annotation is present.
 *
 * @see AnnotatedElement.getAnnotation
 */
public inline fun <reified T: Annotation> AnnotatedElement.getAnnotation(): T? = this.getAnnotation(T::class.java)

/**
 * Returns the annotation of the specified type, or null if no such annotation is _directly_ present on this class.
 *
 * @see AnnotatedElement.getDeclaredAnnotation
 */
public inline fun <reified T: Annotation> AnnotatedElement.getDeclaredAnnotation(): T? = this.getDeclaredAnnotation(T::class.java)
//endregion

//region TypeMirror
/**
 * Returns true if the specified [type annotation][java.lang.annotation.ElementType.TYPE_USE] is present on this
 * type.
 */
public inline fun <reified T: Annotation> TypeMirror.isTypeAnnotationPresent(): Boolean = this.isTypeAnnotationPresent(T::class.java)

/**
 * Returns the [type annotation][java.lang.annotation.ElementType.TYPE_USE] of the specified type, or null if no
 * such annotation is present.
 */
public inline fun <reified T: Annotation> TypeMirror.getTypeAnnotation(): T? = this.getTypeAnnotation(T::class.java)
//endregion

//region ClassMirror
/**
 * Recursively searches through this class's supertypes to find the [most specific][specificity] superclass
 * with the specified type. If this class is the specified type this method returns this class. This method returns
 * null if no such superclass was found. Use [getSuperclass] if you want an exception thrown on failure instead.
 *
 * @return The specialized superclass with the passed type, or null if none were found.
 */
@JvmSynthetic
@JvmName("findSuperclassInline")
public inline fun <reified T> ClassMirror.findSuperclass(): ClassMirror? {
    return findSuperclass(T::class.java)
}

/**
 * Recursively searches through this class's supertypes to find the [most specific][specificity] superclass
 * with the specified type. If this class is the specified type this method returns this class. This method throws
 * an exception if no such superclass was found. Use [findSuperclass] if you want a null on failure instead.
 *
 * @return The specialized superclass with the passed type
 * @throws NoSuchMirrorException if this class has no superclass of the passed type
 */
@JvmSynthetic
@JvmName("getSuperclassInline")
public inline fun <reified T> ClassMirror.getSuperclass(): ClassMirror {
    return getSuperclass(T::class.java)
}
//endregion
