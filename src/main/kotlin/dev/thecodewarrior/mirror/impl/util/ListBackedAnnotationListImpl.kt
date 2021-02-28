package dev.thecodewarrior.mirror.impl.util

import dev.thecodewarrior.mirror.impl.utils.ListBackedAnnotatedElement
import dev.thecodewarrior.mirror.impl.utils.unmodifiableView
import dev.thecodewarrior.mirror.util.AnnotationList

internal class ListBackedAnnotationListImpl(private val annotations: List<Annotation>) : AnnotationList(),
    List<Annotation> by annotations.unmodifiableView() {

    private val wrapper = ListBackedAnnotatedElement(annotations)

    override fun isPresent(annotationClass: Class<out Annotation>): Boolean {
        return get(annotationClass) != null
    }

    override fun <T : Annotation> get(annotationClass: Class<T>): T? {
        return wrapper.getDeclaredAnnotation(annotationClass)
    }

    override fun <T : Annotation> getAllByType(annotationClass: Class<T>): Array<T> {
        return wrapper.getDeclaredAnnotationsByType(annotationClass)
    }

    companion object {
        val EMPTY: AnnotationList = ListBackedAnnotationListImpl(emptyList())
    }
}