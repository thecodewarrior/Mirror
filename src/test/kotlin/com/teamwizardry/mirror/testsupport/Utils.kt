package com.teamwizardry.mirror.testsupport

import org.opentest4j.AssertionFailedError

fun assertSameList(expected: List<Any?>, actual: List<Any?>) {
    if(expected.size != actual.size || expected.zip(actual).any { it.first !== it.second })
        throw AssertionFailedError("",
            expected.map { it.toStringWithIdentity() },
            actual.map { it.toStringWithIdentity() }
        )
}

fun assertSameSet(expected: List<Any?>, actual: List<Any?>) {
    val sortedExpected = expected.sortedBy { System.identityHashCode(it) }
    val sortedActual = actual.sortedBy { System.identityHashCode(it) }
    if(sortedExpected.size != sortedActual.size || sortedExpected.zip(sortedActual).any { it.first !== it.second })
        throw AssertionFailedError("",
            sortedExpected.map { it.toStringWithIdentity() },
            sortedActual.map { it.toStringWithIdentity() }
        )
}

private fun Any?.toStringWithIdentity(): String {
    if(this == null) return "null"

    val hashCode = System.identityHashCode(this).toString(16)
    val value = this.toString()
    if (value.endsWith(hashCode))
        return value
    else
        return "$value@$hashCode"
}