/**
 * Inline extensions for the base interfaces, since Kotlin doesn't allow inline methods in interfaces.
 */
package dev.thecodewarrior.mirror

import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.type.TypeSpecificityComparator
import java.lang.reflect.AnnotatedElement

//region ClassMirror
/**
 * Recursively searches through this class's supertypes to find the [most specific][TypeSpecificityComparator]
 * superclass with the specified type. If this class is the specified type this method returns this class. This method
 * returns null if no such superclass was found. Use [getSuperclass] if you want an exception thrown on failure instead.
 *
 * @return The specialized superclass with the passed type, or null if none were found.
 */
public inline fun <reified T> ClassMirror.findSuperclass(): ClassMirror? {
    return findSuperclass(T::class.java)
}

/**
 * Recursively searches through this class's supertypes to find the [most specific][TypeSpecificityComparator]
 * superclass with the specified type. If this class is the specified type this method returns this class. This method
 * throws an exception if no such superclass was found. Use [findSuperclass] if you want a null on failure instead.
 *
 * @return The specialized superclass with the passed type
 * @throws NoSuchMirrorException if this class has no superclass of the passed type
 */
public inline fun <reified T> ClassMirror.getSuperclass(): ClassMirror {
    return getSuperclass(T::class.java)
}
//endregion
