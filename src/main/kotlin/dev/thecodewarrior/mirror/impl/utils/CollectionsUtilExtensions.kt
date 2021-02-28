package dev.thecodewarrior.mirror.impl.utils

import java.util.Collections
import java.util.IdentityHashMap
import java.util.NavigableMap
import java.util.NavigableSet
import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

// Unmodifiable/synchronized wrappers ==================================================================================

internal fun <T> Collection<T>.unmodifiableView(): Collection<T> = Collections.unmodifiableCollection(this)
internal fun <T> Set<T>.unmodifiableView(): Set<T> = Collections.unmodifiableSet(this)
internal fun <T> SortedSet<T>.unmodifiableView(): SortedSet<T> = Collections.unmodifiableSortedSet(this)
internal fun <T> NavigableSet<T>.unmodifiableView(): NavigableSet<T> = Collections.unmodifiableNavigableSet(this)
internal fun <T> List<T>.unmodifiableView(): List<T> = Collections.unmodifiableList(this)
internal fun <K, V> Map<K, V>.unmodifiableView(): Map<K, V> = Collections.unmodifiableMap(this)
internal fun <K, V> SortedMap<K, V>.unmodifiableView(): SortedMap<K, V> = Collections.unmodifiableSortedMap(this)
internal fun <K, V> NavigableMap<K, V>.unmodifiableView(): NavigableMap<K, V> = Collections.unmodifiableNavigableMap(this)

internal fun <T> Collection<T>.unmodifiableCopy(): Collection<T> = Collections.unmodifiableCollection(this.toList())
internal fun <T> Set<T>.unmodifiableCopy(): Set<T> = Collections.unmodifiableSet(this.toSet())
internal fun <T> SortedSet<T>.unmodifiableCopy(): SortedSet<T> = Collections.unmodifiableSortedSet(this.toSortedSet(this.comparator()))
internal fun <T> NavigableSet<T>.unmodifiableCopy(): NavigableSet<T> = Collections.unmodifiableNavigableSet(TreeSet(this))
internal fun <T> List<T>.unmodifiableCopy(): List<T> = Collections.unmodifiableList(this.toList())
internal fun <K, V> Map<K, V>.unmodifiableCopy(): Map<K, V> = Collections.unmodifiableMap(this.toMap())
internal fun <K, V> SortedMap<K, V>.unmodifiableCopy(): SortedMap<K, V> = Collections.unmodifiableSortedMap(this.toSortedMap(this.comparator()))
internal fun <K, V> NavigableMap<K, V>.unmodifiableCopy(): NavigableMap<K, V> = Collections.unmodifiableNavigableMap(TreeMap(this))

internal fun <T> MutableCollection<T>.synchronizedView(): MutableCollection<T> = Collections.synchronizedCollection(this)
internal fun <T> MutableSet<T>.synchronizedView(): MutableSet<T> = Collections.synchronizedSet(this)
internal fun <T> SortedSet<T>.synchronizedView(): SortedSet<T> = Collections.synchronizedSortedSet(this)
internal fun <T> NavigableSet<T>.synchronizedView(): NavigableSet<T> = Collections.synchronizedNavigableSet(this)
internal fun <T> MutableList<T>.synchronizedView(): MutableList<T> = Collections.synchronizedList(this)
internal fun <K, V> MutableMap<K, V>.synchronizedView(): MutableMap<K, V> = Collections.synchronizedMap(this)
internal fun <K, V> SortedMap<K, V>.synchronizedView(): SortedMap<K, V> = Collections.synchronizedSortedMap(this)
internal fun <K, V> NavigableMap<K, V>.synchronizedView(): NavigableMap<K, V> = Collections.synchronizedNavigableMap(this)

// Unmodifiable/synchronized creators ==================================================================================

internal fun <T> unmodifiableSetOf(vararg values: T): Set<T> = setOf(*values).unmodifiableView()
internal fun <T> unmodifiableSortedSetOf(vararg values: T): SortedSet<T> = sortedSetOf(*values).unmodifiableView()
internal fun <T> unmodifiableListOf(vararg values: T): List<T> = listOf(*values).unmodifiableView()
internal fun <K, V> unmodifiableMapOf(vararg pairs: Pair<K, V>): Map<K, V> = mapOf(*pairs).unmodifiableView()
internal fun <K: Comparable<K>, V> unmodifiableSortedMapOf(vararg pairs: Pair<K, V>): SortedMap<K, V> = sortedMapOf(*pairs).unmodifiableView()

internal fun <T> synchronizedSetOf(vararg values: T): MutableSet<T> = mutableSetOf(*values).synchronizedView()
internal fun <T> synchronizedSortedSetOf(vararg values: T): SortedSet<T> = sortedSetOf(*values).synchronizedView()
internal fun <T> synchronizedListOf(vararg values: T): MutableList<T> = mutableListOf(*values).synchronizedView()
internal fun <K, V> synchronizedMapOf(vararg pairs: Pair<K, V>): MutableMap<K, V> = mutableMapOf(*pairs).synchronizedView()
internal fun <K: Comparable<K>, V> synchronizedSortedMapOf(vararg pairs: Pair<K, V>): SortedMap<K, V> = sortedMapOf(*pairs).synchronizedView()

