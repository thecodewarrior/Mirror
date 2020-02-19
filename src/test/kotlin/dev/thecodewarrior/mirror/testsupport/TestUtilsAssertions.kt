package dev.thecodewarrior.mirror.testsupport

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError

class TestUtilsAssertions {
    @Test
    fun assertSameList_withEmptyLists_shouldSucceed() {
        assertSameList(listOf(), listOf())
    }

    @Test
    fun assertSameList_withSingleIdenticalElement_shouldSucceed() {
        val element = Any()
        assertSameList(listOf(element), listOf(element))
    }

    @Test
    fun assertSameList_withSingleEqualElement_shouldFail() {
        val element1 = 1 to 2
        val element2 = 1 to 2
        assertThrows<AssertionFailedError> {
            assertSameList(listOf(element1), listOf(element2))
        }
    }

    @Test
    fun assertSameList_withTwoIdenticalElementsInOrder_shouldSucceed() {
        val element1 = Any()
        val element2 = Any()
        assertSameList(listOf(element1, element2), listOf(element1, element2))
    }

    @Test
    fun assertSameList_withTwoIdenticalElementsOutOfOrder_shouldFail() {
        val element1 = Any()
        val element2 = Any()
        assertThrows<AssertionFailedError> {
            assertSameList(listOf(element1, element2), listOf(element2, element1))
        }
    }

    @Test
    fun assertSameList_withDuplicatedIdenticalElement_shouldFail() {
        val element = Any()
        assertThrows<AssertionFailedError> {
            assertSameList(listOf(element), listOf(element, element))
        }
    }

    // =============================================================================================================

    @Test
    fun assertSameSet_withEmptyLists_shouldSucceed() {
        assertSameSet(listOf(), listOf())
    }

    @Test
    fun assertSameSet_withSingleIdenticalElement_shouldSucceed() {
        val element = Any()
        assertSameSet(listOf(element), listOf(element))
    }

    @Test
    fun assertSameSet_withSingleEqualElement_shouldFail() {
        val element1 = 1 to 2
        val element2 = 1 to 2
        assertThrows<AssertionFailedError> {
            assertSameSet(listOf(element1), listOf(element2))
        }
    }

    @Test
    fun assertSameSet_withTwoIdenticalElementsInOrder_shouldSucceed() {
        val element1 = Any()
        val element2 = Any()
        assertSameSet(listOf(element1, element2), listOf(element1, element2))
    }

    @Test
    fun assertSameSet_withTwoIdenticalElementsOutOfOrder_shouldSucceed() {
        val element1 = Any()
        val element2 = Any()
        assertSameSet(listOf(element1, element2), listOf(element2, element1))
    }

    @Test
    fun assertSameSet_withDuplicatedIdenticalElement_shouldFail() {
        val element = Any()
        assertThrows<AssertionFailedError> {
            assertSameSet(listOf(element), listOf(element, element))
        }
    }

    // =============================================================================================================

    @Test
    fun assertSetEquals_withEmptyLists_shouldSucceed() {
        assertSetEquals(listOf(), listOf())
    }

    @Test
    fun assertSetEquals_withSingleIdenticalElement_shouldSucceed() {
        val element = Any()
        assertSetEquals(listOf(element), listOf(element))
    }

    @Test
    fun assertSetEquals_withSingleEqualElement_shouldFail() {
        val element1 = 1 to 2
        val element2 = 1 to 2
        assertSetEquals(listOf(element1), listOf(element2))
    }

    @Test
    fun assertSetEquals_withTwoIdenticalElementsInOrder_shouldSucceed() {
        assertSetEquals(listOf("a", "b"), listOf("a", "b"))
    }

    @Test
    fun assertSetEquals_withTwoIdenticalElementsOutOfOrder_shouldSucceed() {
        assertSetEquals(listOf("a", "b"), listOf("b", "a"))
    }

    @Test
    fun assertSetEquals_withDuplicatedIdenticalElement_shouldFail() {
        assertThrows<AssertionFailedError> {
            assertSetEquals(listOf("a"), listOf("a", "a"))
        }
    }

    // =============================================================================================================

    @Test
    fun assertInstanceOf_withExactType_shouldSucceed() {
        assertInstanceOf(Object1::class.java, Object1())
    }

    @Test
    fun assertInstanceOf_withSubclass_shouldSucceed() {
        class SubObject1: Object1()
        assertInstanceOf(Object1::class.java, SubObject1())
    }

