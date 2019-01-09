package com.teamwizardry.mirror.type

import com.teamwizardry.mirror.utils.unmodifiableCopy

internal abstract class TypeSpecialization private constructor(annotations: List<Annotation>, markedNull: Boolean) {
    val annotations: List<Annotation> = annotations.unmodifiableCopy()
    val markedNull: Boolean = markedNull

    abstract fun copy(
        annotations: List<Annotation> = this.annotations,
        markedNull: Boolean = this.markedNull
    ): TypeSpecialization

    class Common(annotations: List<Annotation>, markedNull: Boolean): TypeSpecialization(annotations, markedNull) {
        override fun copy(
            annotations: List<Annotation>,
            markedNull: Boolean
        ): Common {
            return Common(annotations, markedNull)
        }

        override fun toString(): String {
            return ( "" +
                annotations.joinToString(", ") { "@$it" } +
                " ___ " +
                (if(markedNull) "?" else "")
                ).trim()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Common) return false

            if (annotations != other.annotations) return false
            if (markedNull != other.markedNull) return false

            return true
        }

        override fun hashCode(): Int {
            var result = annotations.hashCode()
            result = 31 * result + markedNull.hashCode()
            return result
        }

        companion object {
            val DEFAULT = Common(emptyList(), false)
        }
    }

    class Class(annotations: List<Annotation>, markedNull: Boolean, arguments: List<TypeMirror>?): TypeSpecialization(annotations, markedNull) {
        val arguments: List<TypeMirror>? = if(arguments?.isNotEmpty() == true) arguments.unmodifiableCopy() else null

        override fun copy(
            annotations: List<Annotation>,
            markedNull: Boolean
        ): Class {
            return Class(annotations, markedNull, arguments)
        }

        fun copy(
            annotations: List<Annotation> = this.annotations,
            markedNull: Boolean = this.markedNull,
            arguments: List<TypeMirror>? = this.arguments
        ): Class {
            return Class(annotations, markedNull, arguments)
        }

        override fun toString(): String {
            return ( "" +
                annotations.joinToString(", ") { "@$it" } +
                " ___" +
                (arguments?.let { "<${arguments.joinToString(", ")}> " } ?: "") +
                (if(markedNull) "?" else "")
                ).trim()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Class) return false

            if (annotations != other.annotations) return false
            if (arguments != other.arguments) return false
            if (markedNull != other.markedNull) return false

            return true
        }

        override fun hashCode(): Int {
            var result = annotations.hashCode()
            result = 31 * result + arguments.hashCode()
            result = 31 * result + markedNull.hashCode()
            return result
        }

        companion object {
            val DEFAULT = Class(emptyList(), false, null)
        }
    }

    class Array(annotations: List<Annotation>, markedNull: Boolean, val component: TypeMirror?): TypeSpecialization(annotations, markedNull) {
        override fun copy(
            annotations: List<Annotation>,
            markedNull: Boolean
        ): Array {
            return Array(annotations, markedNull, component)
        }

        fun copy(
            annotations: List<Annotation> = this.annotations,
            markedNull: Boolean = this.markedNull,
            component: TypeMirror? = this.component
        ): Array {
            return Array(annotations, markedNull, component)
        }

        override fun toString(): String {
            return ( "" +
                annotations.joinToString(", ") +
                "$component[]" +
                (if(markedNull) "?" else "")
                ).trim()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Array) return false

            if (annotations != other.annotations) return false
            if (component != other.component) return false
            if (markedNull != other.markedNull) return false

            return true
        }

        override fun hashCode(): Int {
            var result = annotations.hashCode()
            result = 31 * result + component.hashCode()
            result = 31 * result + markedNull.hashCode()
            return result
        }

        companion object {
            val DEFAULT = Array(emptyList(), false, null)
        }
    }
}
