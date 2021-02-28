package dev.thecodewarrior.mirror.type

import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type

/**
 * A type mirror that represents a concrete type, i.e. a type representable by a [java.lang.Class].
 * The raw type conversion may be lossy, for instance a generic array type returns [Object]`[]` and a
 * specialized class returns its raw class counterpart.
 *
 * @see ClassMirror
 * @see ArrayMirror
 */
public interface ConcreteTypeMirror: TypeMirror {
    public override val coreType: Type
    public override val coreAnnotatedType: AnnotatedType
    public override val raw: ConcreteTypeMirror

    /**
     * The raw Core Reflection class this mirror represents
     */
    public val java: Class<*>
}

