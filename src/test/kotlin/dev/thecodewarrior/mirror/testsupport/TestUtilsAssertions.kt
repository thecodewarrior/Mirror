package dev.thecodewarrior.mirror.testsupport

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError

class TestUtilsAssertions {
    @Test
    @DisplayName("assertSameList with empty lists should succeed")
    fun assertSameList_withEmptyLists_shouldSucceed() {
        assertSameList(listOf(), listOf())
    }

    @Test
    @DisplayName("assertSameList with lists containing a single identical element should succeed")
    fun assertSameList_withSingleIdenticalElement_shouldSucceed() {
        val element = Any()
        assertSameList(listOf(element), listOf(element))
    }

    @Test
    @DisplayName("assertSameList with lists containing a single equal but not identical elements should fail")
    fun assertSameList_withSingleEqualElement_shouldFail() {
        val element1 = 1 to 2
        val element2 = 1 to 2
        assertThrows<AssertionFailedError> {
            assertSameList(listOf(element1), listOf(element2))
        }
    }

    @Test
    @DisplayName("assertSameList with lists containing two identical elements in the same order should fail")
    fun assertSameList_withTwoIdenticalElementsInOrder_shouldSucceed() {
        val element1 = Any()
        val element2 = Any()
        assertSameList(listOf(element1, element2), listOf(element1, element2))
    }

    @Test
    @DisplayName("assertSameList with lists containing two identical elements in different orders should fail")
    fun assertSameList_withTwoIdenticalElementsOutOfOrder_shouldFail() {
        val element1 = Any()
        val element2 = Any()
        assertThrows<AssertionFailedError> {
            assertSameList(listOf(element1, element2), listOf(element2, element1))
        }
    }

    @Test
    @DisplayName("assertSameList with lists containing a single identical elements but duplicated in one should fail")
    fun assertSameList_withDuplicatedIdenticalElement_shouldFail() {
        val element = Any()
        assertThrows<AssertionFailedError> {
            assertSameList(listOf(element), listOf(element, element))
        }
    }

    // =============================================================================================================

    @Test
    @DisplayName("assertSameSet with empty lists should succeed")
    fun assertSameSet_withEmptyLists_shouldSucceed() {
        assertSameSet(listOf(), listOf())
    }

    @Test
    @DisplayName("assertSameSet with lists containing a single identical element should succeed")
    fun assertSameSet_withSingleIdenticalElement_shouldSucceed() {
        val element = Any()
        assertSameSet(listOf(element), listOf(element))
    }

    @Test
    @DisplayName("assertSameSet with lists containing a single equal but not identical elements should fail")
    fun assertSameSet_withSingleEqualElement_shouldFail() {
        val element1 = 1 to 2
        val element2 = 1 to 2
        assertThrows<AssertionFailedError> {
            assertSameSet(listOf(element1), listOf(element2))
        }
    }

    @Test
    @DisplayName("assertSameSet with lists containing two identical elements in the same order should succeed")
    fun assertSameSet_withTwoIdenticalElementsInOrder_shouldSucceed() {
        val element1 = Any()
        val element2 = Any()
        assertSameSet(listOf(element1, element2), listOf(element1, element2))
    }

    @Test
    @DisplayName("assertSameSet with lists containing two identical elements in different orders should suceed")
    fun assertSameSet_withTwoIdenticalElementsOutOfOrder_shouldSucceed() {
        val element1 = Any()
        val element2 = Any()
        assertSameSet(listOf(element1, element2), listOf(element2, element1))
    }

    @Test
    @DisplayName("assertSameSet with lists containing a single identical elements but duplicated in one should fail")
    fun assertSameSet_withDuplicatedIdenticalElement_shouldFail() {
        val element = Any()
        assertThrows<AssertionFailedError> {
            assertSameSet(listOf(element), listOf(element, element))
        }
    }

    // =============================================================================================================

    @Test
    @DisplayName("assertSetEquals with empty lists should succeed")
    fun assertSetEquals_withEmptyLists_shouldSucceed() {
        assertSetEquals(listOf(), listOf())
    }

