package dev.thecodewarrior.mirror.impl.utils

import java.lang.reflect.AnnotatedElement

internal class ListBackedAnnotatedElement(private val annotations: List<Annotation>) : AnnotatedElement {
    override fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return annotations.find { it.javaClass == annotationClass } as T?
    }

    override fun getAnnotations(): Array<Annotation> {
        return declaredAnnotations
    }

    override fun getDeclaredAnnotations(): Array<Annotation> {
        return annotations.toTypedArray()
    }
}