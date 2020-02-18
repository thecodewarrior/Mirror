package dev.thecodewarrior.mirror.testsupport

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.MirrorCache
import dev.thecodewarrior.mirror.joor.Compile
import dev.thecodewarrior.mirror.joor.CompileOptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

@Suppress("TestFunctionName")
abstract class MTest {
    /** Get a Class instance */
    protected inline fun <reified T> _c(): Class<*> = T::class.java
    /** Get a Method from this class */
    protected fun _m(name: String, vararg parameters: Class<*>): Method = this.javaClass.m(name, *parameters)
    /** Get a Field from this class */
    protected fun _f(name: String): Field = this.javaClass.f(name)

    /**
     * Get the specified method from this class. If no parameters are specified and no zero-parameter method exists, the
     * only one with the passed name is returned. Throws if no matching methods exist or multiple matching methods exist
     */
    protected inline fun <reified T> m(name: String, vararg parameters: Class<*>): Method
        = T::class.java.m(name, *parameters)

    /** Get the specified field from this class. */
    protected inline fun <reified T> f(name: String): Field = T::class.java.getDeclaredField(name)

    /**
     * Get the specified method from this class. If no parameters are specified and no zero-parameter method exists, the
     * only one with the passed name is returned. Throws if no matching methods exist or multiple matching methods exist
     */
    protected fun Class<*>.m(name: String, vararg parameters: Class<*>): Method {
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
    protected fun Class<*>.f(name: String): Field = this.getDeclaredField(name)

    /**
     * Get the specified method from this class. If no parameters are specified and no zero-parameter method exists, the
     * only one with the passed name is returned. Throws if no matching methods exist or multiple matching methods exist
     */
    protected fun KClass<*>.m(name: String, vararg parameters: Class<*>): Method = this.java.m(name, *parameters)
    /** Shorthand to easily get the backing method for a KFunction that represents a method */
    protected val KFunction<*>.m: Method get() = this.javaMethod!!
    /** Shorthand to easily get the backing constructor for a KFunction that represents a constructor */
    protected val KFunction<*>.c: Constructor<*> get() = this.javaConstructor!!

    /** Get the specified field from this class. */
    protected fun KClass<*>.f(name: String): Field = this.java.getDeclaredField(name)

    /**
     * The root package for test classes. Every test class compiled using [compile] will be placed relative to this
     * package.
     */
    val testClassRoot: String = "testgen." + javaClass.canonicalName

    /**
     * Compiles and returns a class at runtime. This will return the existing class, if it exists.
     *
     * Compiled classes are placed relative to [testClassRoot], and the contents of this test are imported using a
     * wildcard import. Any occurrences of `%root%` in the code will be replaced with the [testClassRoot].
     *
     * @param targetPackage The relative package for this class, or an empty string if this class should be at the root.
     * @param name The name of the class to extract
     * @param code The code to compile. This should not include the package declaration.
     */
    protected fun compile(targetPackage: String, name: String, code: String): Compilation {
        val fullPackage = if(targetPackage.isEmpty()) testClassRoot else "$testClassRoot.$targetPackage"
        val reflect = Compile.compile("$fullPackage.$name",
            "package $fullPackage;\nimport ${javaClass.canonicalName}.*;\n" + code.replace("%root%", testClassRoot),
            CompileOptions()
        )
        return Compilation(reflect)
    }

    protected fun compile(name: String, code: String): Compilation = compile("", name, code)

    protected inner class Compilation(val value: Class<*>) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Class<*> {
            return value
        }
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