package com.teamwizardry.mirror.reflection.type

import com.teamwizardry.mirror.reflection.abstractionlayer.type.AbstractTypeVariable
import com.teamwizardry.mirror.reflection.MirrorCache
import com.teamwizardry.mirror.reflection.abstractionlayer.type.AbstractClass
import com.teamwizardry.mirror.reflection.utils.lazyOrSet

/**
 * A mirror representing a class
 */
class ClassMirror internal constructor(override val cache: MirrorCache, override val abstractType: AbstractClass): ConcreteTypeMirror() {
    override val rawType = abstractType.type

    /**
     * The supertype of this class. This property is `null` if this reflect represents [Object], an interface,
     * a primitive, or `void`
     */
    val superclass: ClassMirror? by lazy {
        abstractType.genericSuperclass?.let {
            cache.specializeMapping(it, genericMapping) as ClassMirror
        }
    }

    /**
     * The list of interfaces directly implemented by this type, in the order they appear in the source code.
     */
    val interfaces: List<ClassMirror> by lazy {
        abstractType.genericInterfaces.map {
            cache.specializeMapping(it, genericMapping) as ClassMirror
        }
    }

    /**
     * The list of type parameters of this class. These will be replaced with the passed types when specializing.
     * Use [raw] to get the type parameters of the original class.
     */
    var typeParameters: List<TypeMirror> by lazyOrSet {
        abstractType.typeParameters.map { cache.reflect(it) }
    }
        internal set

    /**
     * The raw, unspecialized version of this class.
     */
    var raw: ClassMirror = this
        internal set

    private val genericMapping: Map<AbstractTypeVariable, TypeMirror> by lazy {
        raw.typeParameters.indices.associate {
            (raw.typeParameters[it].abstractType as AbstractTypeVariable) to typeParameters[it]
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClassMirror) return false

        if (cache != other.cache) return false
        if (abstractType != other.abstractType) return false
        if (typeParameters != other.typeParameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cache.hashCode()
        result = 31 * result + abstractType.hashCode()
        result = 31 * result + typeParameters.hashCode()
        return result
    }
}

