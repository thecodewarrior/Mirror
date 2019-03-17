package com.teamwizardry.mirror.utils

import java.util.Collections
import java.util.NavigableMap
import java.util.NavigableSet
import java.util.SortedMap
import java.util.SortedSet

internal fun <T> Collection<T>.unmodifiable() = Collections.unmodifiableCollection(this)
internal fun <T> Set<T>.unmodifiable() = Collections.unmodifiableSet(this)
internal fun <T> SortedSet<T>.unmodifiable() = Collections.unmodifiableSortedSet(this)
internal fun <T> NavigableSet<T>.unmodifiable() = Collections.unmodifiableNavigableSet(this)
internal fun <T> List<T>.unmodifiable() = Collections.unmodifiableList(this)
internal fun <K, V> Map<K, V>.unmodifiable() = Collections.unmodifiableMap(this)
internal fun <K, V> SortedMap<K, V>.unmodifiable() = Collections.unmodifiableSortedMap(this)
internal fun <K, V> NavigableMap<K, V>.unmodifiable() = Collections.unmodifiableNavigableMap(this)
internal fun <T> Collection<T>.synchronized() = Collections.synchronizedCollection(this)
internal fun <T> Set<T>.synchronized() = Collections.synchronizedSet(this)
internal fun <T> SortedSet<T>.synchronized() = Collections.synchronizedSortedSet(this)
internal fun <T> NavigableSet<T>.synchronized() = Collections.synchronizedNavigableSet(this)
internal fun <T> List<T>.synchronized() = Collections.synchronizedList(this)
internal fun <K, V> Map<K, V>.synchronized() = Collections.synchronizedMap(this)
internal fun <K, V> SortedMap<K, V>.synchronized() = Collections.synchronizedSortedMap(this)
internal fun <K, V> NavigableMap<K, V>.synchronized() = Collections.synchronizedNavigableMap(this)

internal fun <T> Collection<T>.unmodifiableCopy() = Collections.unmodifiableCollection(this.toList())
internal fun <T> Set<T>.unmodifiableCopy() = Collections.unmodifiableSet(this.toSet())
internal fun <T> List<T>.unmodifiableCopy() = Collections.unmodifiableList(this.toList())
internal fun <K, V> Map<K, V>.unmodifiableCopy() = Collections.unmodifiableMap(this.toMap())