    @Test
    @DisplayName("assertSetEquals with lists containing a single identical element should succeed")
    fun assertSetEquals_withSingleIdenticalElement_shouldSucceed() {
        val element = Any()
        assertSetEquals(listOf(element), listOf(element))
    }

    @Test
    @DisplayName("assertSetEquals with lists containing a single equal but not identical elements should succeed")
    fun assertSetEquals_withSingleEqualElement_shouldFail() {
        val element1 = 1 to 2
        val element2 = 1 to 2
        assertSetEquals(listOf(element1), listOf(element2))
    }

    @Test
    @DisplayName("assertSetEquals with lists containing two equal elements in the same order should succeed")
    fun assertSetEquals_withTwoIdenticalElementsInOrder_shouldSucceed() {
        assertSetEquals(listOf("a", "b"), listOf("a", "b"))
    }

    @Test
    @DisplayName("assertSetEquals with lists containing two equal elements in different orders should succeed")
    fun assertSetEquals_withTwoIdenticalElementsOutOfOrder_shouldSucceed() {
        assertSetEquals(listOf("a", "b"), listOf("b", "a"))
    }

    @Test
    @DisplayName("assertSetEquals with lists containing a equal elements with and one duplicated should fail")
    fun assertSetEquals_withDuplicatedIdenticalElement_shouldFail() {
        assertThrows<AssertionFailedError> {
            assertSetEquals(listOf("a"), listOf("a", "a"))
        }
    }

    // =============================================================================================================

    @Test
    @DisplayName("assertInstanceOf with instance of the exact type should succeed")
    fun assertInstanceOf_withExactType_shouldSucceed() {
        assertInstanceOf(Object1::class.java, Object1())
    }

    @Test
    @DisplayName("assertInstanceOf with subclass should succeed")
    fun assertInstanceOf_withSubclass_shouldSucceed() {
        class SubObject1: Object1()
        assertInstanceOf(Object1::class.java, SubObject1())
    }

    @Test
    @DisplayName("assertInstanceOf with implementor should succeed")
    fun assertInstanceOf_withImplementor_shouldSucceed() {
        class SubObject1: Interface1
        assertInstanceOf(Interface1::class.java, SubObject1())
    }

    @Test
    @DisplayName("assertInstanceOf with unrelated object should fail")
    fun assertInstanceOf_withUnrelated_shouldFail() {
        assertThrows<AssertionFailedError> {
            assertInstanceOf(Object2::class.java, Object1())
        }
    }

    @Test
    @DisplayName("assertInstanceOf with an instance of a superclass should fail")
    fun assertInstanceOf_withSuperclass_shouldFail() {
        class SubObject1: Interface1
        assertThrows<AssertionFailedError> {
            assertInstanceOf(SubObject1::class.java, Object1())
        }
    }

    // =============================================================================================================

    @Test
    @DisplayName("reified assertInstanceOf with instance of the exact type should succeed")
    fun reifiedAssertInstanceOf_withExactType_shouldSucceed() {
        assertInstanceOf<Object1>(Object1())
    }

    @Test
    @DisplayName("reified assertInstanceOf with subclass should succeed")
    fun reifiedAssertInstanceOf_withSubclass_shouldSucceed() {
        class SubObject1: Object1()
        assertInstanceOf<Object1>(SubObject1())
    }

    @Test
    @DisplayName("reified assertInstanceOf with implementor should succeed")
    fun reifiedAssertInstanceOf_withImplementor_shouldSucceed() {
        class SubObject1: Interface1
        assertInstanceOf<Interface1>(SubObject1())
    }

    @Test
    @DisplayName("reified assertInstanceOf with unrelated object should fail")
    fun reifiedAssertInstanceOf_withUnrelated_shouldFail() {
        assertThrows<AssertionFailedError> {
            assertInstanceOf<Object2>(Object1())
        }
    }

    @Test
    @DisplayName("reified assertInstanceOf with an instance of a superclass should fail")
    fun reifiedAssertInstanceOf_withSuperclass_shouldFail() {
        class SubObject1: Interface1
        assertThrows<AssertionFailedError> {
            assertInstanceOf<SubObject1>(Object1())
        }
    }
}