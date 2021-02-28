package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import java.lang.reflect.Executable
import java.lang.reflect.Method
import kotlin.reflect.KCallable

/**
 * A mirror that represents the commonality between methods and constructors.
 *
 * @see ConstructorMirror
 * @see MethodMirror
 */
public interface ExecutableMirror : MemberMirror {
    public override val java: Executable
    public override val raw: ExecutableMirror
    public override val modifiers: Set<Modifier>
    public override val access: Modifier.Access

    /**
     * Returns true if this method takes a variable number of arguments.
     *
     * @see Method.isVarArgs
     */
    public val isVarArgs: Boolean

    /**
     * Returns true if this method/constructor has internal visibility in Kotlin
     */
    public val isInternalAccess: Boolean

    /**
     * Returns the Kotlin [KCallable] instance that represents the same method/constructor as this. This is null if this
     * is [synthetic][isSynthetic] or if this is a [bridge method][MethodMirror.isBridge]. Other cases where no
     * KCallable exists are not yet known, but may exist.
     */
    public val kCallable: KCallable<*>?

    /**
     * The method name. The class's [binary name][Class.getName] for constructors.
     */
    public val name: String

    public val returnType: TypeMirror

    // * **Note: this value is immutable**
    public val parameters: List<ParameterMirror>

    // * **Note: this value is immutable**
    public val parameterTypes: List<TypeMirror>

    /**
     * Used to determine method override relationships
     */
    public val erasedParameterTypes: List<Class<*>>

    // * **Note: this value is immutable**
    public val exceptionTypes: List<TypeMirror>

    // * **Note: this value is immutable**
    public val typeParameters: List<TypeMirror>

    /**
     * Returns a copy of this mirror, replacing its type parameters the given types. Passing zero arguments will return
     * a copy of this mirror with the raw type arguments.
     *
     * **Note: A new mirror is only created if none already exist with the required specialization**
     *
     * @throws InvalidSpecializationException if the passed type list is not the same length as [typeParameters] or zero
     * @return A copy of this mirror with its type parameters replaced
     */
    public fun withTypeParameters(vararg parameters: TypeMirror): ExecutableMirror

    override fun withDeclaringClass(enclosing: ClassMirror?): ExecutableMirror
}