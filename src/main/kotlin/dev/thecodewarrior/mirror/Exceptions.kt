package dev.thecodewarrior.mirror

/**
 * The common superclass of Mirror exceptions
 */
open class MirrorException: RuntimeException {
    constructor(): super()
    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(cause: Throwable): super(cause)
}

/**
 * Thrown when attempting to specialize a mirror with invalid types.
 *
 * For example, specializing `List[]` with a component type of `String` instead of something like `List<String>`.
 * `String` isn't a specialization of the raw component, `List`, but `List<String>` is.
 */
class InvalidSpecializationException: MirrorException {
    constructor(): super()
    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(cause: Throwable): super(cause)
}

/**
 * Thrown when a Mirror was requested but could not be found (e.g. no methods exist with the given name and parameters)
 */
class NoSuchMirrorException : MirrorException {
    constructor(): super()
    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(cause: Throwable): super(cause)
}
