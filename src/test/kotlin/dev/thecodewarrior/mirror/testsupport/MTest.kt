package dev.thecodewarrior.mirror.testsupport

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.MirrorCache
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import java.lang.IllegalArgumentException
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

@Suppress("TestFunctionName")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class MTest {
    val sources: TestSources = TestSources()

    /** Get a Class instance */
    protected inline fun <reified T> _c(): Class<*> = T::class.java

    /**
     * Get the specified method from this class. If no parameters are specified and no zero-parameter method exists, the
     * only one with the passed name is returned. Throws if no matching methods exist or multiple matching methods exist
     */
    protected fun Class<*>._m(name: String, vararg parameters: Class<*>): Method {
        if(parameters.isEmpty()) {
            val methods = this.declaredMethods.filter { it.name == name }
            methods.find { it.parameterCount == 0 }?.let { return it }
            if(methods.size != 1) {
                throw IllegalArgumentException("Found ${methods.size} candidates for method named `$name`")
            }
            return methods.first()
        } else {
            return this.getDeclaredMethod(name, *parameters)
        }
    }
    /** Get the specified field from this class. */
    protected fun Class<*>._f(name: String): Field = this.getDeclaredField(name)

    /** Shorthand to easily get the backing method for a KFunction that represents a method */
    protected val KFunction<*>.m: Method get() = this.javaMethod!!
    /** Shorthand to easily get the backing constructor for a KFunction that represents a constructor */
    protected val KFunction<*>.c: Constructor<*> get() = this.javaConstructor!!

    @BeforeAll
    fun compileSources() {
        sources.compile()
    }

    @BeforeEach
    fun beforeEachTest() {
        this.initializeForTest()
    }

    open fun initializeForTest() {
        cacheField.set(Mirror, MirrorCache())
        typesField.set(Mirror, createTypesMethod.invoke(Mirror))
    }

    private companion object {
        val cacheField = Mirror::class.java.getDeclaredField("cache")
        val typesField = Mirror::class.java.getDeclaredField("_types")
        val createTypesMethod = Mirror::class.java.getDeclaredMethod("createTypes")

        init {
            cacheField.isAccessible = true
            typesField.isAccessible = true
            createTypesMethod.isAccessible = true
        }
    }
}