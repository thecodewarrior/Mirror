package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.NoSuchMirrorException
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.utils.Untested
import dev.thecodewarrior.mirror.utils.unmodifiableView
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * A searchable list of MethodMirrors
 */
@Untested
class MethodList internal constructor(
    private val type: ClassMirror, private val listName: String, private val methods: List<MethodMirror>
): List<MethodMirror> by methods.unmodifiableView() {
    private val methodNameCache = ConcurrentHashMap<String, List<MethodMirror>>()

    /**
     * Returns the methods in this list that have the specified name.
     *
     * **Note: The returned list is immutable.**
     */
    @Untested
    fun find(name: String): List<MethodMirror> {
        return methodNameCache.getOrPut(name) {
            methods.filter { it.name == name }.unmodifiableView()
        }
    }

    /**
     * Finds the method in this list that has the specified signature, or null if no such method exists.
     */
    @Untested
    fun find(name: String, vararg params: TypeMirror): MethodMirror? {
        val paramsList = params.toList()
        return methods.find {
            it.name == name && it.parameterTypes == paramsList
        }
    }

    /**
     * Finds the method in this list that has the specified raw signature, or null if no such method exists.
     */
    @Untested
    fun findRaw(name: String, vararg params: Class<*>): MethodMirror? {
        val paramsList = params.map { type.cache.types.reflect(it) }
        return methods.find {
            it.name == name && it.raw.parameterTypes == paramsList
        }
    }

    /**
     * Returns the method in this list that has the specified signature, or throws if no such method exists.
     *
     * @throws NoSuchMirrorException if no method with the specified signature exists
     */
    @Untested
    fun get(name: String, vararg params: TypeMirror): MethodMirror {
        return find(name, *params)
            ?: throw NoSuchMirrorException("Could not find $listName method $name(${params.joinToString(", ")}) in $type")
    }

    /**
     * Returns the method in this list that has the specified raw signature, or throws if no such method exists.
     *
     * @throws NoSuchMirrorException if no method with the specified signature exists
     */
    @Untested
    fun getRaw(name: String, vararg params: Class<*>): MethodMirror {
        return findRaw(name, *params)
            ?: throw NoSuchMirrorException("Could not find $listName method $name(${params.joinToString(", ")}) in $type")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is List<*>) return false

        if (methods != other) return false

        return true
    }

    override fun hashCode(): Int {
        return methods.hashCode()
    }
}