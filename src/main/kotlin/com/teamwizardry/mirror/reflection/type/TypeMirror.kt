package com.teamwizardry.mirror.reflection.type

import com.teamwizardry.mirror.reflection.MirrorCache
import com.teamwizardry.mirror.reflection.abstractionlayer.type.AbstractType
import java.lang.reflect.*

/**
 * A reflective representation of a class that is decoupled from the Java and Kotlin reflection mechanisms.
 *
 * This decoupling means much more analysis and abstraction can be done, such as specializing a generic class and having
 * the types of all the property and method mirrors of the new mirror replaced with the correct types.
 */
abstract class TypeMirror {
    /**
     * The cache this reflect was created by. Mirrors from other caches will not be considered equal even if they
     * represent the same type. However, no production code should use anything but [TypeMirror.reflect]
     */
    internal abstract val cache: MirrorCache
    /**
     * The type this mirror represents
     */
    internal abstract val abstractType: AbstractType<*>

    /**
     * The raw type this mirror represents. Specializing parameterized types will not preserve the
     * parameterized type as the raw type of the returned mirror.
     */
    abstract val rawType: Type

    companion object {
        @JvmStatic
        fun reflect(type: Type): TypeMirror {
            return MirrorCache.DEFAULT.reflect(AbstractType.create(type))
        }
    }
}

