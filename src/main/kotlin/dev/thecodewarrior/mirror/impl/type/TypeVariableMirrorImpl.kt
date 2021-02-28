package dev.thecodewarrior.mirror.impl.type

import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.impl.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.type.TypeVariableMirror
import dev.thecodewarrior.mirror.type.WildcardMirror
import dev.thecodewarrior.mirror.impl.utils.annotationString
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.AnnotatedTypeVariable
import java.lang.reflect.TypeVariable

internal class TypeVariableMirrorImpl internal constructor(
    override val cache: MirrorCache,
    override val coreType: TypeVariable<*>,
    raw: TypeVariableMirror?,
    override val specialization: TypeSpecialization.Common?
): TypeMirrorImpl(), AnnotatedElement by coreType, TypeVariableMirror {

    override val coreAnnotatedType: AnnotatedTypeVariable
            = CoreTypeUtils.annotate(coreType, typeAnnotations.toTypedArray()) as AnnotatedTypeVariable

    override val raw: TypeVariableMirror = raw ?: this

    override val bounds: List<TypeMirror> by lazy {
        coreType.annotatedBounds.map { cache.types.reflect(it) }
    }

    override fun defaultSpecialization() = TypeSpecialization.Common.DEFAULT

    override fun applySpecialization(specialization: TypeSpecialization): TypeMirror {
        return defaultApplySpecialization<TypeSpecialization.Common>(
            specialization,
            { true }
        ) {
            TypeVariableMirrorImpl(cache, coreType, raw, it)
        }
    }

    override fun isAssignableFrom(other: TypeMirror): Boolean {
        return when(other) {
            this -> true
            is TypeVariableMirror -> this in other.bounds
            is WildcardMirror -> this in other.upperBounds
            else -> false
        }
    }

    override fun toString(): String {
        var str = ""
        str += typeAnnotations.annotationString()
        str += coreType.name
        return str
    }

    override fun toDeclarationString(): String {
        var str = ""
        str += coreType.annotations.annotationString()
        str += coreType.name
        if(bounds.isNotEmpty() && !(bounds.size == 1 && bounds[0] == cache.types.reflect(Any::class.java))) {
            str += " extends ${bounds.joinToString(" & ")}"
        }
        return str
    }
}
