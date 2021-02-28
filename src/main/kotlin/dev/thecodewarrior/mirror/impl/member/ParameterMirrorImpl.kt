package dev.thecodewarrior.mirror.impl.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.impl.MirrorCache
import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.impl.TypeMapping
import dev.thecodewarrior.mirror.impl.member.ExecutableMirrorImpl
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.Untested
import dev.thecodewarrior.mirror.member.ConstructorMirror
import dev.thecodewarrior.mirror.member.ExecutableMirror
import dev.thecodewarrior.mirror.member.Modifier
import dev.thecodewarrior.mirror.member.ParameterMirror
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Parameter

internal class ParameterMirrorImpl(
    internal val cache: MirrorCache,
    raw: ParameterMirrorImpl?,
    override val declaringExecutable: ExecutableMirror?,
    override val java: Parameter
): ParameterMirror, AnnotatedElement by java {
    override val hasName: Boolean = java.isNamePresent
    override val name: String = java.name

    override val raw: ParameterMirror = raw ?: this

    override val index: Int = java.declaringExecutable.parameters.indexOf(java)

    override val isFinal: Boolean = Modifier.FINAL in Modifier.fromModifiers(java.modifiers)

    @Untested
    override val isVarArgs: Boolean = java.isVarArgs

    override val type: TypeMirror by lazy {
        java.annotatedType.let {
            genericMapping[cache.types.reflect(it)]
        }
    }

    private val genericMapping: TypeMapping by lazy {
        TypeMapping(emptyMap()) + (declaringExecutable as ExecutableMirrorImpl?)?.genericMapping
    }

    override fun withDeclaringExecutable(enclosing: ExecutableMirror?): ParameterMirror {
        if(enclosing != null && enclosing.java != java.declaringExecutable)
            throw InvalidSpecializationException("Invalid declaring " +
                    (if(enclosing is ConstructorMirror) "constructor" else "method") +
                    " $enclosing. $this is declared in ${java.declaringExecutable}")
        return if(enclosing == null || enclosing == raw.declaringExecutable) raw else cache.parameters.specialize(this, enclosing)
    }

    override fun toString(): String {
        var str = ""
        if(isFinal)
            str += "final "
        val type = type
        if(isVarArgs && type is ArrayMirror) {
            str += "${type.component}... $name"
        } else {
            str += "$type $name"
        }
        return str
    }
}