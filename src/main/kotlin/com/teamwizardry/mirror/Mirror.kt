package com.teamwizardry.mirror

import com.teamwizardry.mirror.member.ConstructorMirror
import com.teamwizardry.mirror.member.ExecutableMirror
import com.teamwizardry.mirror.member.FieldMirror
import com.teamwizardry.mirror.member.MethodMirror
import com.teamwizardry.mirror.type.ArrayMirror
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.type.VoidMirror
import io.leangen.geantyref.AnnotationFormatException
import io.leangen.geantyref.TypeFactory
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * Provides access to the Mirrors for various reflective types.
 */
object Mirror {
    internal var cache = MirrorCache()

    /**
     * Gets the type mirror representing the passed type
     */
    @JvmStatic
    fun reflect(type: Type): TypeMirror {
        return cache.types.reflect(type)
    }

    /**
     * Gets the type mirror representing the passed annotated type
     */
    @JvmStatic
    fun reflect(type: AnnotatedType): TypeMirror {
        return cache.types.reflect(type)
    }

    /**
     * Gets the type mirror representing the specified type
     */
    inline fun <reified T> reflect(): TypeMirror {
        return reflect(typeToken<T>())
    }

    /**
     * Gets the [ClassMirror] representing the passed annotated type. This is a convenience method for when the type
     * is known to be a class, rather than an array, void, variable, or wildcard.
     *
     * @throws IllegalArgumentException if the input type is an array, void, variable, or wildcard
     */
    @JvmStatic
    fun reflectClass(token: TypeToken<*>): ClassMirror {
        return reflectClass(token.getAnnotated())
    }

    /**
     * Gets the [ClassMirror] representing the passed annotated type. This is a convenience method for when the type
     * is known to be a class, rather than an array, void, variable, or wildcard.
     *
     * @throws IllegalArgumentException if the input type is an array, void, variable, or wildcard
     */
    @JvmStatic
    fun reflectClass(type: AnnotatedType): ClassMirror {
        val reflected = reflect(type)
        if(reflected is ClassMirror) {
            return reflected
        } else {
            throw IllegalArgumentException("Passed type $reflected is not a class")
        }
    }

    /**
     * Gets the [ClassMirror] representing the passed type. This is a convenience method for when the type is known
     * to be a class, rather than an array, void, variable, or wildcard.
     *
     * @throws IllegalArgumentException if the input type is an array, void, variable, or wildcard
     */
    @JvmStatic
    fun reflectClass(type: Type): ClassMirror {
        val reflected = reflect(type)
        if(reflected is ClassMirror) {
            return reflected
        } else {
            throw IllegalArgumentException("Passed type $reflected is not a class")
        }
    }

    /**
     * Gets the [ClassMirror] representing the specified type. This is a convenience method for when the type is known
     * to be a class, rather than an array or void.
     *
     * @throws IllegalArgumentException if the input type is an array or void
     */
    inline fun <reified T> reflectClass(): ClassMirror {
        if(T::class.java.isArray) throw IllegalArgumentException("reflectClass cannot reflect an array type")
        if(T::class.java == Void.TYPE) throw IllegalArgumentException("reflectClass cannot reflect the void type")
        return reflect<T>() as ClassMirror
    }

    /**
     * Gets the field mirror representing the passed field
     */
    @JvmStatic
    fun reflect(field: Field): FieldMirror {
        return cache.fields.reflect(field)
    }

    /**
     * Gets the method mirror representing the passed method
     */
    @JvmStatic
    fun reflect(method: Method): MethodMirror {
        return cache.executables.reflect(method) as MethodMirror
    }

    /**
     * Gets the constructor mirror representing the passed constructor
     */
    @JvmStatic
    fun reflect(constructor: Constructor<*>): ConstructorMirror {
        return cache.executables.reflect(constructor) as ConstructorMirror
    }

    /**
     * Gets the method or constructor mirror representing the passed method or constructor
     */
    @JvmStatic
    fun reflect(executable: Executable): ExecutableMirror {
        return cache.executables.reflect(executable)
    }

    @JvmStatic
    @JvmOverloads
    @Throws(AnnotationFormatException::class)
    fun <T: Annotation> newAnnotation(clazz: Class<T>, arguments: Map<String, Any> = emptyMap()): T {
        return TypeFactory.annotation(clazz, arguments)
    }

    @JvmStatic
    @Throws(AnnotationFormatException::class)
    fun <T: Annotation> newAnnotation(clazz: Class<T>, vararg arguments: Pair<String, Any>): T {
        return newAnnotation(clazz, mapOf(*arguments))
    }

    inline fun <reified T: Annotation> newAnnotation(arguments: Map<String, Any> = emptyMap()): T {
        return TypeFactory.annotation(T::class.java, arguments)
    }

    inline fun <reified T: Annotation> newAnnotation(vararg arguments: Pair<String, Any>): T {
        return TypeFactory.annotation(T::class.java, mapOf(*arguments))
    }

    /**
     * Create an array whose component type is the passed mirror
     */
    @JvmStatic
    fun createArrayType(type: TypeMirror): ArrayMirror {
        return reflect(TypeFactory.arrayOf(type.coreAnnotatedType, emptyArray())) as ArrayMirror
    }

    /**
     * Easy access to core Java types (void + primitives + Object)
     */
    object Types {
        /** The type mirror representing the `void` type */
        val void: VoidMirror get() = reflect(Void.TYPE) as VoidMirror

        /** The type mirror representing the primitive `boolean` type */
        val boolean: ClassMirror get() = reflectClass(Boolean::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `byte` type */
        val byte: ClassMirror get() = reflectClass(Byte::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `char` type */
        val char: ClassMirror get() = reflectClass(Char::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `short` type */
        val short: ClassMirror get() = reflectClass(Short::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `int` type */
        val int: ClassMirror get() = reflectClass(Int::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `long` type */
        val long: ClassMirror get() = reflectClass(Long::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `float` type */
        val float: ClassMirror get() = reflectClass(Float::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `double` type */
        val double: ClassMirror get() = reflectClass(Double::class.javaPrimitiveType!!)

        /** The type mirror representing the `Object` type */
        val `object`: ClassMirror get() = reflectClass<Any>()
        /** The type mirror representing the `Any` type (synonymous with [object]) */
        @get:JvmSynthetic
        val any: ClassMirror get() = reflectClass<Any>()
    }
}
