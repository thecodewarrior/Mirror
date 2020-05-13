package dev.thecodewarrior.mirror.testsupport

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.MirrorCache
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import java.lang.IllegalArgumentException
import java.lang.reflect.AccessibleObject
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
    /**
     * The value configured in the constructor is compiled, then each test individually gets its own instance.
     */
    val sources: TestSources get() = _sources
    // storage is in a separate property to get rid of IntelliJ's irritating underline on something that's effectively
    // constant in each context it's used.
    private var _sources: TestSources = TestSources()

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
    /**
     * Get the specified constructor from this class. If no parameters are specified and no zero-parameter constructor
     * exists, the only constructor is returned. Throws if no matching constructors exist or if no parameters were
     * passed and there are multiple constructors.
     */
    protected fun Class<*>._constructor(vararg parameters: Class<*>): Constructor<*> {
        if(parameters.isEmpty()) {
            declaredConstructors.find { it.parameterCount == 0 }?.let { return it }
            if(declaredConstructors.size != 1) {
                throw IllegalArgumentException("Found ${declaredConstructors.size} constructors when looking for the " +
                    "only constructor")
            }
            return declaredConstructors.first()
        } else {
            return this.getDeclaredConstructor(*parameters)
        }
    }
    /** Get the specified field from this class. */
    protected fun Class<*>._f(name: String): Field = this.getDeclaredField(name)
    /** Get the specified field from this class. */
    protected fun Class<*>._class(name: String): Class<*> = this.declaredClasses.find { it.simpleName == name }
        ?: throw IllegalArgumentException("Couldn't find declared class $name in $this")

    /** Invokes this constructor, ensuring it's accessible before doing so. */
    @Suppress("UNCHECKED_CAST")
    protected fun<T> Constructor<*>._newInstance(vararg arguments: Any?): T
        = this.also { it.isAccessible = true }.newInstance(*arguments) as T
    /** Invokes this method, ensuring it's accessible before doing so. */
    @Suppress("UNCHECKED_CAST")
    protected fun<T> Method._invoke(target: Any?, vararg arguments: Any?): T
        = this.also { it.isAccessible = true }.invoke(target, *arguments) as T
    /** Get the value of this field, ensuring it's accessible before doing so. */
    @Suppress("UNCHECKED_CAST")
    protected fun<T> Field._get(target: Any?): T
        = this.also { it.isAccessible = true }.get(target) as T
    /** Get the value of this field, ensuring it's accessible before doing so. */
    protected fun Field._set(target: Any?, value: Any?): Unit
        = this.also { it.isAccessible = true }.set(target, value)

    /** Get the value of the specified field from this object. */
    @Suppress("UNCHECKED_CAST")
    protected fun<T> Any._get(name: String): T
        = this.javaClass._f(name)._get(this)
    /** Get the value of the specified field from this object. */
    protected fun Any._set(name: String, value: Any?): Unit
        = this.javaClass._f(name)._get(this)

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
        _sources = TestSources()
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