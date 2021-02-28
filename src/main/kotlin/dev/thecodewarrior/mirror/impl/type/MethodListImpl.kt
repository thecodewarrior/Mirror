package dev.thecodewarrior.mirror.impl.type

import dev.thecodewarrior.mirror.NoSuchMirrorException
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.type.MethodList
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.mirror.impl.utils.unmodifiableView
import java.util.concurrent.ConcurrentHashMap

internal class MethodListImpl internal constructor(
    private val type: ClassMirrorImpl, private val listName: String, private val methods: List<MethodMirror>
): List<MethodMirror> by methods.unmodifiableView(), MethodList {
    private val methodNameCache = ConcurrentHashMap<String, List<MethodMirror>>()

    override fun findAll(name: String): List<MethodMirror> {
        return methodNameCache.getOrPut(name) {
            methods.filter { it.name == name }.unmodifiableView()
        }
    }

    override fun find(name: String, vararg params: TypeMirror): MethodMirror? {
        val paramsList = params.toList()
        return methods.find {
            it.name == name && it.parameterTypes == paramsList
        }
    }

    override fun findRaw(name: String, vararg params: Class<*>): MethodMirror? {
        return methods.find { method ->
            @Suppress("PlatformExtensionReceiverOfInline")
            method.name == name && method.java.parameterTypes.contentEquals(params)
        }
    }

    override fun get(name: String, vararg params: TypeMirror): MethodMirror {
        return find(name, *params)
            ?: throw NoSuchMirrorException("Could not find $listName method $name(${params.joinToString(", ")}) in $type")
    }

    override fun getRaw(name: String, vararg params: Class<*>): MethodMirror {
        return findRaw(name, *params)
            ?: throw NoSuchMirrorException("Could not find $listName method $name(${params.joinToString(", ")}) in $type")
    }
}
