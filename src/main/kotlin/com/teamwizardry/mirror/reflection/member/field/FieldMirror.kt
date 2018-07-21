package com.teamwizardry.mirror.reflection.member.field

import com.teamwizardry.mirror.reflection.MirrorCache
import com.teamwizardry.mirror.reflection.abstractionlayer.field.AbstractField
import com.teamwizardry.mirror.reflection.type.TypeMirror
import com.teamwizardry.mirror.reflection.utils.lazyOrSet

class FieldMirror internal constructor(val cache: MirrorCache, val abstractField: AbstractField) {

    var raw: FieldMirror = this
        internal set
    val isEnumConstant = abstractField.isEnumConstant

    var name: String = abstractField.name

    var declaringClass: TypeMirror by lazyOrSet {
        cache.types.reflect(abstractField.declaringClass)
    }
        internal set

    var type: TypeMirror by lazyOrSet {
        cache.types.reflect(abstractField.type)
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