package dev.thecodewarrior.mirror.testsupport

import org.junit.jupiter.api.fail
import org.opentest4j.AssertionFailedError

/**
 * _Asserts_ that [expected] and [actual] have the same length and that the elements of each refer to the same objects.
 */
fun assertSameList(expected: List<Any?>, actual: List<Any?>) {
    if(expected.size != actual.size || expected.zip(actual).any { it.first !== it.second })
        throw AssertionFailedError("",
            expected.map { it.toStringWithIdentity() },
            actual.map { it.toStringWithIdentity() }
        )
}

/**
 * _Asserts_ that [expected] and [actual] have the same size and that each element in [expected] has exactly one
 * element in [actual] that is equal *by identity.* (multiple copies in one will require the same number of
 * copies in the other)
 *
 * To use [equals], use [assertSetEquals]
 */
fun assertSameSet(expected: List<Any?>, actual: Collection<Any?>) {
    val remainingExpected = expected.toMutableList()
    val remainingActual = actual.toMutableList()

    val orderedExpected = mutableListOf<Any?>()
    val orderedActual = mutableListOf<Any?>()

    remainingExpected.removeIf { expect ->
        val i = remainingActual.indexOfFirst { it === expect }
        if(i != -1) {
            orderedActual.add(remainingActual.removeAt(i))
            orderedExpected.add(expect)
            true
        } else {
            false
        }
    }

    orderedExpected.addAll(remainingExpected)
    orderedActual.addAll(remainingActual)

    if(expected.size != actual.size || orderedExpected.zip(orderedActual).any { it.first !== it.second })
        throw AssertionFailedError("",
            orderedExpected.map { it.toStringWithIdentity() },
            orderedActual.map { it.toStringWithIdentity() }
        )
}

/**
 * _Asserts_ that [expected] and [actual] have the same size and that each element in [expected] has exactly one
 * equal element in [actual] (multiple copies in one will require the same number of copies in the other)
 *
 * To match by identity, use [assertSameSet]
 */
fun assertSetEquals(expected: List<Any?>, actual: List<Any?>) {
    val remainingExpected = expected.toMutableList()
    val remainingActual = actual.toMutableList()

    val orderedExpected = mutableListOf<Any?>()
    val orderedActual = mutableListOf<Any?>()

    remainingExpected.removeIf { expect ->
        val i = remainingActual.indexOfFirst { it == expect }
        if(i != -1) {
            orderedActual.add(remainingActual.removeAt(i))
            orderedExpected.add(expect)
            true
        } else {
            false
        }
    }

    orderedExpected.addAll(remainingExpected)
    orderedActual.addAll(remainingActual)

    if(expected.size != actual.size || orderedExpected.zip(orderedActual).any { it.first != it.second })
        throw AssertionFailedError("",
            orderedExpected,
            orderedActual
        )
}

fun <T: Any?> assertInstanceOf(expected: Class<T>, actual: Any?): T {
    if(!expected.isInstance(actual))
        throw AssertionFailedError("",
            expected.canonicalName,
            actual.toStringWithIdentity()
        )
    @Suppress("UNCHECKED_CAST")
    return actual as T
}

inline fun <reified T: Any?> assertInstanceOf(actual: Any?): T {
    return assertInstanceOf(T::class.java, actual)
}

fun assertNotInstanceOf(notExpected: Class<*>, actual: Any?) {
    if(notExpected.isInstance(actual))
        throw AssertionFailedError("",
            "Not " + notExpected.canonicalName,
            actual.toStringWithIdentity()
        )
}

inline fun <reified T> assertNotInstanceOf(actual: Any?) {
    assertNotInstanceOf(T::class.java, actual)
}

fun Any?.toStringWithIdentity(): String {
    if(this == null) return "null"

    val hashCode = System.identityHashCode(this).toString(16)
    val value = this.toString()
    if (value.endsWith(hashCode))
        return value
    else
        return "$value@$hashCode"
}


/**
 * Asserts that this exception has a cause of the specified type, then returns that cause.
 */
inline fun <reified T : Throwable> Throwable.assertCause(message: String? = null): T = this.assertCause { message }

/**
 * Asserts that this exception has a cause of the specified type, then returns that cause.
 */
inline fun <reified T : Throwable> Throwable.assertCause(message: () -> String?): T {
    return when(val cause = this.cause) {
        null -> fail(AssertionUtils.format(message(), T::class.java, null, "Exception has no cause"))
        is T -> cause
        else -> throw AssertionFailedError(
            AssertionUtils.format(message(), T::class.java, cause.javaClass, "Unexpected cause type"),
            cause
        )
    }
}

/**
 * Asserts that this exception has the passed message, then returns this exception.
 */
inline fun <reified T : Throwable> T.assertMessage(expected: String?, assertionMessage: String? = null): T =
    this.assertMessage(expected) { assertionMessage }

/**
 * Asserts that this exception has the passed message, then returns this exception.
 */
inline fun <reified T : Throwable> T.assertMessage(expected: String?, assertionMessage: () -> String?): T {
    if(this.message == expected) {
        return this
    }
    throw AssertionFailedError(
        AssertionUtils.format(assertionMessage(), expected, message, "Unexpected message"),
        this
    )
}

val nop: Nothing
    get() = throw NotImplementedError("nop")