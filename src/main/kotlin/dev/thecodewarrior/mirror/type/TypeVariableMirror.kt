package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.util.DeclarationMirror
import java.lang.reflect.AnnotatedTypeVariable
import java.lang.reflect.TypeVariable

/**
 * The type of mirror used to represent [type variables](https://docs.oracle.com/javase/tutorial/java/generics/types.html).
 *
 * **Note:** Type variables' bounds will never be specialized, as doing so would require a significant increase in
 * complexity in order to avoid infinite recursion and/or deadlocks. However, should a pressing enough need come up in
 * the future to outweigh this increase in complexity, it is likely possible it could be added.
 *
 * @see ArrayMirror
 * @see ClassMirror
 * @see VoidMirror
 * @see WildcardMirror
 */
public interface TypeVariableMirror : TypeMirror, DeclarationMirror {
    public override val coreType: TypeVariable<*>
    public override val coreAnnotatedType: AnnotatedTypeVariable
    public override val raw: TypeVariableMirror

    /**
     * The bounds of this type variable. Types specializing this type variable must extend all of these.
     *
     * By default it contains the [Object] mirror.
     */
    public val bounds: List<TypeMirror>

    public override fun withTypeAnnotations(annotations: List<Annotation>): TypeVariableMirror
}

