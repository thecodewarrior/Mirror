package dev.thecodewarrior.mirror.specialization

import dev.thecodewarrior.mirror.InvalidSpecializationException
import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.OuterClass1
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EnclosingExecutableTest: MirrorTestBase() {
    @Test
    fun enclosingExecutable_ofRootClass_shouldReturnNull() {
        val clazz = Mirror.reflectClass(OuterClass1::class.java)
        assertNull(clazz.enclosingExecutable)
    }

    @Test
    fun enclosingExecutable_ofInnerClass_shouldReturnNull() {
        val clazz = Mirror.reflectClass(OuterClass1.InnerClass::class.java)
        assertNull(clazz.enclosingExecutable)
    }

    @Test
    fun enclosingExecutable_ofLocalClass_shouldReturnEnclosingMethod() {
        class LocalClass
        val clazz = Mirror.reflectClass(LocalClass::class.java)
        val thisMethod = javaClass.getMethod("enclosingExecutable_ofLocalClass_shouldReturnEnclosingMethod")
        assertSame(Mirror.reflect(thisMethod), clazz.enclosingExecutable)
    }

    @Test
    fun enclosingExecutable_ofConstructorLocalClass_shouldReturnEnclosingConstructor() {
        class ConstructorHolder {
            init {
                class LocalClass
                val clazz = Mirror.reflectClass(LocalClass::class.java)
                val thisConstructor = javaClass.getConstructor()
                assertSame(Mirror.reflect(thisConstructor), clazz.enclosingExecutable)
            }
        }
        ConstructorHolder()
    }

    @Test
    fun <T: Any> typeParameters_inLocalClass_shouldReferenceMethodTypeParameters() {
        class LocalClass {
            lateinit var foo: T
        }
        val clazz = Mirror.reflectClass<LocalClass>()
        val thisMethod = Mirror.reflectClass<EnclosingExecutableTest>().findPublicMethods("typeParameters_inLocalClass_shouldReferenceMethodTypeParameters")[0]
        assertSame(thisMethod.typeParameters[0], clazz.findPublicField("foo")?.type)
    }

    @Test
    fun <T: Any> memberTypes_ofLocalClass_shouldReferenceMethodTypeParameters() {
        class LocalClass {
            lateinit var foo: T
        }
        val clazz = Mirror.reflectClass<LocalClass>()
        val thisMethod = Mirror.reflectClass<EnclosingExecutableTest>().findPublicMethods("memberTypes_ofLocalClass_shouldReferenceMethodTypeParameters")[0]
        assertSame(Mirror.reflect<String>(), clazz.withEnclosingExecutable(thisMethod.withTypeParameters(Mirror.reflect<String>())).findPublicField("foo")?.type)
    }

    @Test
    fun specializeClass_withWrongEnclosingExecutable_shouldThrow() {
        class LocalClass
        val clazz = Mirror.reflectClass<LocalClass>()
        val thisMethod = Mirror.reflectClass<EnclosingExecutableTest>().findPublicMethods("toString")[0]
        assertThrows<InvalidSpecializationException> {
            clazz.withEnclosingExecutable(thisMethod)
        }
    }

    @Test
    fun specializeRootClass_withExecutable_shouldThrow() {
        val clazz = Mirror.reflectClass<Object1>()
        val thisMethod = Mirror.reflectClass<EnclosingExecutableTest>().findPublicMethods("toString")[0]
        assertThrows<InvalidSpecializationException> {
            clazz.withEnclosingExecutable(thisMethod)
        }
    }

    @Test
    fun specializingRootClass_withNullExecutable_shouldReturnSameClass() {
        val clazz = Mirror.reflectClass<Object1>()
        assertSame(clazz, clazz.withEnclosingExecutable(null))
    }
}