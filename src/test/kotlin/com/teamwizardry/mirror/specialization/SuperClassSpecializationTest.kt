package com.teamwizardry.mirror.specialization

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.reflection.testsupport.MirrorTestBase
import com.teamwizardry.mirror.reflection.testsupport.GenericInterface1
import com.teamwizardry.mirror.reflection.testsupport.GenericObject1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class SuperClassSpecializationTest: MirrorTestBase() {
    @Test
    @DisplayName("`T1 extends T2<SomeType>; T2<A>`: Reflecting T1 should return a mirror whose supertype is T2 " +
            "specialized for SomeType")
    fun explicitSupertypeSpecialization() {
        class SimpleSubtype: GenericObject1<String>()
        val simpleType = Mirror.reflectClass(SimpleSubtype::class.java)
        val supertype = simpleType.superclass!!

        val generic = Mirror.reflectClass(GenericObject1::class.java)
        assertEquals(generic.specialize(Mirror.reflect<String>()), supertype)
    }

    @Test
    @DisplayName("`T1<A> extends T2<A>; T2<B>`: Reflecting T1 should return a mirror whose supertype is T2 " +
            "specialized for A")
    fun reflectedHandoffToSupertype() {
        class SimpleSubtype<A>: GenericObject1<A>()
        val simpleType = Mirror.reflectClass(SimpleSubtype::class.java)
        val supertype = simpleType.superclass!!
        val simpleTypeVariable = simpleType.typeParameters[0]

        assertEquals(listOf(simpleTypeVariable), supertype.typeParameters)
    }

    @Test
    @DisplayName("`T1<A> extends T2<A>; T2<B>`: Specializing T1 for SomeType should return a mirror whose supertype " +
            "is specialized for SomeType")
    fun specializedHandoffToSupertype() {
        class SimpleSubtype<A>: GenericObject1<A>()
        val specialized = Mirror.reflectClass(SimpleSubtype::class.java).specialize(Mirror.reflect<String>())
        val supertype = specialized.superclass!!

        assertEquals(listOf(Mirror.reflect<String>()), supertype.typeParameters)
    }

    @Test
    @DisplayName("`T1<A> extends T2<T3<A>>; T2<B>; T3<C>`: Reflecting T1 should return mirror whose supertype is " +
            "specialized for T3<A>")
    fun reflectedHandoffWrappedToSupertype() {
        class SimpleSubtype<A>: GenericObject1<GenericInterface1<A>>()
        val subtype = Mirror.reflectClass(SimpleSubtype::class.java)
        val supertype = subtype.superclass!!

        assertEquals(listOf(
                Mirror.reflectClass(GenericInterface1::class.java)
                        .specialize(subtype.typeParameters[0])
        ), supertype.typeParameters)
    }

    @Test
    @DisplayName("`T1<A> extends T2<T3<A>>; T2<B>; T3<C>`: Specializing T1 for SomeType should return mirror whose " +
            "supertype is specialized for T3<SomeType>")
    fun specializeHandoffWrappedToSupertype() {
        class SimpleSubtype<A>: GenericObject1<GenericInterface1<A>>()
        val subtype = Mirror.reflectClass(SimpleSubtype::class.java).specialize(Mirror.reflect<String>())
        val supertype = subtype.superclass!!

        assertEquals(listOf(
                Mirror.reflectClass(GenericInterface1::class.java)
                        .specialize(Mirror.reflect<String>())
        ), supertype.typeParameters)
    }
}