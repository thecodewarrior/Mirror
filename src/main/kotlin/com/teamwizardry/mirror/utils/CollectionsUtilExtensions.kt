package com.teamwizardry.mirror.utils

import java.util.Collections
import java.util.IdentityHashMap
import java.util.NavigableMap
import java.util.NavigableSet
import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

// Unmodifiable/synchronized wrappers ==================================================================================

fun <T> Collection<T>.unmodifiableView(): Collection<T> = Collections.unmodifiableCollection(this)
fun <T> Set<T>.unmodifiableView(): Set<T> = Collections.unmodifiableSet(this)
fun <T> SortedSet<T>.unmodifiableView(): SortedSet<T> = Collections.unmodifiableSortedSet(this)
fun <T> NavigableSet<T>.unmodifiableView(): NavigableSet<T> = Collections.unmodifiableNavigableSet(this)
fun <T> List<T>.unmodifiableView(): List<T> = Collections.unmodifiableList(this)
fun <K, V> Map<K, V>.unmodifiableView(): Map<K, V> = Collections.unmodifiableMap(this)
fun <K, V> SortedMap<K, V>.unmodifiableView(): SortedMap<K, V> = Collections.unmodifiableSortedMap(this)
fun <K, V> NavigableMap<K, V>.unmodifiableView(): NavigableMap<K, V> = Collections.unmodifiableNavigableMap(this)

fun <T> Collection<T>.unmodifiableCopy(): Collection<T> = Collections.unmodifiableCollection(this.toList())
fun <T> Set<T>.unmodifiableCopy(): Set<T> = Collections.unmodifiableSet(this.toSet())
fun <T> SortedSet<T>.unmodifiableCopy(): SortedSet<T> = Collections.unmodifiableSortedSet(this.toSortedSet(this.comparator()))
fun <T> NavigableSet<T>.unmodifiableCopy(): NavigableSet<T> = Collections.unmodifiableNavigableSet(TreeSet(this))
fun <T> List<T>.unmodifiableCopy(): List<T> = Collections.unmodifiableList(this.toList())
fun <K, V> Map<K, V>.unmodifiableCopy(): Map<K, V> = Collections.unmodifiableMap(this.toMap())
fun <K, V> SortedMap<K, V>.unmodifiableCopy(): SortedMap<K, V> = Collections.unmodifiableSortedMap(this.toSortedMap(this.comparator()))
fun <K, V> NavigableMap<K, V>.unmodifiableCopy(): NavigableMap<K, V> = Collections.unmodifiableNavigableMap(TreeMap(this))

fun <T> MutableCollection<T>.synchronizedView(): MutableCollection<T> = Collections.synchronizedCollection(this)
fun <T> MutableSet<T>.synchronizedView(): MutableSet<T> = Collections.synchronizedSet(this)
fun <T> SortedSet<T>.synchronizedView(): SortedSet<T> = Collections.synchronizedSortedSet(this)
fun <T> NavigableSet<T>.synchronizedView(): NavigableSet<T> = Collections.synchronizedNavigableSet(this)
fun <T> MutableList<T>.synchronizedView(): MutableList<T> = Collections.synchronizedList(this)
fun <K, V> MutableMap<K, V>.synchronizedView(): MutableMap<K, V> = Collections.synchronizedMap(this)
fun <K, V> SortedMap<K, V>.synchronizedView(): SortedMap<K, V> = Collections.synchronizedSortedMap(this)
fun <K, V> NavigableMap<K, V>.synchronizedView(): NavigableMap<K, V> = Collections.synchronizedNavigableMap(this)

// Checked casts/wrappers ==============================================================================================

inline fun <reified T> MutableCollection<T>.checkedView(): MutableCollection<T> = Collections.checkedCollection(this.toList(), T::class.java)
inline fun <reified T> MutableSet<T>.checkedView(): MutableSet<T> = Collections.checkedSet(this.toSet(), T::class.java)
inline fun <reified T> SortedSet<T>.checkedView(): SortedSet<T> = Collections.checkedSortedSet(this.toSortedSet(this.comparator()), T::class.java)
inline fun <reified T> NavigableSet<T>.checkedView(): NavigableSet<T> = Collections.checkedNavigableSet(TreeSet(this), T::class.java)
inline fun <reified T> MutableList<T>.checkedView(): MutableList<T> = Collections.checkedList(this.toList(), T::class.java)
inline fun <reified K, reified V> MutableMap<K, V>.checkedView(): MutableMap<K, V> = Collections.checkedMap(this.toMap(), K::class.java, V::class.java)
inline fun <reified K, reified V> SortedMap<K, V>.checkedView(): SortedMap<K, V> = Collections.checkedSortedMap(this.toSortedMap(this.comparator()), K::class.java, V::class.java)
inline fun <reified K, reified V> NavigableMap<K, V>.checkedView(): NavigableMap<K, V> = Collections.checkedNavigableMap(TreeMap(this), K::class.java, V::class.java)

