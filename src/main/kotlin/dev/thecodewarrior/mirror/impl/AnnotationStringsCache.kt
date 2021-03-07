package dev.thecodewarrior.mirror.impl

import dev.thecodewarrior.mirror.MirrorException
import java.util.concurrent.ConcurrentHashMap

internal class AnnotationStringsCache {
    private val cache = ConcurrentHashMap<Class<*>, AnnotationStringsImpl>()

    fun getStringConverter(type: Class<out Annotation>): AnnotationStringsImpl {
        @Suppress("UNCHECKED_CAST")
        val annotationType = if(type.isAnnotation) {
            type
        } else {
            type.interfaces.find { it.isAnnotation } as Class<out Annotation>?
                ?: throw MirrorException("Could not find the annotation interface for ${type.canonicalName}")
        }
        return cache.getOrPut(annotationType) { AnnotationStringsImpl(annotationType, this) }
    }
}