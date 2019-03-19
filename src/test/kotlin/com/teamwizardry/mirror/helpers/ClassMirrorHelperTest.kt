package com.teamwizardry.mirror.helpers

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.testsupport.GenericInterface1
import com.teamwizardry.mirror.testsupport.MirrorTestBase
import com.teamwizardry.mirror.testsupport.Object1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ClassMirrorHelperTest: MirrorTestBase() {
    @Test
    fun findSuperclass_onSameType_shouldReturnItself() {
        class GenericObject<T>: GenericInterface1<T>
        val mirror = Mirror.reflectClass<GenericObject<Object1>>()
        assertEquals(mirror, mirror.findSuperclass(GenericObject::class.java))
    }

    @Test
    fun findSuperclass_onSubclass_shouldReturnSpecializedSuperclass() {
        open class GenericSuper<T>
        class GenericSub<T>: GenericSuper<T>()
        assertEquals(
            Mirror.reflectClass<GenericSuper<Object1>>(),
            Mirror.reflectClass<GenericSub<Object1>>().findSuperclass(GenericSuper::class.java)
        )
    }

    @Test
    fun findSuperclass_onSubclass_shouldReturnSpecializedInterface() {
        class GenericObject<T>: GenericInterface1<T>
        assertEquals(
            Mirror.reflectClass<GenericInterface1<Object1>>(),
            Mirror.reflectClass<GenericObject<Object1>>().findSuperclass(GenericInterface1::class.java)
        )
    }
}
