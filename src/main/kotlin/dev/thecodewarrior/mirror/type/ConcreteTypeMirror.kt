package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.utils.Untested

/**
 * A type mirror that represents a concrete type, i.e. a type representable by a [java.lang.Class].
 * The raw type conversion may be lossy, for instance a generic array type returns [Object]`[]` and a
 * specialized class returns its raw class counterpart.
 *
 * @see ClassMirror
 * @see ArrayMirror
 */
abstract class ConcreteTypeMirror: TypeMirror() {
    /**
     * The raw Core Reflection class this mirror represents
     */
    @Untested
    abstract val java: Class<*>
}

