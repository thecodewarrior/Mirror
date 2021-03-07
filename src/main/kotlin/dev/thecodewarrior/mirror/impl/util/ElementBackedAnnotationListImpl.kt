package dev.thecodewarrior.mirror.impl.util

import dev.thecodewarrior.mirror.impl.utils.unmodifiableView
import dev.thecodewarrior.mirror.util.AnnotationList
import java.lang.reflect.AnnotatedElement

internal class ElementBackedAnnotationListImpl private constructor(
    val element: AnnotatedElement,
    val useDeclared: Boolean,
    backingList: List<Annotation>
) : AbstractAnnotationListImpl(), List<Annotation> by backingList {

    constructor(element: AnnotatedElement, useDeclared: Boolean) : this(
        element,
        useDeclared,
        when(useDeclared) {
            true -> element.declaredAnnotations
            false -> element.annotations
        }.toList().unmodifiableView()
    )

    override fun isPresent(annotationClass: Class<out Annotation>): Boolean {
        return get(annotationClass) != null
    }

    override fun <T : Annotation> get(annotationClass: Class<T>): T? {
        return when(useDeclared) {
            true -> element.getDeclaredAnnotation(annotationClass)
            false -> element.getAnnotation(annotationClass)
        }
    }

    override fun <T : Annotation> getAllByType(annotationClass: Class<T>): Array<T> {
        return when(useDeclared) {
            true -> element.getDeclaredAnnotationsByType(annotationClass)
            false -> element.getAnnotationsByType(annotationClass)
        }
    }
}