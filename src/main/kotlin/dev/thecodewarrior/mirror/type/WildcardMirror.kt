package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.impl.utils.Untested
import java.lang.reflect.AnnotatedWildcardType
import java.lang.reflect.WildcardType

/**
 * The type of mirror used to represent [wildcard types](https://docs.oracle.com/javase/tutorial/java/generics/wildcards.html).
 *
 * @see ArrayMirror
 * @see ClassMirror
 * @see TypeVariableMirror
 * @see VoidMirror
 */
public interface WildcardMirror : TypeMirror {
    public override val coreType: WildcardType
    public override val coreAnnotatedType: AnnotatedWildcardType
    public override val raw: WildcardMirror

    /**
     * `? super T` or `out T`. The lowermost type in the hierarchy that is valid. Any valid type must be a supertype
     * of `T`. The current language spec only allows for one, but the Core Reflection API supports multiple for
     * future-proofing reasons.
     *
     * In plain english, a lower bounded wildcard represents "somewhere I can put a < bound >", as opposed to upper
     * bounded wildcards being "some kind of < bound >".
     *
     * ```
     * For `? super AbstractList`
     * - Object          - Valid   - `Object myVar = myAbstractList;` compiles
     * - List            - Valid   - `List myVar = myAbstractList;` compiles
     * * AbstractList    - Valid   - `AbstractList myVar = myAbstractList;` compiles
     * - ArrayList       - Invalid - `ArrayList myVar = myAbstractList;` does not compile
     * ```
     */
    public val lowerBounds: List<TypeMirror>

    /**
     * `? super T` or `out T`. The lowermost type in the hierarchy that is valid. Any valid type must be a supertype
     * of `T`. This is a shorthand for the first element of [lowerBounds], if it exists, as currently that is the only
     * one supported by the language.
     *
     * In plain english, a lower bounded wildcard represents "somewhere I can put a < bound >", as opposed to upper
     * bounded wildcards being "some kind of < bound >".
     *
     * ```
     * For `? super AbstractList`
     * - Object          - Valid   - `Object myVar = myAbstractList;` compiles
     * - List            - Valid   - `List myVar = myAbstractList;` compiles
     * * AbstractList    - Valid   - `AbstractList myVar = myAbstractList;` compiles
     * - ArrayList       - Invalid - `ArrayList myVar = myAbstractList;` does not compile
     * ```
     */
    @Untested
    public val lowerBound: TypeMirror?

    /**
     * `? extends T` or `in T`. The uppermost type in the hierarchy that is valid. Any valid type must be a subclass
     * of and implement the interfaces in [upperBounds]. The current language spec only allows for one, but the Core
     * Reflection API supports multiple for future-proofing reasons.
     *
     * In plain english, an upper bounded wildcard represents "some kind of < bound >", as opposed to lower bounded
     * wildcards being "somewhere I can put a < bound >".
     *
     * ```
     * For `? extends List`
     * - Object          - Invalid - `public List foo() { return myObject; }` does not compile
     * * List            - Valid   - `public List foo() { return myList; }` compiles
     * - AbstractList    - Valid   - `public List foo() { return myAbstractList; }` compiles
     * - ArrayList       - Valid   - `public List foo() { return myArrayList; }` compiles
     * ```
     */
    public val upperBounds: List<TypeMirror>

    /**
     * `? extends T` or `in T`. The uppermost type in the hierarchy that is valid. Any valid type must be a subclass
     * of or implement the classes in [upperBounds]. This is a shorthand for the first element of [upperBounds], if it
     * exists, as currently that is the only one supported by the language.
     *
     * In plain english, an upper bounded wildcard represents "some kind of < bound >", as opposed to lower bounded
     * wildcards being "somewhere I can put a < bound >".
     *
     * ```
     * For `? extends List`
     * - Object          - Invalid - `public List foo() { return myObject; }` does not compile
     * * List            - Valid   - `public List foo() { return myList; }` compiles
     * - AbstractList    - Valid   - `public List foo() { return myAbstractList; }` compiles
     * - ArrayList       - Valid   - `public List foo() { return myArrayList; }` compiles
     * ```
     */
    @Untested
    public val upperBound: TypeMirror?

    /**
     * Specialize this wildcard with the provided upper and lower bounds. If the upper and lower bounds aren't
     */
    @Untested
    public fun withBounds(upperBounds: List<TypeMirror>?, lowerBounds: List<TypeMirror>?): WildcardMirror
}
