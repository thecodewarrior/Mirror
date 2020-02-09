package dev.thecodewarrior.mirror.testsupport

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.MirrorCache
import org.junit.jupiter.api.BeforeEach
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass

internal open class MirrorTestBase {
    @BeforeEach
    fun beforeEachTest() {
        this.initializeForTest()
    }

    open fun initializeForTest() {
        cacheField.set(Mirror, MirrorCache())
        typesField.set(Mirror, createTypesMethod.invoke(Mirror))
    }

    protected fun Class<*>.f(name: String): Field = this.getDeclaredField(name)
    protected fun KClass<*>.f(name: String): Field = this.java.getDeclaredField(name)
    protected inline fun <reified T> f(name: String): Field = T::class.java.getDeclaredField(name)

    protected fun Class<*>.m(name: String, vararg parameters: Class<*>): Method
        = this.getDeclaredMethod(name, *parameters)
    protected fun KClass<*>.m(name: String, vararg parameters: Class<*>): Method
        = this.java.getDeclaredMethod(name, *parameters)
    protected inline fun <reified T> m(name: String, vararg parameters: Class<*>): Method
        = T::class.java.getDeclaredMethod(name, *parameters)

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