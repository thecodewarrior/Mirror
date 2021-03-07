package dev.thecodewarrior.mirror.type

import java.lang.reflect.AnnotatedType

/**
 * The type of mirror used to represent the `void` type.
 *
 * @see ArrayMirror
 * @see ClassMirror
 * @see TypeVariableMirror
 * @see WildcardMirror
 */
public interface VoidMirror : TypeMirror {
    public override val coreType: Class<*>
    public override val coreAnnotatedType: AnnotatedType
    public override val raw: VoidMirror

    public override fun withTypeAnnotations(annotations: List<Annotation>): VoidMirror
}