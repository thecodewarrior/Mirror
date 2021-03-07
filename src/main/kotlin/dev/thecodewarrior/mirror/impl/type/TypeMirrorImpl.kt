package dev.thecodewarrior.mirror.impl.type

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.impl.coretypes.CoreTypeUtils
import dev.thecodewarrior.mirror.impl.util.ListBackedAnnotationListImpl
import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.Untested
import dev.thecodewarrior.mirror.util.AnnotationList

internal abstract class TypeMirrorImpl: TypeMirror {
    /**
     * The cache this mirror was created by. Mirrors from other caches will not be considered equal even if they
     * represent the same type. However, no production code should use anything but
     * [Mirror.reflect], which uses a global cache.
     */
    internal abstract val cache: MirrorCache

    override val erasure: Class<*> get() = CoreTypeUtils.erase(coreType)

    internal abstract val specialization: TypeSpecialization?
    internal abstract fun defaultSpecialization(): TypeSpecialization

    internal abstract fun applySpecialization(specialization: TypeSpecialization): TypeMirror

    internal inline fun <reified T: TypeSpecialization> defaultApplySpecialization(
        specialization: TypeSpecialization,
        rawTest: (T) -> Boolean,
        crossinline specializedConstructor: (T) -> TypeMirror
    ): TypeMirror {
        if(specialization !is T)
            throw InvalidSpecializationException("Can't apply ${specialization.javaClass}" +
                " to ${this.javaClass.simpleName } $this")
        if(specialization.annotations == this.specialization?.annotations ?: emptyList<Annotation>() &&
            rawTest(specialization)
        )
            return raw

        return specializedConstructor(specialization)
    }

    protected fun withTypeAnnotationsImpl(annotations: List<Annotation>): TypeMirror {
        return cache.types.specialize(raw,
            (this.specialization ?: this.defaultSpecialization()).copy(
                annotations = annotations
            )
        )
    }

    override val typeAnnotations: AnnotationList by lazy {
        specialization?.annotations?.let { ListBackedAnnotationListImpl(it) } ?: ListBackedAnnotationListImpl.EMPTY
    }

    @Throws(ClassCastException::class)
    override fun asClassMirror(): ClassMirror {
        return this as ClassMirror
    }

    @Throws(ClassCastException::class)
    override fun asArrayMirror(): ArrayMirror {
        return this as ArrayMirror
    }
}

