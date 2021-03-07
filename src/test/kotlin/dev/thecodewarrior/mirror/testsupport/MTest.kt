package dev.thecodewarrior.mirror.testsupport

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.impl.MirrorCache
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import java.lang.IllegalArgumentException
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

/**
 * The base class for Mirror tests, which provides access to systems for runtime compilation, as well as extensions for
 * easily accessing Core Reflection objects.
 *
 * ## Basic MTest usage
 * ```kotlin
 * internal class SomeTest: MTest() {
 *     val A by sources.add("A", "@rt(TYPE_USE) @interface A {}")
 *     val X by sources.add("X", "class X {}")
 *     val Generic by sources.add("Generic", "class Generic<T> {}")
 *
 *     val types = sources.types {
 *         +"X[]"
 *         +"Generic<X>[]"
 *         +"@A X @A []"
 *         +"Generic<@A X>[]"
 *         block("K", "V") {
 *             +"K[]"
 *             +"@A Generic<V>"
 *         }
 *     }
 *
 *     @Test
 *     fun `methods should not override themselves`() {
 *         val X by sources.add("X", "public class X { public void method() {} }")
 *         sources.compile()
 *         assertFalse(Mirror.reflect(X._m("method")).doesOverride(X._m("method")))
 *     }
 * }
 * ```
 *
 * ## Basic `TestSources` usage
 * ```kotlin
 * val sources = TestSources()
 * val X: Class<*> by sources.add("X", "class X {}")
 * val A: Class<Annotation> by sources.add("A", "@interface A {}")
 * val types = sources.types {
 *     +"? extends X"
 *     block("T") {
 *         +"T"
 *     }
 * }
 * sources.compile()
 *
 * types["? extends X"]
 * types["T"]
 * ```
 */
