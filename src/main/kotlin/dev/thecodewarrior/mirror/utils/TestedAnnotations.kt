package dev.thecodewarrior.mirror.utils

/**
 * "This thing has no tests"
 * @param value Additional notes
 */
internal annotation class Untested(val value: String = "")

/**
 * "This thing has no tests for the negative case (e.g. false, zero, empty list, etc.)"
 * @param value Additional notes
 */
internal annotation class UntestedNegative(val value: String = "")

/**
 * "This thing's failure conditions are untested"
 * @param value Additional notes
 */
internal annotation class UntestedFailure(val value: String = "")

/**
 * "This thing is tested indirectly via another thing (e.g. another method that delegates to it is tested)"
 * @param value Additional notes
 */
internal annotation class IndirectTests(val value: String = "")
