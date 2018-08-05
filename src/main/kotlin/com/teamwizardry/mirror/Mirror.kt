package com.teamwizardry.mirror

import com.teamwizardry.mirror.abstractionlayer.field.AbstractField
import com.teamwizardry.mirror.abstractionlayer.type.AbstractType
import com.teamwizardry.mirror.member.FieldMirror
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import java.lang.reflect.Field
import java.lang.reflect.Type

/*
temp holding place
terminology:
    specialized mirror - a mirror that has had the type parameters as provided by Java replaced.
    specialization - the process of creating a specialized mirror, or a specialized version of the mentioned mirror
    specialize - the act of creating a specialized version of amirror
*/


object Mirror {
    internal var cache = MirrorCache()

    /**
     * Create a mirror of the passed type.
     */
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

    fun reflect(field: Field): FieldMirror {
        val abstract = AbstractField(field)
        return cache.fields.reflect(abstract)
    }
}