// Checked casts/wrappers ==============================================================================================

internal inline fun <reified T> MutableCollection<T>.checkedView(): MutableCollection<T> = Collections.checkedCollection(this.toList(), T::class.java)
internal inline fun <reified T> MutableSet<T>.checkedView(): MutableSet<T> = Collections.checkedSet(this.toSet(), T::class.java)
internal inline fun <reified T> SortedSet<T>.checkedView(): SortedSet<T> = Collections.checkedSortedSet(this.toSortedSet(this.comparator()), T::class.java)
internal inline fun <reified T> NavigableSet<T>.checkedView(): NavigableSet<T> = Collections.checkedNavigableSet(TreeSet(this), T::class.java)
internal inline fun <reified T> MutableList<T>.checkedView(): MutableList<T> = Collections.checkedList(this.toList(), T::class.java)
internal inline fun <reified K, reified V> MutableMap<K, V>.checkedView(): MutableMap<K, V> = Collections.checkedMap(this.toMap(), K::class.java, V::class.java)
internal inline fun <reified K, reified V> SortedMap<K, V>.checkedView(): SortedMap<K, V> = Collections.checkedSortedMap(this.toSortedMap(this.comparator()), K::class.java, V::class.java)
internal inline fun <reified K, reified V> NavigableMap<K, V>.checkedView(): NavigableMap<K, V> = Collections.checkedNavigableMap(TreeMap(this), K::class.java, V::class.java)

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> Array<*>.checkedCast(): Array<T> {
    this.forEach {
        it as T
    }
    return this as Array<T>
}
internal inline fun <reified T> Collection<*>.checkedCast(): Collection<T> = checkCollection<T, Collection<T>>(this)
internal inline fun <reified T> Set<*>.checkedCast(): Set<T> = checkCollection<T, Set<T>>(this)
internal inline fun <reified T> SortedSet<*>.checkedCast(): SortedSet<T> = checkCollection<T, SortedSet<T>>(this)
internal inline fun <reified T> NavigableSet<*>.checkedCast(): NavigableSet<T> = checkCollection<T, NavigableSet<T>>(this)
internal inline fun <reified T> List<*>.checkedCast(): List<T> = checkCollection<T, List<T>>(this)
internal inline fun <reified K, reified V> Map<*, *>.checkedCast(): Map<K, V> = checkMap<K, V, Map<K, V>>(this)
internal inline fun <reified K, reified V> SortedMap<*, *>.checkedCast(): SortedMap<K, V> = checkMap<K, V, SortedMap<K, V>>(this)
internal inline fun <reified K, reified V> NavigableMap<*, *>.checkedCast(): NavigableMap<K, V> = checkMap<K, V, NavigableMap<K, V>>(this)

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

internal fun <K, V> identityMapOf(): MutableMap<K, V> = IdentityHashMap()
internal fun <K, V> identityMapOf(vararg pairs: Pair<K, V>): MutableMap<K, V> {
    return IdentityHashMap<K, V>(mapCapacity(pairs.size)).apply { putAll(pairs) }
}

internal fun <T> identitySetOf(): MutableSet<T> = Collections.newSetFromMap(IdentityHashMap())
internal fun <T> identitySetOf(vararg elements: T): MutableSet<T> {
    val map = IdentityHashMap<T, Boolean>(mapCapacity(elements.size))
    return elements.toCollection(Collections.newSetFromMap(map))
}

internal fun <K, V> Map<K, V>.toIdentityMap(): MutableMap<K, V> = IdentityHashMap(this)
internal fun <T> Set<T>.toIdentitySet(): MutableSet<T> = identitySetOf<T>().also { it.addAll(this) }

internal fun <K, V> Map<K, V>.unmodifiableIdentityCopy(): Map<K, V> = Collections.unmodifiableMap(this.toIdentityMap())
internal fun <T> Set<T>.unmodifiableIdentityCopy(): Set<T> = Collections.unmodifiableSet(this.toIdentitySet())

// Misc utils ==========================================================================================================

/**
 * Returns a list containing all unique elements from this collection. If any duplicates are found the first object is
 * used.
 */
internal fun <T> Collection<T>.unique(): List<T> {
    val set = mutableSetOf<T>()
    set.addAll(this)
    return set.toList()
}

/**
 * Returns a list containing all unique elements from this collection. If any duplicates are found the first object is
 * used.
 */
internal fun <T, K> Collection<T>.uniqueBy(mapping: (T) -> K): List<T> {
    val set = mutableSetOf<K>()
    return this.filter { set.add(mapping(it)) }
}

/**
 * Removes all duplicate elements from this list. If duplicates are found the first object is left in the collection.
 */
internal fun <T> MutableCollection<T>.removeDuplicates() {
    val set = mutableSetOf<T>()
    this.removeIf { !set.add(it) }
}

/**
 * Removes all duplicate elements from this list. If duplicates are found the first object is left in the collection.
 */
internal fun <T, K> MutableCollection<T>.removeDuplicatesBy(mapping: (T) -> K) {
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
