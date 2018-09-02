package com.teamwizardry.mirror.type

/**
 * A type mirror that represents a concrete type, AKA one representable by a [java.lang.Class].
 * The raw type conversion may be lossy, for instance a generic array type returns [Object]`[]` and a
 * specialized class returns its raw class counterpart.
 *
 * @see ClassMirror
 * @see ArrayMirror
 */
abstract class ConcreteTypeMirror: TypeMirror() {
    abstract override val java: Class<*>
}

