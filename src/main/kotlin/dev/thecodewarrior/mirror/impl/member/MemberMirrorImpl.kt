package dev.thecodewarrior.mirror.impl.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.member.MemberMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Member

internal abstract class MemberMirrorImpl internal constructor(
    val cache: MirrorCache,
    val annotatedElement: AnnotatedElement,
    _enclosing: ClassMirror?
): MemberMirror {

    override val declaringClass: ClassMirror by lazy {
        _enclosing ?: cache.types.reflect(java.declaringClass) as ClassMirror
    }

    override fun <T: Annotation> getAnnotation(annotationClass: Class<T>): T? {
        return annotatedElement.getAnnotation(annotationClass)
    }

    override fun getAnnotations(): Array<Annotation> {
        return annotatedElement.annotations
    }

    override fun getDeclaredAnnotations(): Array<Annotation> {
        return annotatedElement.declaredAnnotations
    }
}
