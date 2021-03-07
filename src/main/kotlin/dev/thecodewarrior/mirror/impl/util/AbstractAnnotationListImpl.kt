package dev.thecodewarrior.mirror.impl.util

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.util.AnnotationList

internal abstract class AbstractAnnotationListImpl: AnnotationList() {
    override fun toJavaString(joiner: String, trailing: String): String {
        if(this.isEmpty())
            return ""
        return this.joinToString(joiner) { Mirror.annotationToJavaString(it) } + trailing
    }

    override fun toKotlinString(joiner: String, trailing: String): String {
        if(this.isEmpty())
            return ""
        return this.joinToString(joiner) { Mirror.annotationToKotlinString(it) } + trailing
    }

}