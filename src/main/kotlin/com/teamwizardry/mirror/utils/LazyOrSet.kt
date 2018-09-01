package com.teamwizardry.mirror.utils

import kotlin.reflect.KProperty

internal interface LazyOrSet<T>: Lazy<T> {
    override var value: T

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
}

/**
 * Based upon Kotlin's [lazy] method, except that the value of the property can be prematurely set, thus bypassing the
 * initializer expression. The following was copied from Kotlin's docs as it all applies here as well:
 *
 * Creates a new instance of the [Lazy] that uses the specified initialization function [initializer]
 * and the default thread-safety mode [LazyThreadSafetyMode.SYNCHRONIZED].
 *
 * If the initialization of a value throws an exception, it will attempt to reinitialize the value at next access.
 *
 * Note that the returned instance uses itself to synchronize on. Do not synchronize from external code on
 * the returned instance as it may cause accidental deadlock. Also this behavior can be changed in the future.
 */
internal fun <T> lazyOrSet(initializer: () -> T): LazyOrSet<T> = SynchronizedLazyOrSetImpl(initializer)

private object UNINITIALIZED_VALUE

private class SynchronizedLazyOrSetImpl<T>(initializer: () -> T, lock: Any? = null): LazyOrSet<T> {
    private var initializer: (() -> T)? = initializer
    @Volatile private var _value: Any? = UNINITIALIZED_VALUE
    // final field is required to enable safe publication of constructed instance
    private val lock = lock ?: this

    override var value: T
        get() {
            val _v1 = _value
            if (_v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST")
                return _v1 as T
            }

            return synchronized(lock) {
                val _v2 = _value
                if (_v2 !== UNINITIALIZED_VALUE) {
                    @Suppress("UNCHECKED_CAST") (_v2 as T)
                } else {
                    val typedValue = initializer!!()
                    _value = typedValue
                    initializer = null
                    typedValue
                }
            }
        }
        set(value) {
            synchronized(lock) {
                _value = value
                initializer = null
            }
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { this.value = value }
}
