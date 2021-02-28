package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.NoSuchMirrorException
import dev.thecodewarrior.mirror.member.MethodMirror

/**
 * A searchable list of MethodMirrors.
 *
 * **Note: when used as a `List`, this object is immutable**
 */
public interface MethodList: List<MethodMirror> {
    /**
     * Returns the methods in this list that have the specified name.
     *
     * **Note: The returned list is immutable.**
     */
    public fun findAll(name: String): List<MethodMirror>

    /**
     * Finds the method in this list that has the specified signature, or null if no such method exists.
     */
    public fun find(name: String, vararg params: TypeMirror): MethodMirror?

    /**
     * Finds the method in this list that has the specified raw signature, or null if no such method exists.
     */
    public fun findRaw(name: String, vararg params: Class<*>): MethodMirror?

    /**
     * Returns the method in this list that has the specified signature, or throws if no such method exists.
     *
     * @throws NoSuchMirrorException if no method with the specified signature exists
     */
    public fun get(name: String, vararg params: TypeMirror): MethodMirror

    /**
     * Returns the method in this list that has the specified raw signature, or throws if no such method exists.
     *
     * @throws NoSuchMirrorException if no method with the specified signature exists
     */
    public fun getRaw(name: String, vararg params: Class<*>): MethodMirror
}
