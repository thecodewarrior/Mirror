package dev.thecodewarrior.mirror.impl.type

import dev.thecodewarrior.mirror.ArrayReflect
import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.impl.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.impl.coretypes.TypeImplAccess
import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.Untested
import java.lang.reflect.AnnotatedArrayType
import java.lang.reflect.Type

internal class ArrayMirrorImpl internal constructor(
    override val cache: MirrorCache,
    override val java: Class<*>,
    raw: ArrayMirror?,
    override val specialization: TypeSpecialization.Array?
): TypeMirrorImpl(), ArrayMirror {

    override val component: TypeMirror by lazy {
        specialization?.component
            ?: cache.types.reflect(
                java.componentType
            )
    }

    override val coreType: Type = specialization?.component?.let { component ->
        TypeImplAccess.createArrayType(component.coreType)
    } ?: java

    override val coreAnnotatedType: AnnotatedArrayType = specialization?.component?.let { component ->
        TypeImplAccess.createArrayType(component.coreAnnotatedType, typeAnnotations.toTypedArray())
    } ?: CoreTypeUtils.annotate(java, typeAnnotations.toTypedArray()) as AnnotatedArrayType

    override val raw: ArrayMirror = raw ?: this

    override fun defaultSpecialization() = TypeSpecialization.Array.DEFAULT

    override fun withComponent(component: TypeMirror): ArrayMirror {
        if(!this.raw.component.isAssignableFrom(component))
            throw InvalidSpecializationException("Passed component $component is not assignable to raw component type " +
                    "${this.raw.component}")
        val newSpecialization = (specialization ?: defaultSpecialization()).copy(component = component)
        return cache.types.specialize(this, newSpecialization) as ArrayMirror
    }

    override fun withTypeAnnotations(annotations: List<Annotation>): ArrayMirror {
        return withTypeAnnotationsImpl(annotations) as ArrayMirror
    }

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Array>(
            specialization,
            { it.component == this.component || it.component == null}
        ) {
            ArrayMirrorImpl(cache, java, raw, it)
        }
    }

    override fun isAssignableFrom(other: TypeMirror): Boolean {
        if(other == this) return true
        if (other !is ArrayMirror)
            return false
        return component.isAssignableFrom(other.component)
    }

    override fun newInstance(length: Int): Any {
        return when (this.java) {
            BooleanArray::class.java -> BooleanArray(length)
            ByteArray::class.java -> ByteArray(length)
            CharArray::class.java -> CharArray(length)
            ShortArray::class.java -> ShortArray(length)
            IntArray::class.java -> IntArray(length)
            LongArray::class.java -> LongArray(length)
            FloatArray::class.java -> FloatArray(length)
            DoubleArray::class.java -> DoubleArray(length)
            else -> ArrayReflect.newInstanceRaw(component.erasure, length)
        }
    }

    @Untested
    override fun toString(): String {
        return toJavaString()
    }

    @Untested
    override fun toJavaString(): String {
        var str = "$component"
        str += typeAnnotations.toJavaString(joiner = " ", trailing = " ")
        str += "[]"
        return str
    }

    @Untested
    override fun toKotlinString(): String {
        TODO("Not yet implemented")
    }
}
