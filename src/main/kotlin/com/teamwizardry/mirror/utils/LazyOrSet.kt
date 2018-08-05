package com.teamwizardry.mirror.utils

import kotlin.reflect.KProperty

internal interface LazyOrSet<T>: Lazy<T> {
    override var value: T

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
}

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
