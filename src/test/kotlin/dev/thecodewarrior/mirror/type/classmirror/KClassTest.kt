@file:Suppress("ClassName", "PropertyName")

package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.member.Modifier
import dev.thecodewarrior.mirror.testsupport.*
import dev.thecodewarrior.mirror.utils.Untested
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.reflect.Method
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.jvmErasure

/**
 * Currently just tests for certain KClass behavioral assumptions, but I may eventually create a `KClassMirror` type.
 */
@Suppress("LocalVariableName")
internal class KClassTest: MTest() {

    @Test
    fun `declaredMemberProperties should include fields as 'KMutableProperty1's`() {
        val A by sources.add("A", "@rt(FIELD) @interface A {}")
        val X by sources.add("X", "class X { @A int field; }")
        sources.compile()
        val members = X.kotlin.declaredMembers.toList()
        assertEquals(1, members.size)
        val property = assertInstanceOf<KMutableProperty1<Any, Any>>(members[0])
        assertEquals("field", property.name)
        assertEquals(_int, property.returnType.jvmErasure.java)
    }

    @Test
    fun `declaredMemberProperties should include final fields as 'KProperty1's`() {
        val X by sources.add("X", "class X { final int field = 0; }")
        sources.compile()
        val members = X.kotlin.declaredMembers.toList()
        assertEquals(1, members.size)
        val property = assertInstanceOf<KProperty1<Any, Any>>(members[0])
        assertNotInstanceOf<KMutableProperty1<*, *>>(property);
        assertEquals("field", property.name)
        assertEquals(_int, property.returnType.jvmErasure.java)
    }

}
