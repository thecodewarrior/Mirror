package dev.thecodewarrior.mirror.utils

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles.publicLookup
import java.lang.invoke.MethodType
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Source: [LibrarianLib](https://github.com/TeamWizardry/LibrarianLib/blob/1.12/src/main/java/com/teamwizardry/librarianlib/features/methodhandles/MethodHandleHelper.kt)
 *
 * @author WireSegal, thecodewarrior
 *
 * Changes - thecodewarrior:
 * Stripped down to only the basic Core Reflection -> wrapper methods
 */
internal object MethodHandleHelper {

    //region getter

    /**
     * Provides a wrapper for an existing MethodHandle getter.
     * No casts are required to use this, although they are recommended.
     */
    @JvmStatic
    fun wrapperForGetter(handle: MethodHandle): (Any) -> Any? {
        val wrapper = InvocationWrapper(handle.asType(MethodType.genericMethodType(1)))
        return { wrapper(it) }
    }

    @JvmStatic
    fun wrapperForGetter(field: Field): (Any) -> Any? = wrapperForGetter(publicLookup().unreflectGetter(field))

    /**
     * Provides a wrapper for an existing static MethodHandle getter.
     * No casts are required to use this, although they are recommended.
     */
    @JvmStatic
    fun wrapperForStaticGetter(handle: MethodHandle): () -> Any? {
        val wrapper = InvocationWrapper(handle.asType(MethodType.genericMethodType(0)))
        return { wrapper() }
    }

    @JvmStatic
    fun wrapperForStaticGetter(field: Field): () -> Any? = wrapperForStaticGetter(publicLookup().unreflectGetter(field))

    //endregion

    //region setter

    /**
     * Provides a wrapper for an existing MethodHandle setter.
     */
    @JvmStatic
    fun wrapperForSetter(handle: MethodHandle): (Any, Any?) -> Unit {
        val wrapper = InvocationWrapper(handle.asType(MethodType.genericMethodType(2)))
        return { obj, value -> wrapper(obj, value) }
    }

    @JvmStatic
    fun wrapperForSetter(field: Field): (Any, Any?) -> Unit = wrapperForSetter(publicLookup().unreflectSetter(field))

    /**
     * Provides a wrapper for an existing static MethodHandle setter.
     */
    @JvmStatic
    fun wrapperForStaticSetter(handle: MethodHandle): (Any?) -> Unit {
        val wrapper = InvocationWrapper(handle.asType(MethodType.genericMethodType(1)))
        return { wrapper(it) }
    }

    @JvmStatic
    fun wrapperForStaticSetter(field: Field): (Any?) -> Unit = wrapperForStaticSetter(publicLookup().unreflectSetter(field))

    //endregion

    //region methods

    /**
     * Provides a wrapper for an existing MethodHandle method wrapper.
     */
    @JvmStatic
    fun wrapperForMethod(handle: MethodHandle): (Any, Array<Any?>) -> Any? {
        val type = handle.type()
        val count = type.parameterCount()
        var remapped = handle.asType(MethodType.genericMethodType(count))

        if (count > 1)
            remapped = remapped.asSpreader(Array<Any>::class.java, count)

        val wrapper = InvocationWrapper(remapped)
        if (count == 1)
            return { obj, _ -> wrapper(obj) }

        return { obj, args -> wrapper.invokeArity(arrayOf(obj, *args)) }
    }

    @JvmStatic
    fun wrapperForMethod(method: Method): (Any, Array<Any?>) -> Any? = wrapperForMethod(publicLookup().unreflect(method))

    /**
     * Provides a wrapper for an existing MethodHandle method wrapper.
     */
    @JvmStatic
    fun wrapperForStaticMethod(handle: MethodHandle): (Array<Any?>) -> Any? {
        val type = handle.type()
        val count = type.parameterCount()
        val wrapper = InvocationWrapper(handle.asType(MethodType.genericMethodType(count)).asSpreader(Array<Any>::class.java, count))
        return { wrapper.invokeArity(it) }
    }

    @JvmStatic
    fun wrapperForStaticMethod(method: Method): (Array<Any?>) -> Any? = wrapperForStaticMethod(publicLookup().unreflect(method))

    //endregion

    //region constructors

    /**
     * Provides a wrapper for an existing MethodHandle constructor wrapper.
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun wrapperForConstructor(handle: MethodHandle): (Array<Any?>) -> Any {
        val type = handle.type()
        val count = type.parameterCount()
        val wrapper = InvocationWrapper(handle.asType(MethodType.genericMethodType(count)).asSpreader(Array<Any>::class.java, count))
        return { wrapper.invokeArity(it) as Any }
    }

    @JvmStatic
    fun wrapperForConstructor(constructor: Constructor<Any>): (Array<Any?>) -> Any {
        return wrapperForConstructor(publicLookup().unreflectConstructor(constructor))
    }

    //endregion
}
