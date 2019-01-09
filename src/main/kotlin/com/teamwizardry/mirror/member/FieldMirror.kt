package com.teamwizardry.mirror.member

import com.teamwizardry.mirror.MirrorCache
import com.teamwizardry.mirror.abstractionlayer.field.AbstractField
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.mirror.utils.lazyOrSet
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class FieldMirror internal constructor(internal val cache: MirrorCache, internal val abstractField: AbstractField) {

    val java: Field = abstractField.field

    var raw: FieldMirror = this
        internal set
    val isEnumConstant: Boolean = abstractField.isEnumConstant

    val name: String = abstractField.name
    val isStatic: Boolean = Modifier.isStatic(abstractField.modifiers)
    val isTransient: Boolean = Modifier.isTransient(abstractField.modifiers)
    val isVolatile: Boolean = Modifier.isVolatile(abstractField.modifiers)
    val accessLevel: AccessLevel = AccessLevel.fromModifiers(abstractField.modifiers)

    var type: TypeMirror by lazyOrSet {
        abstractField.type.annotated?.let { cache.types.reflect(it) }
            ?: cache.types.reflect(abstractField.type.type)
    }
        internal set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FieldMirror) return false

        if (cache != other.cache) return false
        if (abstractField != other.abstractField) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cache.hashCode()
        result = 31 * result + abstractField.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        var str = ""
        str += "$type $name"
        return str
    }
}