package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.type.AbstractType
import java.lang.reflect.Type

/**
 * An abstract representation of a Java type that allows simpler reflective access to it, its members, and the generic
 * type information provided by the JVM.
 *
 * Mirrors can be "specialized", which results in the generic type arguments being substituted all the way down the
 * chain. This substitution means that the mirror of [java.util.HashMap]`<Foo, Bar>` would have a superclass
 * [java.util.AbstractMap]`<Foo, Bar>` and a [java.util.Map.get] method whose return value is `Bar`. This "trickle down"
 * approach makes generic reflection dead easy and is much better than the mind-numbingly complex task of tracing type
 * parameters upward to figure out where they are defined.
 *
 * @see ClassMirror
 * @see ArrayMirror
 * @see VariableMirror
 * @see WildcardMirror
 */
abstract class TypeMirror {
    /**
     * The cache this mirror was created by. Mirrors from other caches will not be considered equal even if they
     * represent the same type. However, no production code should use anything but
     * [com.teamwizardry.mirror.Mirror.reflect], which uses a global cache.
     */
    internal abstract val cache: MirrorCache

    /**
     * The abstract type this mirror represents. Abstract types are used to make explicit the reflection APIs used and
     * sometimes to do some pre-processing of reflection data
     */
    internal abstract val abstractType: AbstractType<*>

    /**
     * The Java Core Reflection type this mirror represents
     */
    abstract val java: Type
}

