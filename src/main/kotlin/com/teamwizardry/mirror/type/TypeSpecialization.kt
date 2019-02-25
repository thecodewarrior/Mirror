package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.member.MethodMirror
import com.teamwizardry.mirror.utils.unmodifiableCopy

internal abstract class TypeSpecialization private constructor(annotations: List<Annotation>) {
    val annotations: List<Annotation> = annotations.unmodifiableCopy()

    abstract fun copy(
        annotations: List<Annotation> = this.annotations
    ): TypeSpecialization

    class Common(annotations: List<Annotation>): TypeSpecialization(annotations) {
        override fun copy(
            annotations: List<Annotation>
        ): Common {
            return Common(annotations)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Common) return false

            if (annotations != other.annotations) return false

            return true
        }

        override fun hashCode(): Int {
            var result = annotations.hashCode()
            return result
        }

        companion object {
            val DEFAULT = Common(emptyList())
        }
    }

    class Class(annotations: List<Annotation>, arguments: List<TypeMirror>?, val enclosingClass: ClassMirror?, val enclosingMethod: MethodMirror?): TypeSpecialization(annotations) {
        val arguments: List<TypeMirror>? = if(arguments?.isNotEmpty() == true) arguments.unmodifiableCopy() else null

        override fun copy(
            annotations: List<Annotation>
        ): Class {
            return Class(annotations, arguments, enclosingClass, enclosingMethod)
        }

        fun copy(
            annotations: List<Annotation> = this.annotations,
            arguments: List<TypeMirror>? = this.arguments,
            enclosingClass: ClassMirror? = this.enclosingClass,
            enclosingMethod: MethodMirror? = this.enclosingMethod
        ): Class {
            return Class(annotations, arguments, enclosingClass, enclosingMethod)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Class) return false

            if (annotations != other.annotations) return false
            if (arguments != other.arguments) return false
            if (enclosingClass != other.enclosingClass) return false
            if (enclosingMethod != other.enclosingMethod) return false

            return true
        }

        override fun hashCode(): Int {
            var result = annotations.hashCode()
            result = 31 * result + arguments.hashCode()
            result = 31 * result + enclosingClass.hashCode()
            result = 31 * result + enclosingMethod.hashCode()
            return result
        }

        companion object {
            val DEFAULT = Class(emptyList(), null, null, null)
        }
    }

    class Array(annotations: List<Annotation>, val component: TypeMirror?): TypeSpecialization(annotations) {
        override fun copy(
            annotations: List<Annotation>
        ): Array {
            return Array(annotations, component)
        }

        fun copy(
            annotations: List<Annotation> = this.annotations,
            component: TypeMirror? = this.component
        ): Array {
            return Array(annotations, component)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Array) return false

            if (annotations != other.annotations) return false
            if (component != other.component) return false

            return true
        }

        override fun hashCode(): Int {
            var result = annotations.hashCode()
            result = 31 * result + component.hashCode()
            return result
        }

        companion object {
            val DEFAULT = Array(emptyList(), null)
        }
    }
}
