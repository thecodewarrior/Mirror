package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.unmodifiableCopy

class ExecutableSpecialization(val enclosing: ClassMirror?, arguments: List<TypeMirror>?) {
    val arguments: List<TypeMirror>? = arguments?.unmodifiableCopy()

    fun copy(
        enclosing: ClassMirror? = this.enclosing,
        arguments: List<TypeMirror>? = this.arguments
    ): ExecutableSpecialization {
        return ExecutableSpecialization(enclosing, arguments)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExecutableSpecialization) return false

        if (enclosing != other.enclosing) return false
        if (arguments != other.arguments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enclosing.hashCode()
        result = 31 * result + arguments.hashCode()
        return result
    }
}