package dev.thecodewarrior.mirror

import dev.thecodewarrior.mirror.coretypes.AnnotationFormatException
import dev.thecodewarrior.mirror.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.coretypes.TypeImplAccess
import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.member.ExecutableMirror
import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.type.VoidMirror
import dev.thecodewarrior.mirror.utils.IndirectTests
import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.UntestedNegative
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * The central class used to retrieve mirrors of Core Reflection objects
 */
@Untested("there should be tests to ensure all the different `reflect` methods agree")
object Mirror {
    // these instances are replaced by the unit tests using reflection. `types` isn't a `var` with a private setter
    // because if it was IDEA would mark it as mutable with an underline, which is incorrect and gets irritating
    private var cache = MirrorCache()
    private var _types = createTypes()
    private fun createTypes(): Types {
        val c = Types::class.java.getDeclaredConstructor()
        c.isAccessible = true
        return c.newInstance()
    }

    /**
     * Easy access to core Java types (void + primitives + Object)
     */
    @JvmStatic
    val types: Types get() = _types

    /**
     * Gets the type mirror representing the passed type
     */
    @JvmStatic
    fun reflect(type: Type): TypeMirror {
        return cache.types.reflect(type)
    }

    /**
     * Gets the type mirror representing the passed type
     */
    @JvmStatic
    @IndirectTests
    fun reflect(type: TypeToken<*>): TypeMirror {
        return cache.types.reflect(type.getAnnotated())
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
    @Untested
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
    @Untested
    fun reflect(executable: Executable): ExecutableMirror {
        return cache.executables.reflect(executable)
    }

    /**
     * Dynamically creates a new annotation instance.
     *
     * @throws AnnotationFormatException if the [clazz] isn't an annotation class
     * @throws AnnotationFormatException if any required annotation values are missing from the passed map
     * @throws AnnotationFormatException if any values in the map have incompatible types with the attributes of the annotation
     */
    @JvmStatic
    @JvmOverloads
    @IndirectTests("via the <reified T> newAnnotation(pairs)")
    fun <T: Annotation> newAnnotation(clazz: Class<T>, arguments: Map<String, Any> = emptyMap()): T {
        return CoreTypeUtils.createAnnotation(clazz, arguments)
    }

    /**
     * Dynamically creates a new annotation instance.
     *
     * @throws AnnotationFormatException if the [clazz] isn't an annotation class
     * @throws AnnotationFormatException if any required annotation values are missing from the passed name-value pair set
     * @throws AnnotationFormatException if any values in the set have incompatible types with the attributes of the annotation
     */
    @JvmStatic
    @IndirectTests
    fun <T: Annotation> newAnnotation(clazz: Class<T>, vararg arguments: Pair<String, Any>): T {
        return newAnnotation(clazz, mapOf(*arguments))
    }

    /**
     * Dynamically creates a new annotation instance.
     *
     * @throws AnnotationFormatException if [T] isn't an annotation class
     * @throws AnnotationFormatException if any required annotation values are missing from the passed map
     * @throws AnnotationFormatException if any values in the map have incompatible types with the attributes of the annotation
     */
    @IndirectTests
    inline fun <reified T: Annotation> newAnnotation(arguments: Map<String, Any> = emptyMap()): T {
        return newAnnotation(T::class.java, arguments)
    }

    /**
     * Dynamically creates a new annotation instance.
     *
     * @throws AnnotationFormatException if [T] isn't an annotation class
     * @throws AnnotationFormatException if any required annotation values are missing from the passed name-value pair set
     * @throws AnnotationFormatException if any values in the set have incompatible types with the attributes of the annotation
     */
    inline fun <reified T: Annotation> newAnnotation(vararg arguments: Pair<String, Any>): T {
        return newAnnotation(T::class.java, mapOf(*arguments))
    }

    /**
     * Create an array whose component type is the passed mirror
     */
    @JvmStatic
    fun createArrayType(type: TypeMirror): ArrayMirror {
        return reflect(TypeImplAccess.createArrayType(type.coreAnnotatedType, emptyArray())) as ArrayMirror
    }

    /**
     * Transforms the passed type to an equivalent one that implements the [equals] and [hashCode] methods.
     */
    @JvmStatic
    @IndirectTests
    fun <T: AnnotatedType> toCanonical(type: T): T = CoreTypeUtils.toCanonical(type)

    /**
     * Easy access to core Java types (void + primitives + Object)
     */
    class Types private constructor() {
        /** The type mirror representing the `void` type */
        val void: VoidMirror = reflect(Void.TYPE) as VoidMirror

        /** The type mirror representing the primitive `boolean` type */
        val boolean: ClassMirror = reflectClass(Boolean::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `byte` type */
        val byte: ClassMirror = reflectClass(Byte::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `char` type */
        val char: ClassMirror = reflectClass(Char::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `short` type */
        val short: ClassMirror = reflectClass(Short::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `int` type */
        val int: ClassMirror = reflectClass(Int::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `long` type */
        val long: ClassMirror = reflectClass(Long::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `float` type */
        val float: ClassMirror = reflectClass(Float::class.javaPrimitiveType!!)
        /** The type mirror representing the primitive `double` type */
        val double: ClassMirror = reflectClass(Double::class.javaPrimitiveType!!)

        /** The type mirror representing the `Object` type (`Any` in Kotlin) */
        @get:JvmName("getObject")
        val any: ClassMirror = reflectClass<Any>()
    }
}
