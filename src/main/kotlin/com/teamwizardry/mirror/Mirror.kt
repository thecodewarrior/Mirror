package com.teamwizardry.mirror

import com.teamwizardry.mirror.abstractionlayer.field.AbstractField
import com.teamwizardry.mirror.abstractionlayer.method.AbstractMethod
import com.teamwizardry.mirror.abstractionlayer.type.AbstractType
import com.teamwizardry.mirror.member.FieldMirror
import com.teamwizardry.mirror.member.MethodMirror
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * Provides access to the Mirrors for various reflective types.
 */
object Mirror {
    internal var cache = MirrorCache()

    /**
     * Create a mirror of the passed type.
     */
    @JvmStatic
    fun reflect(type: Type): TypeMirror {
        val abstract = AbstractType.create(type)
        return cache.types.reflect(abstract)
    }

    inline fun <reified T> reflect(): TypeMirror {
        return reflect(typeToken<T>())
    }

    /**
     * Convenience method to reduce unneeded casting when the passed type is known to be a class rather than an array.
     *
     * @throws IllegalArgumentException if the input class is an array type
     */
    @JvmStatic
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
        if(T::class.java.isArray) throw IllegalArgumentException("reflectClass cannot reflect an array type")
        return reflect<T>() as ClassMirror
    }

    @JvmStatic
    fun reflect(field: Field): FieldMirror {
        val abstract = AbstractField(field)
        return cache.fields.reflect(abstract)
    }

    @JvmStatic
    fun reflect(method: Method): MethodMirror {
        val abstract = AbstractMethod(method)
        return cache.methods.reflect(abstract)
    }
}