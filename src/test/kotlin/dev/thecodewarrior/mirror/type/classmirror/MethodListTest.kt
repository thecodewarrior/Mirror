package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.NoSuchMirrorException
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.type.TypeMirror
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MethodListTest: MTest() {
    @Test
    fun `calling findAll(name) multiple times should return a cached list`() {
        val X by sources.add("X", "class X { void method1() {}}")
        sources.compile()
        assertSame(
            Mirror.reflectClass(X).declaredMethods.findAll("method1"),
            Mirror.reflectClass(X).declaredMethods.findAll("method1")
        )
    }

    @Test
    fun `calling findAll(name) with no matching method should return an empty list`() {
        val X by sources.add("X", "class X { void method2() {}}")
        sources.compile()
        assertEquals(
            emptyList(),
            Mirror.reflectClass(X).declaredMethods.findAll("method1")
        )
    }

    @Test
    fun `calling findAll(name) with one matching method should return a list with a single mirror`() {
        val X by sources.add("X", "class X { void method1() {} void method2() {}}")
        sources.compile()
        assertEquals(
            listOf(Mirror.reflect(X._m("method1"))),
            Mirror.reflectClass(X).declaredMethods.findAll("method1")
        )
    }

    @Test
    fun `calling findAll(name) with multiple matching methods should return a list with all the matching mirrors`() {
        val X by sources.add("X", "class X { void method1() {} void method1(int foo) {} void method2() {}}")
        sources.compile()
        assertEquals(
            listOf(Mirror.reflect(X._m("method1")), Mirror.reflect(X._m("method1", Mirror.types.int.java))),
            Mirror.reflectClass(X).declaredMethods.findAll("method1")
        )
    }

    @Test
    fun `calling find(name, params) with no params and a matching method should return the matching mirror`() {
        val X by sources.add("X", "class X { void method1() {} void method1(int foo) {} }")
        sources.compile()
        assertEquals(
            Mirror.reflect(X._m("method1")),
            Mirror.reflectClass(X).declaredMethods.find("method1")
        )
    }

    @Test
    fun `calling find(name, params) with no params and no matching method should return null`() {
        val X by sources.add("X", "class X { void method1(int foo) {} }")
        sources.compile()
        assertNull(Mirror.reflectClass(X).declaredMethods.find("method1"))
    }

    @Test
    fun `calling find(name, params) with params and a matching methods should return the matching mirror`() {
        val X by sources.add("X", "class X { void method1() {} void method1(int foo) {} }")
        sources.compile()
        assertEquals(
            Mirror.reflect(X._m("method1", _int)),
            Mirror.reflectClass(X).declaredMethods.find("method1", Mirror.types.int)
        )
    }

    @Test
    fun `calling find(name, params) with params and no matching methods should return null`() {
        val X by sources.add("X", "class X { void method1() {} void method1(float foo) {} }")
        sources.compile()
        assertNull(Mirror.reflectClass(X).declaredMethods.find("method1", Mirror.types.int))
    }

    @Test
    fun `calling find(name, params) with erased generic params should return null`() {
        val X by sources.add("X", "class X { void method1() {} void method1(List<Y> foo) {} }")
        val Y by sources.add("Y", "class Y {}")
        sources.compile()
        assertNull(Mirror.reflectClass(X).declaredMethods.find("method1", Mirror.reflectClass<List<*>>().raw))
    }

    @Test
    fun `calling find(name, params) with a specialized class, no params, and a matching specialized method should return the matching specialized mirror`() {
        val X by sources.add("X", "class X<T> { void method1() {} void method1(T foo) {} }")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"X<Y>"
        }
        sources.compile()
        assertEquals(
            Mirror.reflectClass(types["X<Y>"]).getMethod(X._m("method1")),
            Mirror.reflectClass(types["X<Y>"]).declaredMethods.find("method1")
        )
    }

    @Test
    fun `calling find(name, params) with a specialized class, no params, and no matching specialized method should return null`() {
        val X by sources.add("X", "class X<T> { void method1(T foo) {} }")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"X<Y>"
        }
        sources.compile()
        assertNull(Mirror.reflectClass(types["X<Y>"]).declaredMethods.find("method1"))
    }

    @Test
    fun `calling find(name, params) with a specialized class, params, and a matching specialized method should return the matching specialized mirror`() {
        val X by sources.add("X", "class X<T> { void method1() {} void method1(T foo) {} }")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"X<Y>"
        }
        sources.compile()
        assertEquals(
            Mirror.reflectClass(types["X<Y>"]).getMethod(X._m("method1", _object)),
            Mirror.reflectClass(types["X<Y>"]).declaredMethods.find("method1", Mirror.reflect(Y))
        )
    }

    @Test
    fun `calling find(name, params) with a specialized class, params, and a method that matches raw but not specialized should return null`() {
        val X by sources.add("X", "class X<T> { void method1() {} void method1(T foo) {} }")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"X<Y>"
        }
        sources.compile()
        val rawType = Mirror.reflectClass(X)
        assertNotNull(rawType.declaredMethods.find("method1", rawType.typeParameters.single()))
        val type = Mirror.reflectClass(types["X<Y>"])
        assertNull(type.declaredMethods.find("method1", rawType.typeParameters.single()))
    }

    @Test
    fun `calling find(name, params) with a specialized class and erased params should return null`() {
        val X by sources.add("X", "class X<T> { void method1() {} void method1(T foo) {} }")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"X<Y>"
        }
        sources.compile()
        val type = Mirror.reflectClass(types["X<Y>"])
        assertNull(type.declaredMethods.find("method1", Mirror.types.any))
    }

    @Test
    fun `calling findRaw(name, params) with a specialized class, no params, and a matching raw method should return the matching specialized mirror`() {
        val X by sources.add("X", "class X<T> { void method1() {} void method1(T foo) {} }")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"X<Y>"
        }
        sources.compile()
        assertEquals(
            Mirror.reflectClass(types["X<Y>"]).getMethod(X._m("method1")),
            Mirror.reflectClass(types["X<Y>"]).declaredMethods.findRaw("method1")
        )
    }

    @Test
    fun `calling findRaw(name, params) with a specialized class, no params, and no matching raw method should return null`() {
        val X by sources.add("X", "class X<T> { void method1(T foo) {} }")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"X<Y>"
        }
        sources.compile()
        assertNull(Mirror.reflectClass(types["X<Y>"]).declaredMethods.findRaw("method1"))
    }

    @Test
    fun `calling findRaw(name, params) with a specialized class, erased params, and a matching raw method should return the matching specialized mirrors`() {
        val X by sources.add("X", "class X<T> { void method1() {} void method1(T foo) {} }")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"X<Y>"
        }
        sources.compile()
        assertEquals(
            Mirror.reflectClass(types["X<Y>"]).getMethod(X._m("method1", _object)),
            Mirror.reflectClass(types["X<Y>"]).declaredMethods.findRaw("method1", _object)
        )
    }

    @Test
    fun `calling findRaw(name, params) with a specialized class, specialized params, and a matching specialized method that does not match when raw should return null`() {
        val X by sources.add("X", "class X<T> { void method1() {} void method1(T foo) {} }")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"X<Y>"
        }
        sources.compile()
        val type = Mirror.reflectClass(types["X<Y>"])
        assertNotNull(type.declaredMethods.find("method1", Mirror.reflect(Y)))
        assertNull(type.declaredMethods.findRaw("method1", Y))
    }

    @Test
    fun `calling get(name, params) should delegate directly to find(name, params)`() {
        val list = spyk(Mirror.types.any.declaredMethods)

        list.get("equals", Mirror.types.any)

        verify(exactly = 1) { list.get("equals", Mirror.types.any) }
        verify(exactly = 1) { list.find("equals", Mirror.types.any) }
        confirmVerified(list)
    }

    @Test
    fun `calling get(name, params) should delegate directly to find(name, params) and throw when it returns null`() {
        val list = spyk(Mirror.types.any.declaredMethods)
        every { list.find("equals", Mirror.types.any) } returns null

        assertThrows<NoSuchMirrorException> {
            list.get("equals", Mirror.types.any)
        }
    }

    @Test
    fun `calling getRaw(name, params) should delegate directly to findRaw(name, params)`() {
        val list = spyk(Mirror.types.any.declaredMethods)

        list.getRaw("equals", _object)

        verify(exactly = 1) { list.getRaw("equals", _object) }
        verify(exactly = 1) { list.findRaw("equals", _object) }
        confirmVerified(list)
    }

    @Test
    fun `calling getRaw(name, params) should delegate directly to findRaw(name, params) and throw when it returns null`() {
        val list = spyk(Mirror.types.any.declaredMethods)
        every { list.findRaw("equals", _object) } returns null

        assertThrows<NoSuchMirrorException> {
            list.getRaw("equals", _object)
        }
    }
}
