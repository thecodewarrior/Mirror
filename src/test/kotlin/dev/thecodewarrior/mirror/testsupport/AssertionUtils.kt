package dev.thecodewarrior.mirror.testsupport

/**
 * Stuff from [org.junit.jupiter.api.AssertionUtils] that I need
 */
object AssertionUtils {

    fun format(customMessage: String?, expected: Any?, actual: Any?, message: String?): String {
        return buildPrefix(customMessage) + format(expected, actual, message)
    }

    fun format(expected: Any?, actual: Any?, message: String?): String {
        return buildPrefix(message) + formatValues(expected, actual)
    }

    fun formatValues(expected: Any?, actual: Any?): String {
        val expectedString = toAssertionString(expected)
        val actualString = toAssertionString(actual)
        return if (expectedString == actualString) {
            String.format("expected: %s but was: %s", formatClassAndValue(expected, expectedString),
                formatClassAndValue(actual, actualString))
        } else String.format("expected: <%s> but was: <%s>", expectedString, actualString)
    }

    fun formatClassAndValue(value: Any?, valueString: String): String? {
        val classAndHash = getClassName(value) + toHash(value)
        // if it's a class, there's no need to repeat the class name contained in the valueString.
        return if (value is Class<*>) "<$classAndHash>" else "$classAndHash<$valueString>"
    }

    fun getClassName(obj: Any?): String? {
        return when(obj) {
            null -> "null"
            is Class<*> -> obj.canonicalName ?: obj.name
            else -> obj.javaClass.name
        }
    }

    fun toHash(obj: Any?): String? {
        return if (obj == null) "" else "@" + Integer.toHexString(System.identityHashCode(obj))
    }

    fun buildPrefix(message: String?): String = if (message?.isNotBlank() == true) "$message ==> " else ""

    fun toAssertionString(obj: Any?): String {
        val clazz = obj as? Class<*>
        return clazz?.canonicalName ?: clazz?.name ?: obj.toString()
    }
}