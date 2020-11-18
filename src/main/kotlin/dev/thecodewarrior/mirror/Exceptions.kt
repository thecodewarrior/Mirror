package dev.thecodewarrior.mirror

/**
 * The common superclass of Mirror exceptions
 */
public open class MirrorException: RuntimeException {
    public constructor(): super()
    public constructor(message: String): super(message)
    public constructor(message: String, cause: Throwable): super(message, cause)
    public constructor(cause: Throwable): super(cause)
}

/**
 * Thrown when attempting to specialize a mirror with invalid types.
 *
 * For example, specializing `List[]` with a component type of `String` instead of something like `List<String>`.
 * `String` isn't a specialization of the raw component, `List`, but `List<String>` is.
 */
public class InvalidSpecializationException: MirrorException {
    public constructor(): super()
    public constructor(message: String): super(message)
    public constructor(message: String, cause: Throwable): super(message, cause)
    public constructor(cause: Throwable): super(cause)
}

/**
 * Thrown when a Mirror was requested but could not be found (e.g. no methods exist with the given name and parameters)
 */
public class NoSuchMirrorException : MirrorException {
    public constructor(): super()
    public constructor(message: String): super(message)
    public constructor(message: String, cause: Throwable): super(message, cause)
    public constructor(cause: Throwable): super(cause)
}
