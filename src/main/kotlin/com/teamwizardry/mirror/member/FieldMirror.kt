package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.InvalidSpecializationException
import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.type.TypeMapping
import com.teamwizardry.mirror.type.TypeMirror
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class FieldMirror internal constructor(
    internal val cache: MirrorCache,
    raw: FieldMirror?,
    val java: Field,
    val specialization: TypeMirror?
) {

    var raw: FieldMirror = raw ?: this
    val isEnumConstant: Boolean = java.isEnumConstant

    val name: String = java.name
    val isStatic: Boolean = Modifier.isStatic(java.modifiers)
    val isTransient: Boolean = Modifier.isTransient(java.modifiers)
    val isVolatile: Boolean = Modifier.isVolatile(java.modifiers)
    val accessLevel: AccessLevel = AccessLevel.fromModifiers(java.modifiers)

    val type: TypeMirror by lazy {
        specialization ?: java.annotatedType.let { cache.types.reflect(it) }
    }

    fun specialize(mapping: TypeMapping): FieldMirror {
        if(this.specialization != null)
            throw InvalidSpecializationException("Can't apply specialization to specialized mirror $this")
        return cache.fields.specialize(this, mapping[type])
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FieldMirror) return false

        if (cache != other.cache) return false
        if (java != other.java) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cache.hashCode()
        result = 31 * result + java.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        var str = ""
        str += "$type $name"
        return str
    }
}