package dev.thecodewarrior.mirror.helpers

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.GenericInterface1
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ClassMirrorHelperTest: MTest() {
    val X by sources.add("X", "class X {}")
    val Generic by sources.add("Generic", "class Generic<T> {}")
    val IGeneric by sources.add("IGeneric", "interface IGeneric<T> {}")
    val GenericSub by sources.add("GenericSub", "class GenericSub<T> extends Generic<X> implements IGeneric<T> {}")
    val types = sources.types {
        +"Generic<X>"
        +"IGeneric<X>"
        +"GenericSub<X>"
    }

    @Test
    fun findSuperclass_onSameType_shouldReturnItself() {
        val mirror = Mirror.reflectClass(types["Generic<X>"])
        assertEquals(mirror, mirror.findSuperclass(Generic))
    }

    @Test
    fun findSuperclass_onSubclass_shouldReturnSpecializedSuperclass() {
        assertEquals(
            Mirror.reflectClass(types["Generic<X>"]),
            Mirror.reflectClass(types["GenericSub<X>"]).findSuperclass(Generic)
        )
    }

    @Test
    fun findSuperclass_onSubclass_shouldReturnSpecializedInterface() {
        assertEquals(
            Mirror.reflectClass(types["IGeneric<X>"]),
            Mirror.reflectClass(types["GenericSub<X>"]).findSuperclass(IGeneric)
        )
    }

    @Test
    fun findSuperclass_withUnrelatedType_shouldThrow() {
        assertNull(Mirror.reflectClass(types["Generic<X>"]).findSuperclass(List::class.java))
    }
}
