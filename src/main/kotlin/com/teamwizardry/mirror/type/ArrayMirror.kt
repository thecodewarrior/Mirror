package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.ArrayReflect
import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import io.leangen.geantyref.GenericTypeReflector
import io.leangen.geantyref.TypeFactory
import java.lang.reflect.AnnotatedArrayType
import java.lang.reflect.Type

/**
 * A mirror that represents an array type
 */
class ArrayMirror internal constructor(
    override val cache: MirrorCache,
    override val java: Class<*>,
    raw: ArrayMirror?,
    override val specialization: TypeSpecialization.Array?
): ConcreteTypeMirror() {

    val component: TypeMirror by lazy {
        specialization?.component
            ?: cache.types.reflect(
                java.componentType
            )
    }

    override val coreType: Type = specialization?.component?.let { component ->
        TypeFactory.arrayOf(component.coreType)
    } ?: java

    override val coreAnnotatedType: AnnotatedArrayType = specialization?.component?.let { component ->
        TypeFactory.arrayOf(component.coreAnnotatedType, typeAnnotations.toTypedArray())
    } ?: GenericTypeReflector.annotate(java, typeAnnotations.toTypedArray()) as AnnotatedArrayType

    override val raw: ArrayMirror = raw ?: this

    override fun defaultSpecialization() = TypeSpecialization.Array.DEFAULT

    fun withComponent(component: TypeMirror): ArrayMirror {
        if(!this.raw.component.isAssignableFrom(component))
            throw InvalidSpecializationException("Passed component $component is not assignable to raw component type " +
                "${this.raw.component}")
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
     * common superclass for arrays. Use [ArrayReflect] to access this array's values or cast if the result type is
     * known. If this mirror represents a non-primitive array, the returned array is filled with null values.
     */
    fun newInstance(length: Int): Any {
        return when {
            this.java == BooleanArray::class.java -> BooleanArray(length)
            this.java == ByteArray::class.java -> ByteArray(length)
            this.java == CharArray::class.java -> CharArray(length)
            this.java == ShortArray::class.java -> ShortArray(length)
            this.java == IntArray::class.java -> IntArray(length)
            this.java == LongArray::class.java -> LongArray(length)
            this.java == FloatArray::class.java -> FloatArray(length)
            this.java == DoubleArray::class.java -> DoubleArray(length)
            else -> ArrayReflect.newInstanceRaw(component.erasure, length)
        }
    }
}