package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.ArrayReflect
import com.teamwizardry.mirror.MirrorCache
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Type

/**
 * A mirror that represents an array type
 */
class ArrayMirror internal constructor(
    override val cache: MirrorCache,
    private val type: Type,
    raw: ArrayMirror?,
    override val specialization: TypeSpecialization.Array?
): ConcreteTypeMirror() {

    override var java: Class<*> = when(type) {
        is Class<*> -> type
        is GenericArrayType -> Array<Any>::class.java
        else -> throw IllegalArgumentException("The `type` parameter of ArrayMirrors must be either a Class or " +
            "a GenericArrayType. It was a ${type.javaClass}")
    }
    /**
     * The component type of this mirror. `String` in `[String]`, `int` in `[int]`, `T` in `[T]`, etc.
     */
    val component: TypeMirror by lazy {
        specialization?.component
            ?: cache.types.reflect(
                (type as? GenericArrayType)?.genericComponentType ?: java.componentType
            )
    }

    override val raw: ArrayMirror = raw ?: this

    override fun defaultSpecialization() = TypeSpecialization.Array.DEFAULT

    fun specialize(component: TypeMirror): ArrayMirror {
        val newSpecialization = (specialization ?: defaultSpecialization()).copy(component = component)
        return cache.types.specialize(this, newSpecialization) as ArrayMirror
    }

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Array>(
            specialization,
            { it.component == this.component || it.component == null}
        ) {
            ArrayMirror(cache, java, raw, it)
        }
    }

    override fun isAssignableFrom(other: TypeMirror): Boolean {
        if(other == this) return true
        if (other !is ArrayMirror)
            return false
        return component.isAssignableFrom(other.component)
    }

    override fun toString(): String {
        var str = "$component"
        if(specialization?.annotations?.isNotEmpty() == true) {
            str += " " + specialization.annotations.joinToString(" ") + " "
        }
        str += "[]"
        return str
    }

    /**
     * Create a new instance of this array type with the given length. Returns an [Object] because there is no
     * common superclass for primitive arrays. Use [ArrayReflect] to access this array's values generically.
     * If this mirror represents a non-primitive array, the returned array is filled with null values.
     */
    fun newInstance(length: Int): Any {
        val rawComponent = this.component.raw as? ConcreteTypeMirror

        return when {
            this.java == BooleanArray::class.java -> BooleanArray(length)
            this.java == ByteArray::class.java -> ByteArray(length)
            this.java == CharArray::class.java -> CharArray(length)
            this.java == ShortArray::class.java -> ShortArray(length)
            this.java == IntArray::class.java -> IntArray(length)
            this.java == LongArray::class.java -> LongArray(length)
            this.java == FloatArray::class.java -> FloatArray(length)
            this.java == DoubleArray::class.java -> DoubleArray(length)
            rawComponent == null -> Array<Any?>(length) { null }
            else -> ArrayReflect.newInstanceRaw(rawComponent.java, length)
        }
    }
}