@Suppress("UNCHECKED_CAST")
inline fun <reified T> Array<*>.checkedCast(): Array<T> {
    this.forEach {
        it as T
    }
    return this as Array<T>
}
inline fun <reified T> Collection<*>.checkedCast(): Collection<T> = checkCollection<T, Collection<T>>(this)
inline fun <reified T> Set<*>.checkedCast(): Set<T> = checkCollection<T, Set<T>>(this)
inline fun <reified T> SortedSet<*>.checkedCast(): SortedSet<T> = checkCollection<T, SortedSet<T>>(this)
inline fun <reified T> NavigableSet<*>.checkedCast(): NavigableSet<T> = checkCollection<T, NavigableSet<T>>(this)
inline fun <reified T> List<*>.checkedCast(): List<T> = checkCollection<T, List<T>>(this)
inline fun <reified K, reified V> Map<*, *>.checkedCast(): Map<K, V> = checkMap<K, V, Map<K, V>>(this)
inline fun <reified K, reified V> SortedMap<*, *>.checkedCast(): SortedMap<K, V> = checkMap<K, V, SortedMap<K, V>>(this)
inline fun <reified K, reified V> NavigableMap<*, *>.checkedCast(): NavigableMap<K, V> = checkMap<K, V, NavigableMap<K, V>>(this)

@PublishedApi
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T, R: Any> checkCollection(collection: Collection<*>): R {
    collection.forEach {
        it as T
    }
    return collection as R
}

@PublishedApi
@Suppress("UNCHECKED_CAST")
internal inline fun <reified K, reified V, R: Any> checkMap(map: Map<*, *>): R {
    map.forEach { key, value ->
        key as K
        value as V
    }
    return map as R
}

// Identity map ========================================================================================================

fun <K, V> identityMapOf(): MutableMap<K, V> = IdentityHashMap()
fun <K, V> identityMapOf(vararg pairs: Pair<K, V>): MutableMap<K, V> {
    return IdentityHashMap<K, V>(mapCapacity(pairs.size)).apply { putAll(pairs) }
}

fun <T> identitySetOf(): MutableSet<T> = Collections.newSetFromMap(IdentityHashMap())
fun <T> identitySetOf(vararg elements: T): MutableSet<T> {
    val map = IdentityHashMap<T, Boolean>(mapCapacity(elements.size))
    return elements.toCollection(Collections.newSetFromMap(map))
}

fun <K, V> Map<K, V>.toIdentityMap(): MutableMap<K, V> = IdentityHashMap(this)
fun <T> Set<T>.toIdentitySet(): MutableSet<T> = identitySetOf<T>().also { it.addAll(this) }

fun <K, V> Map<K, V>.unmodifiableIdentityCopy(): Map<K, V> = Collections.unmodifiableMap(this.toIdentityMap())
fun <T> Set<T>.unmodifiableIdentityCopy(): Set<T> = Collections.unmodifiableSet(this.toIdentitySet())

// Misc utils ==========================================================================================================

/**
 * Returns a list containing all unique elements from this collection. If any duplicates are found the first object is
 * used.
 */
fun <T> Collection<T>.unique(): List<T> {
    val set = mutableSetOf<T>()
    set.addAll(this)
    return set.toList()
}

/**
 * Returns a list containing all unique elements from this collection. If any duplicates are found the first object is
 * used.
 */
fun <T, K> Collection<T>.uniqueBy(mapping: (T) -> K): List<T> {
    val set = mutableSetOf<K>()
    return this.filter { set.add(mapping(it)) }
}

/**
 * Removes all duplicate elements from this list. If duplicates are found the first object is left in the collection.
 */
fun <T> MutableCollection<T>.removeDuplicates() {
    val set = mutableSetOf<T>()
    this.removeIf { !set.add(it) }
}

/**
 * Removes all duplicate elements from this list. If duplicates are found the first object is left in the collection.
 */
fun <T, K> MutableCollection<T>.removeDuplicatesBy(mapping: (T) -> K) {
    val set = mutableSetOf<K>()
    this.removeIf { !set.add(mapping(it)) }
}

// Private support =====================================================================================================

// ripped from the Kotlin runtime:
private fun mapCapacity(expectedSize: Int): Int {
    if (expectedSize < 3) {
        return expectedSize + 1
    }
    if (expectedSize < INT_MAX_POWER_OF_TWO) {
        return expectedSize + expectedSize / 3
    }
    return Int.MAX_VALUE // any large value
}

private const val INT_MAX_POWER_OF_TWO: Int = Int.MAX_VALUE / 2 + 1