@Suppress("TestFunctionName", "PropertyName")
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
    protected inline fun <reified T> _c(): Class<T> = T::class.java

    protected val _boolean: Class<*> = Boolean::class.javaPrimitiveType!!
    protected val _byte: Class<*> = Byte::class.javaPrimitiveType!!
    protected val _char: Class<*> = Char::class.javaPrimitiveType!!
    protected val _short: Class<*> = Short::class.javaPrimitiveType!!
    protected val _int: Class<*> = Int::class.javaPrimitiveType!!
    protected val _long: Class<*> = Long::class.javaPrimitiveType!!
    protected val _float: Class<*> = Float::class.javaPrimitiveType!!
    protected val _double: Class<*> = Double::class.javaPrimitiveType!!
    protected val _object: Class<*> = Any::class.java

    /**
     * An easy way to get an empty list for assertions without having to specify the type. Normally you would have to
     * use `emptyList<Any>()`
     */
    protected fun emptyList(): List<Any> = listOf()

    /**
     * An easy way to get an empty set for assertions without having to specify the type. Normally you would have to
     * use `emptySet<Any>()`
     */
    protected fun emptySet(): Set<Any> = setOf()

    /**
     * Get the specified method from this class. If no parameters are specified and no zero-parameter method exists, the
     * only one with the passed name is returned. Throws if no matching methods exist or multiple matching methods exist
     */
    protected fun Class<*>._m(name: String, vararg parameters: Class<*>): Method {
        if (parameters.isEmpty()) {
            val methods = this.declaredMethods.filter { it.name == name }
            methods.find { it.parameterCount == 0 }?.let { return it }
            if (methods.size != 1) {
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
        if (parameters.isEmpty()) {
            declaredConstructors.find { it.parameterCount == 0 }?.let { return it }
            if (declaredConstructors.size != 1) {
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

    /** Get the specified inner class from this class. */
    protected fun Class<*>._class(name: String): Class<*> = this.declaredClasses.find { it.simpleName == name }
        ?: throw IllegalArgumentException("Couldn't find declared class $name in $this")

    /** Invokes the default (no-arg) constructor, ensuring it's accessible before doing so. */
    @Suppress("UNCHECKED_CAST")
    protected fun <T> Class<*>._new(vararg arguments: Any?): T {
        val constructors = this.declaredConstructors.filter {
            it.name == name && !Modifier.isStatic(it.modifiers) &&
                it.parameterCount == arguments.size && it.parameterTypes.zip(arguments).all { (p, a) ->
                p.isAssignableFrom(a?.javaClass ?: Any::class.java)
            }
        }
        if (constructors.size != 1) {
            throw IllegalArgumentException("Found ${constructors.size} candidates for constructor with parameter " +
                "types `${arguments.joinToString(", ") { it?.javaClass?.simpleName ?: "null" }}")
        }
        return constructors.single()._newInstance(*arguments)
    }

    /** Invokes this constructor, ensuring it's accessible before doing so. */
    @Suppress("UNCHECKED_CAST")
    protected fun <T> Constructor<*>._newInstance(vararg arguments: Any?): T {
        return this.also { it.isAccessible = true }.newInstance(*arguments) as T
    }

    /** Invokes this method, ensuring it's accessible before doing so. */
    @Suppress("UNCHECKED_CAST")
    protected fun <T> Method._call(target: Any?, vararg arguments: Any?): T {
        return this.also { it.isAccessible = true }.invoke(target, *arguments) as T
    }

    /**
     * Call the specified method from this object by searching for methods with parameters that are compatible with the
     * passed arguments. Throws if no matching methods exist or multiple matching methods exist.
     */
    protected fun <T> Any._call(name: String, vararg arguments: Any?): T {
        val methods = this.javaClass.declaredMethods.filter {
            it.name == name && !Modifier.isStatic(it.modifiers) &&
                it.parameterCount == arguments.size && it.parameterTypes.zip(arguments).all { (p, a) ->
                p.isAssignableFrom(a?.javaClass ?: Any::class.java)
            }
        }
        if (methods.size != 1) {
            throw IllegalArgumentException("Found ${methods.size} candidates for method named `$name` with parameter " +
                "types `${arguments.joinToString(", ") { it?.javaClass?.simpleName ?: "null" }}")
        }
        return methods.single()._call(this, *arguments)
    }

    /**
     * Call the specified static method from this class by searching for methods with parameters that are compatible
     * with the passed arguments. Throws if no matching methods exist or multiple matching methods exist.
     */
    protected fun <T> Class<*>._call(name: String, vararg arguments: Any?): T {
        val methods = this.javaClass.declaredMethods.filter {
            it.name == name && Modifier.isStatic(it.modifiers)
            it.parameterCount == arguments.size && it.parameterTypes.zip(arguments).all { (p, a) ->
                p.isAssignableFrom(a?.javaClass ?: Any::class.java)
            }
        }
        if (methods.size != 1) {
            throw IllegalArgumentException("Found ${methods.size} candidates for method named `$name` with parameter " +
                "types `${arguments.joinToString(", ") { it?.javaClass?.simpleName ?: "null" }}")
        }
        return methods.single()._call<T>(null, arguments)
    }

    /** Get the value of this field, ensuring it's accessible before doing so. */
    @Suppress("UNCHECKED_CAST")
    protected fun <T> Field._get(target: Any?): T = this.also { it.isAccessible = true }.get(target) as T

    /** Get the value of this field, ensuring it's accessible before doing so. */
    protected fun Field._set(target: Any?, value: Any?): Unit = this.also { it.isAccessible = true }.set(target, value)

    /** Get the value of the specified field from this object. */
    @Suppress("UNCHECKED_CAST")
    protected fun <T> Any._get(name: String): T = this.javaClass._f(name)._get(this)

    /** Get the value of the specified field from this object. */
    protected fun Any._set(name: String, value: Any?): Unit = this.javaClass._f(name)._set(this, value)

    /** Get the value of the specified static field from this object. */
    @Suppress("UNCHECKED_CAST")
    protected fun <T> Class<*>._get(name: String): T = this._f(name)._get<T>(null)

    /** Get the value of the specified static field from this object. */
    protected fun Class<*>._set(name: String, value: Any?): Unit = this._f(name)._set(null, value)

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