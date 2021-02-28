package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import java.lang.reflect.Constructor

/**
 * A mirror representing a constructor.
 */
public interface ConstructorMirror : ExecutableMirror {
    public override val java: Constructor<*>
    public override val raw: ConstructorMirror

    /**
     * Create a new instance using this constructor. After the one-time cost of creating the
     * [MethodHandle][java.lang.invoke.MethodHandle], the access should be near-native speed.
     */
    public fun <T : Any?> call(vararg args: Any?): T

    override fun withTypeParameters(vararg parameters: TypeMirror): ConstructorMirror

    override fun withDeclaringClass(enclosing: ClassMirror?): ConstructorMirror
}