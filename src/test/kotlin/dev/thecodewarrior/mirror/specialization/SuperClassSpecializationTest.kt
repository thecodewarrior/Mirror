package dev.thecodewarrior.mirror.specialization

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.GenericInterface1
import dev.thecodewarrior.mirror.testsupport.GenericObject1
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.assertSameList
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
        assertEquals(generic.withTypeArguments(Mirror.reflect<String>()), supertype)
    }

    @Test
    @DisplayName("`T1<A> extends T2<A>; T2<B>`: Reflecting T1 should return a mirror whose supertype is T2 " +
            "specialized for A")
    fun reflectedHandoffToSupertype() {
        class SimpleSubtype<A>: GenericObject1<A>()
        val simpleType = Mirror.reflectClass(SimpleSubtype::class.java)
        val supertype = simpleType.superclass!!
        val simpleTypeVariable = simpleType.typeParameters[0]

        assertSameList(listOf(simpleTypeVariable), supertype.typeParameters)
    }

    @Test
    @DisplayName("`T1<A> extends T2<A>; T2<B>`: Specializing T1 for SomeType should return a mirror whose supertype " +
            "is specialized for SomeType")
    fun specializedHandoffToSupertype() {
        class SimpleSubtype<A>: GenericObject1<A>()
        val specialized = Mirror.reflectClass(SimpleSubtype::class.java).withTypeArguments(Mirror.reflect<String>())
        val supertype = specialized.superclass!!

        assertSameList(listOf(Mirror.reflect<String>()), supertype.typeParameters)
    }

    @Test
    @DisplayName("`T1<A> extends T2<T3<A>>; T2<B>; T3<C>`: Reflecting T1 should return mirror whose supertype is " +
            "specialized for T3<A>")
    fun reflectedHandoffWrappedToSupertype() {
        class SimpleSubtype<A>: GenericObject1<GenericInterface1<A>>()
        val subtype = Mirror.reflectClass(SimpleSubtype::class.java)
        val supertype = subtype.superclass!!

        assertSameList(listOf(
                Mirror.reflectClass(GenericInterface1::class.java)
                        .withTypeArguments(subtype.typeParameters[0])
        ), supertype.typeParameters)
    }

    @Test
    @DisplayName("`T1<A> extends T2<T3<A>>; T2<B>; T3<C>`: Specializing T1 for SomeType should return mirror whose " +
            "supertype is specialized for T3<SomeType>")
    fun specializeHandoffWrappedToSupertype() {
        class SimpleSubtype<A>: GenericObject1<GenericInterface1<A>>()
        val subtype = Mirror.reflectClass(SimpleSubtype::class.java).withTypeArguments(Mirror.reflect<String>())
        val supertype = subtype.superclass!!

        assertSameList(listOf(
                Mirror.reflectClass(GenericInterface1::class.java)
                        .withTypeArguments(Mirror.reflect<String>())
        ), supertype.typeParameters)
    }
}