    @Test
    fun assertInstanceOf_withImplementor_shouldSucceed() {
        class SubObject1: Interface1
        assertInstanceOf(Interface1::class.java, SubObject1())
    }

    @Test
    fun assertInstanceOf_withUnrelated_shouldFail() {
        assertThrows<AssertionFailedError> {
            assertInstanceOf(Object2::class.java, Object1())
        }
    }

    @Test
    fun assertInstanceOf_withSuperclass_shouldFail() {
        class SubObject1: Interface1
        assertThrows<AssertionFailedError> {
            assertInstanceOf(SubObject1::class.java, Object1())
        }
    }

    // =============================================================================================================

    @Test
    fun reifiedAssertInstanceOf_withExactType_shouldSucceed() {
        assertInstanceOf<Object1>(Object1())
    }

    @Test
    fun reifiedAssertInstanceOf_withSubclass_shouldSucceed() {
        class SubObject1: Object1()
        assertInstanceOf<Object1>(SubObject1())
    }

    @Test
    fun reifiedAssertInstanceOf_withImplementor_shouldSucceed() {
        class SubObject1: Interface1
        assertInstanceOf<Interface1>(SubObject1())
    }

    @Test
    fun reifiedAssertInstanceOf_withUnrelated_shouldFail() {
        assertThrows<AssertionFailedError> {
            assertInstanceOf<Object2>(Object1())
        }
    }

    @Test
    fun reifiedAssertInstanceOf_withSuperclass_shouldFail() {
        class SubObject1: Interface1
        assertThrows<AssertionFailedError> {
            assertInstanceOf<SubObject1>(Object1())
        }
    }

    class ExceptionType1: RuntimeException {
        constructor(): super()
        constructor(message: String?): super(message)
        constructor(message: String?, cause: Throwable?): super(message, cause)
        constructor(cause: Throwable?): super(cause)
    }

    class ExceptionType2: RuntimeException {
        constructor(): super()
        constructor(message: String?): super(message)
        constructor(message: String?, cause: Throwable?): super(message, cause)
        constructor(cause: Throwable?): super(cause)
    }

    @Test
    fun assertCause_withCorrectCause_shouldReturnCause() {
        val cause = ExceptionType1()
        val assertResult = RuntimeException(cause).assertCause<ExceptionType1>()
        assertSame(cause, assertResult)
    }

    @Test
    fun assertCause_withIncorrectCause_shouldFail() {
        val e = assertThrows<AssertionFailedError> {
            val cause = ExceptionType1()
            RuntimeException(cause).assertCause<ExceptionType2>("Hi!")
        }
        assertEquals("Hi! ==> Unexpected cause type ==> " +
            "expected: <dev.thecodewarrior.mirror.testsupport.TestUtilsAssertions.ExceptionType2> " +
            "but was: <dev.thecodewarrior.mirror.testsupport.TestUtilsAssertions.ExceptionType1>",
            e.message
        )
    }

    @Test
    fun assertCause_withNoCause_shouldFail() {
        val e = assertThrows<AssertionFailedError> {
            RuntimeException().assertCause<ExceptionType1>("Hi!")
        }
        assertEquals("Hi! ==> Exception has no cause ==> " +
            "expected: <dev.thecodewarrior.mirror.testsupport.TestUtilsAssertions.ExceptionType1> " +
            "but was: <null>",
            e.message
        )
    }

    @Test
    fun assertMessage_withCorrectNullMessage_shouldReturnSameException() {
        val exception = RuntimeException()
        val assertResult = exception.assertMessage(null)
        assertSame(exception, assertResult)
    }

    @Test
    fun assertMessage_withIncorrectNullMessage_shouldReturnSameException() {
        val e = assertThrows<AssertionFailedError> {
            RuntimeException("Hi, I'm a message!").assertMessage(null)
        }
        assertEquals("Unexpected message ==> expected: <null> but was: <Hi, I'm a message!>", e.message)
    }

    @Test
    fun assertMessage_withIncorrectMessage_shouldFail() {
        val e = assertThrows<AssertionFailedError> {
            RuntimeException("Hi, I'm a message!").assertMessage("Hi, I'm wrong!", "**C U S T O M**")
        }
        assertEquals(
            "**C U S T O M** ==> Unexpected message ==> expected: <Hi, I'm wrong!> but was: <Hi, I'm a message!>",
            e.message
        )
    }
}
