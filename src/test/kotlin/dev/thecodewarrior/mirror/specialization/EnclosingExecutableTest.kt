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

class EnclosingExecutableTest: MirrorTestBase() {
    @Test
    @DisplayName("Getting the enclosing method of a root class should return null")
    fun enclosingExecutable_ofRootClass_shouldReturnNull() {
        val clazz = Mirror.reflectClass(OuterClass1::class.java)
        assertNull(clazz.enclosingExecutable)
    }

    @Test
    @DisplayName("Getting the enclosing executable of an inner class should return null")
    fun enclosingExecutable_ofInnerClass_shouldReturnNull() {
        val clazz = Mirror.reflectClass(OuterClass1.InnerClass::class.java)
        assertNull(clazz.enclosingExecutable)
    }

    @Test
    @DisplayName("Getting the enclosing executable of a local class should return the enclosing method")
    fun enclosingExecutable_ofLocalClass_shouldReturnEnclosingMethod() {
        class LocalClass
        val clazz = Mirror.reflectClass(LocalClass::class.java)
        val thisMethod = javaClass.getMethod("enclosingExecutable_ofLocalClass_shouldReturnEnclosingMethod")
        assertSame(Mirror.reflect(thisMethod), clazz.enclosingExecutable)
    }

    @Test
    @DisplayName("Getting the enclosing method of a local class in a constructor should return the enclosing constructor")
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
    @DisplayName("The type of local class members should correctly reference the enclosing method's type parameters by identity")
    fun <T: Any> localClassUsingMethodTypeParameter() {
        class LocalClass {
            lateinit var foo: T
        }
        val clazz = Mirror.reflectClass<LocalClass>()
        val thisMethod = Mirror.reflectClass<EnclosingExecutableTest>().methods("localClassUsingMethodTypeParameter")[0]
        assertSame(thisMethod.typeParameters[0], clazz.field("foo")?.type)
    }

    @Test
    @DisplayName("The type of local class members should be specialized when specializing a local class in a generic method")
    fun <T: Any> localClassUsingSpecializedMethodTypeParameter() {
        class LocalClass {
            lateinit var foo: T
        }
        val clazz = Mirror.reflectClass<LocalClass>()
        val thisMethod = Mirror.reflectClass<EnclosingExecutableTest>().methods("localClassUsingSpecializedMethodTypeParameter")[0]
        assertSame(Mirror.reflect<String>(), clazz.withEnclosingExecutable(thisMethod.withTypeParameters(Mirror.reflect<String>())).field("foo")?.type)
    }

    @Test
    @DisplayName("Specializing a class with the wrong enclosing executable should throw")
    fun specializingWithWrongExecutable() {
        class LocalClass
        val clazz = Mirror.reflectClass<LocalClass>()
        val thisMethod = Mirror.reflectClass<EnclosingExecutableTest>().methods("toString")[0]
        assertThrows<InvalidSpecializationException> {
            clazz.withEnclosingExecutable(thisMethod)
        }
    }

    @Test
    @DisplayName("Specializing a root class for an enclosing executable should throw")
    fun specializingRootClass() {
        val clazz = Mirror.reflectClass<Object1>()
        val thisMethod = Mirror.reflectClass<EnclosingExecutableTest>().methods("toString")[0]
        assertThrows<InvalidSpecializationException> {
            clazz.withEnclosingExecutable(thisMethod)
        }
    }

    @Test
    @DisplayName("Specializing a root class for an enclosing executable should throw")
    fun specializingRootClassForNull() {
        val clazz = Mirror.reflectClass<Object1>()
        assertSame(clazz, clazz.withEnclosingExecutable(null))
    }
}