package dev.thecodewarrior.mirror.specialization

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.GenericInterface1
import dev.thecodewarrior.mirror.testsupport.GenericInterface2
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.assertSameList
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class SuperInterfaceSpecializationTest: MirrorTestBase() {
    @Test
    @DisplayName("`T1 implements T2<SomeType>; T2<A>`: Reflecting T1 should return a mirror with T2 " +
            "specialized for SomeType as an interface")
    fun explicitInterfaceSpecialization() {
        class SimpleClass: GenericInterface1<String>
        val simpleType = Mirror.reflectClass(SimpleClass::class.java)
        val interfaces = simpleType.interfaces

        val generic = Mirror.reflectClass(GenericInterface1::class.java)
        assertSameList(listOf(generic.withTypeArguments(Mirror.reflect<String>())), interfaces)
    }

    @Test
    @DisplayName("`T1<A> implements T2<A>; T2<B>`: Reflecting T1 should return a mirror with T2 " +
            "specialized for A as an interface")
    fun reflectedHandoffToInterface() {
        class SimpleClass<A>: GenericInterface1<A>
        val simpleType = Mirror.reflectClass(SimpleClass::class.java)
        val genericInterface = simpleType.interfaces[0]
        val simpleTypeVariable = simpleType.typeParameters[0]

        assertSameList(listOf(simpleTypeVariable), genericInterface.typeParameters)
    }

    @Test
    @DisplayName("`T1<A> implements T2<A>; T2<B>`: Specializing T1 for SomeType should return a mirror with " +
            "T2 specialized for SomeType as an interface")
    fun specializedHandoffToInterface() {
        class SimpleClass<A>: GenericInterface1<A>
        val specialized = Mirror.reflectClass(SimpleClass::class.java).withTypeArguments(Mirror.reflect<String>())
        val superInterface = specialized.interfaces[0]

        assertSameList(listOf(Mirror.reflect<String>()), superInterface.typeParameters)
    }

    @Test
    @DisplayName("`T1<A> implements T2<T3<A>>; T2<B>; T3<C>`: Reflecting T1 should return mirror with " +
            "T2 specialized for T3<A> as an interface")
    fun reflectedHandoffWrappedToInterface() {
        class SimpleClass<A>: GenericInterface1<GenericInterface2<A>>
        val simpleType = Mirror.reflectClass(SimpleClass::class.java)
        val superInterface = simpleType.interfaces[0]

        assertSameList(listOf(
                Mirror.reflectClass(GenericInterface2::class.java)
                        .withTypeArguments(simpleType.typeParameters[0])
        ), superInterface.typeParameters)
    }

    @Test
    @DisplayName("`T1<A> implements T2<T3<A>>; T2<B>; T3<C>`: Specializing T1 for SomeType should return mirror with " +
            "T2 specialized for T3<SomeType> as an interface")
    fun specializeHandoffWrappedToSupertype() {
        class SimpleClass<A>: GenericInterface1<GenericInterface2<A>>
        val simpleType = Mirror.reflectClass(SimpleClass::class.java).withTypeArguments(Mirror.reflect<String>())
        val superInterface = simpleType.interfaces[0]

        assertSameList(listOf(
                Mirror.reflectClass(GenericInterface2::class.java)
                        .withTypeArguments(Mirror.reflect<String>())
        ), superInterface.typeParameters)
    }
}