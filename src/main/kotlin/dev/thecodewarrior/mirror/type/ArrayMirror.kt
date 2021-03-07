package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.ArrayReflect
import java.lang.reflect.AnnotatedArrayType
import java.lang.reflect.Type

/**
 * The type of mirror used to represent arrays, as opposed to classes, type variables, wildcards, and `void`.
 *
 * @see ClassMirror
 * @see TypeVariableMirror
 * @see VoidMirror
 * @see WildcardMirror
 */
public interface ArrayMirror: ConcreteTypeMirror {
    public override val coreType: Type
    public override val coreAnnotatedType: AnnotatedArrayType
    public override val raw: ArrayMirror

    /**
     * The specialized component type of this mirror
     */
    public val component: TypeMirror

    /**
     * Creates a type mirror with the passed specialized component. The passed component must be assignable to the raw
     * component of this mirror.
     */
    public fun withComponent(component: TypeMirror): ArrayMirror

    public override fun withTypeAnnotations(annotations: List<Annotation>): ArrayMirror

    /**
     * Create a new instance of this array type with the given length. Returns an [Object] because there is no
     * common superclass for arrays. Use [ArrayReflect] to access this array's values or cast if the result type is
     * known. If this mirror represents a non-primitive array, the returned array is filled with null values.
     */
    public fun newInstance(length: Int): Any
}
