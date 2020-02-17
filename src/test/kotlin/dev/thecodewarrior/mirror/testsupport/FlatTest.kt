package dev.thecodewarrior.mirror.testsupport

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import java.net.URI

/**
 * Useful for having separate class scopes for tests without having fragmented reports.
 *
 * @see FlatTestScanner.scan
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class FlatTest

object FlatTestScanner {
    /**
     * Returns dynamic tests to run all the flat tests declared in the passed class. This currently only works with
     * static inner classes.
     *
     * - Annotated functions in inner classes will be run
     * - Annotated inner classes will have their `run()` methods run
     *
     * To use this create a method in the outer class annotated with [@TestFactory][org.junit.jupiter.api.TestFactory]
     * and return the result of passing `this` to this method. This will return matches from superclasses as well.
     *
     * ```java
     * @TestFactory
     * Iterator<DynamicTest> flat() {
     *     return FlatTestScanner.scan(this)
     * }
     * ```
     */
    @JvmStatic
    fun scan(obj: Any): Iterator<DynamicTest> {
        val classes = generateSequence<Class<*>>(obj.javaClass) { it.superclass }
            .flatMap { it.declaredClasses.asSequence() }
            .toList()
        val testMethods = classes.asSequence()
            .map { clazz ->
                clazz to clazz.declaredMethods.filter { m -> m.isAnnotationPresent(FlatTest::class.java) }
            }
            .filter { (_, methods) -> methods.isNotEmpty() }
            .flatMap { (clazz, methods) ->
                val instance = clazz.newInstance()
                methods.asSequence().map { m ->
                    val name = m.getAnnotation(DisplayName::class.java)?.value ?: m.name

                    m.isAccessible = true
                    DynamicTest.dynamicTest(name, URI("method:${clazz.canonicalName}#${m.name}")) {
                        m.invoke(instance)
                    }
                }
            }

        val testClasses = classes.asSequence()
            .filter { it.isAnnotationPresent(FlatTest::class.java) }
            .map {
                val name = it.getAnnotation(DisplayName::class.java)?.value ?: it.simpleName

                val instance = it.newInstance()
                val method = it.getDeclaredMethod("run")
                method.isAccessible = true
                DynamicTest.dynamicTest(name, URI("method:${it.canonicalName}#run")) {
                    method.invoke(instance)
                }
            }
        return (testMethods + testClasses).iterator()
    }
}
