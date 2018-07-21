package com.teamwizardry.mirror.reflection

import com.teamwizardry.mirror.reflection.abstractionlayer.field.AbstractField
import com.teamwizardry.mirror.reflection.abstractionlayer.type.AbstractType
import com.teamwizardry.mirror.reflection.member.field.FieldMirror
import com.teamwizardry.mirror.reflection.type.ClassMirror
import com.teamwizardry.mirror.reflection.type.TypeMirror
import java.lang.reflect.Field
import java.lang.reflect.Type

object Mirror {
    internal var cache = MirrorCache()

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