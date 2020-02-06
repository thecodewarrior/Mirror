package dev.thecodewarrior.mirror.member

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.type.TypeMapping
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.UntestedNegative
import dev.thecodewarrior.mirror.utils.unmodifiableView
import java.lang.reflect.Parameter

class ParameterMirror internal constructor(
    internal val cache: MirrorCache,
    raw: ParameterMirror?,
    val declaringExecutable: ExecutableMirror?,
    internal val java: Parameter
) {
    val name: String? = if(java.isNamePresent) java.name else null

    val raw: ParameterMirror = raw ?: this
    @Untested
    val isFinal: Boolean = Modifier.FINAL in Modifier.fromModifiers(java.modifiers)

    val type: TypeMirror by lazy {
        java.annotatedType.let {
            genericMapping[cache.types.reflect(it)]
        }
    }

    //todo: Should this be a public API? It is in ClassMirror, should it be private there too?
    @Untested
    val genericMapping: TypeMapping by lazy {
        TypeMapping(emptyMap()) + declaringExecutable?.genericMapping
    }

    /**
     * Returns annotations that are present on the parameter this mirror represents.
     *
     * **Note: this value is immutable**
     *
     * @see Parameter.getAnnotations
     */
    @UntestedNegative
    val annotations: List<Annotation> = java.annotations.toList().unmodifiableView()

    fun withDeclaringExecutable(executable: ExecutableMirror?): ParameterMirror {
        if(executable != null && executable.java != java.declaringExecutable)
            throw InvalidSpecializationException("Invalid declaring " +
                (if(executable is ConstructorMirror) "constructor" else "method") +
                " $executable. $this is declared in ${java.declaringExecutable}")
        return if(executable == null) raw else cache.parameters.specialize(this, executable)
    }

    // todo: document why parameters have `equals` and `hashCode` and why everything else is by identity
    // todo: this won't even work right when there are annotations that aren't equal. remove this.
    @Untested
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParameterMirror) return false

        if (cache != other.cache) return false
        if (java != other.java) return false
        if (type != other.type) return false

        return true
    }

    @Untested
    override fun hashCode(): Int {
        var result = cache.hashCode()
        result = 31 * result + java.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    @Untested
    override fun toString(): String {
        var str = ""
        str += "$type $name"
        return str
    }
}