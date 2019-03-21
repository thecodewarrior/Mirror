package com.teamwizardry.mirror

import com.teamwizardry.mirror.member.ConstructorMirror
import com.teamwizardry.mirror.member.ExecutableMirror
import com.teamwizardry.mirror.member.FieldMirror
import com.teamwizardry.mirror.member.MethodMirror
import com.teamwizardry.mirror.type.ArrayMirror
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.type.VoidMirror
import io.leangen.geantyref.TypeFactory
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Provides access to the Mirrors for various reflective types.
 */
object Mirror {
    internal var cache = MirrorCache()

    @JvmField
    val void: VoidMirror = reflect(Void.TYPE) as VoidMirror
    @JvmField
    val `object`: ClassMirror = reflectClass<Any>()
    @get:JvmSynthetic
    val any: ClassMirror get() = this.`object`

    /**
     * Create a mirror of the passed type.
     */
    @JvmStatic
    fun reflect(type: Type): TypeMirror {
        return cache.types.reflect(type)
    }

    @JvmStatic
    fun reflect(type: AnnotatedType): TypeMirror {
        return cache.types.reflect(type)
    }

    inline fun <reified T> reflect(): TypeMirror {
        return reflect(typeToken<T>())
    }

    /**
     * Convenience method to reduce unneeded casting when the passed type is known to be a class rather than an array
     * or void.
     *
     * @throws IllegalArgumentException if the input class is an array type or void
     */
    @JvmStatic
    fun reflectClass(clazz: Class<*>): ClassMirror {
        if(clazz.isArray) throw IllegalArgumentException("reflectClass cannot reflect an array type")
        return reflect(clazz) as ClassMirror
    }

    /**
     * Convenience method to reduce unneeded casting when the passed type is known to be a class rather than an array
     * or void.
     *
     * @throws IllegalArgumentException if the input class is an array type or void
     */
    inline fun <reified T> reflectClass(): ClassMirror {
        if(T::class.java.isArray) throw IllegalArgumentException("reflectClass cannot reflect an array type")
        return reflect<T>() as ClassMirror
    }

    /**
     * Convenience method to reduce unneeded casting when the passed type is known to be a class rather than an array
     * or void.
     *
     * @throws IllegalArgumentException if the input class is an array type or void
     */
    inline fun <reified T: Number> reflectPrimitive(): ClassMirror {
        val primitiveClass = T::class.javaPrimitiveType
            ?: throw IllegalArgumentException("reflectPrimitive requires a primitive type")
        return reflect(primitiveClass) as ClassMirror
    }

    @JvmStatic
    fun reflect(field: Field): FieldMirror {
        return cache.fields.reflect(field)
    }

    @JvmStatic
    fun reflect(method: Method): MethodMirror {
        return cache.executables.reflect(method) as MethodMirror
    }

    @JvmStatic
    fun reflect(constructor: Constructor<*>): ConstructorMirror {
        return cache.executables.reflect(constructor) as ConstructorMirror
    }

    @JvmStatic
    fun reflect(executable: Executable): ExecutableMirror {
        return cache.executables.reflect(executable)
    }

    @JvmStatic
    @JvmOverloads
    fun <T: Annotation> newAnnotation(clazz: Class<T>, arguments: Map<String, Any> = emptyMap()): T {
        return TypeFactory.annotation(clazz, arguments)
    }

    inline fun <reified T: Annotation> newAnnotation(arguments: Map<String, Any> = emptyMap()): T {
        return TypeFactory.annotation(T::class.java, arguments)
    }

    /**
     * Create an array of the passed mirror with [depth] dimensions
     */
    @JvmStatic
    @JvmOverloads
    fun createArrayType(type: TypeMirror, depth: Int = 1): ArrayMirror {
        if(depth < 1) throw IllegalArgumentException("Depth must be positive, not $depth")
        val arrayType = (0 until depth).fold(type.java) { t, _ -> TypeFactory.arrayOf(t) }
        return reflect(arrayType) as ArrayMirror
    }


    val Class<*>.arrayMirror: ArrayMirror get() = Mirror.reflect(this) as ArrayMirror
    val Class<*>.mirror: ClassMirror get() = Mirror.reflect(this) as ClassMirror
    val KClass<*>.arrayMirror: ArrayMirror get() = Mirror.reflect(this.java) as ArrayMirror
    val KClass<*>.mirror: ClassMirror get() = Mirror.reflect(this.java) as ClassMirror
}
