package com.teamwizardry.mirror.reflection

import com.teamwizardry.mirror.reflection.abstractionlayer.type.AbstractType
import com.teamwizardry.mirror.reflection.type.ClassMirror
import com.teamwizardry.mirror.reflection.type.TypeMirror
import java.lang.reflect.Type

object Mirror {
    internal var cache = MirrorCache()

    fun reflect(type: Type): TypeMirror {
        val abstract = AbstractType.create(type)
        return cache.reflect(abstract)
    }

    inline fun <reified T> reflect(): TypeMirror {
        return reflect(T::class.java)
    }

    /**
     * Convenience method to reduce unneeded casting when the passed type is known to be a class rather than an array.
     *
     * @throws IllegalArgumentException if the input class is an array type
     */
    fun reflectClass(clazz: Class<*>): ClassMirror {
        if(clazz.isArray) throw IllegalArgumentException("reflectClass cannot reflect an array type")
        return reflect(clazz) as ClassMirror
    }

    /**
     * Convenience method to reduce unneeded casting when the passed type is known to be a class rather than an array.
     *
     * @throws IllegalArgumentException if the input class is an array type
     */
    inline fun <reified T> reflectClass(): ClassMirror {
        return reflectClass(T::class.java)
    }
}