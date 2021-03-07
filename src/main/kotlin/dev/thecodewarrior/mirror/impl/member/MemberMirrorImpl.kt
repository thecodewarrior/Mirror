package dev.thecodewarrior.mirror.impl.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.impl.util.ElementBackedAnnotationListImpl
import dev.thecodewarrior.mirror.impl.utils.Untested
import dev.thecodewarrior.mirror.member.MemberMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.util.AnnotationList
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Member

internal abstract class MemberMirrorImpl internal constructor(
    val cache: MirrorCache,
    val annotatedElement: AnnotatedElement,
    _enclosing: ClassMirror?
): MemberMirror {
    @Untested
    override val isKotlinMember: Boolean
        get() = declaringClass.isKotlinClass

    override val declaringClass: ClassMirror by lazy {
        _enclosing ?: cache.types.reflect(java.declaringClass) as ClassMirror
    }

    override val annotations: AnnotationList by lazy {
        ElementBackedAnnotationListImpl(annotatedElement, false)
    }

    override val declaredAnnotations: AnnotationList by lazy {
        ElementBackedAnnotationListImpl(annotatedElement, false)
    }

    override fun toDeclarationString(): String {
        return if(isKotlinMember) {
            toKotlinDeclarationString()
        } else {
            toJavaDeclarationString()
        }
    }
}
