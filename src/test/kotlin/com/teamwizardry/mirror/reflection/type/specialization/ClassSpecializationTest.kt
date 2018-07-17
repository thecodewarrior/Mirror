package com.teamwizardry.mirror.reflection.type.specialization

import com.teamwizardry.mirror.reflection.Mirror
import com.teamwizardry.mirror.reflection.MirrorTestBase
import com.teamwizardry.mirror.reflection.type.Object1
import com.teamwizardry.mirror.reflection.type.TypeMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ClassSpecializationTest: MirrorTestBase() {
    @Test
    @DisplayName("Specializing generic type should return mirror with new type parameters and correct value for raw")
    fun basicSpecialization() {
        class SimpleGeneric<T>
        val genericType = Mirror.reflectClass(SimpleGeneric::class.java)
        val specializeWith = Mirror.reflectClass<Object1>()
        val specialized = genericType.specialize(specializeWith)

        assertNotEquals(genericType, specialized)
        assertEquals(listOf(specializeWith), specialized.typeParameters)
        assertEquals(genericType, specialized.raw)
    }

    @Test
    @DisplayName("Specializing type with wrong number of arguments should throw IllegalArgumentException")
    fun specializingWrongParameterCount() {
        class SimpleGeneric<T>
        val genericType = Mirror.reflectClass(SimpleGeneric::class.java)
        val specializeWith = Mirror.reflectClass<Object1>()
        assertThrows<IllegalArgumentException> {
            genericType.specialize(specializeWith, specializeWith)
        }
    }

    //=================================================== Supertype ====================================================

    @Test
    @DisplayName("`T1 extends T2<SomeType>; T2<A>`: Reflecting T1 should return a mirror whose supertype is T2 " +
            "specialized for SomeType")
    fun explicitSupertypeSpecialization() {
        open class SimpleGeneric<T>
        class SimpleSubtype: SimpleGeneric<String>()
        val simpleType = Mirror.reflectClass(SimpleSubtype::class.java)
        val supertype = simpleType.superclass!!

        val generic = Mirror.reflectClass(SimpleGeneric::class.java)
        assertEquals(generic.specialize(Mirror.reflect<String>()), supertype)
    }

    @Test
    @DisplayName("`T1<A> extends T2<A>; T2<B>`: Reflecting T1 should return a mirror whose supertype is T2 " +
            "specialized for A")
    fun reflectedHandoffToSupertype() {
        open class SimpleGeneric<T>
        class SimpleSubtype<A>: SimpleGeneric<A>()
        val simpleType = Mirror.reflectClass(SimpleSubtype::class.java)
        val supertype = simpleType.superclass!!
        val simpleTypeVariable = simpleType.typeParameters[0]

        assertEquals(listOf(simpleTypeVariable), supertype.typeParameters)
    }

    @Test
    @DisplayName("`T1<A> extends T2<A>; T2<B>`: Specializing T1 for SomeType should return a mirror whose supertype " +
            "is specialized for SomeType")
    fun specializedHandoffToSupertype() {
        open class SimpleGeneric<T>
        class SimpleSubtype<A>: SimpleGeneric<A>()
        val specialized = Mirror.reflectClass(SimpleSubtype::class.java).specialize(Mirror.reflect<String>())
        val supertype = specialized.superclass!!

        assertEquals(listOf(Mirror.reflect<String>()), supertype.typeParameters)
    }

    @Test
    @DisplayName("`T1<A> extends T2<T3<A>>; T2<B>; T3<C>`: Reflecting T1 should return mirror whose supertype is " +
            "specialized for T3<A>")
    fun reflectedHandoffWrappedToSupertype() {
        open class SimpleGeneric<T>
        class IntermediateGeneric<T>
        class SimpleSubtype<A>: SimpleGeneric<IntermediateGeneric<A>>()
        val subtype = Mirror.reflectClass(SimpleSubtype::class.java)
        val supertype = subtype.superclass!!

        assertEquals(listOf(
                Mirror.reflectClass(IntermediateGeneric::class.java)
                        .specialize(subtype.typeParameters[0])
        ), supertype.typeParameters)
    }

    @Test
    @DisplayName("`T1<A> extends T2<T3<A>>; T2<B>; T3<C>`: Specializing T1 for SomeType should return mirror whose " +
            "supertype is specialized for T3<SomeType>")
    fun specializeHandoffWrappedToSupertype() {
        open class SimpleGeneric<T>
        class IntermediateGeneric<T>
        class SimpleSubtype<A>: SimpleGeneric<IntermediateGeneric<A>>()
        val subtype = Mirror.reflectClass(SimpleSubtype::class.java).specialize(Mirror.reflect<String>())
        val supertype = subtype.superclass!!

        assertEquals(listOf(
                Mirror.reflectClass(IntermediateGeneric::class.java)
                        .specialize(Mirror.reflect<String>())
        ), supertype.typeParameters)
    }
}