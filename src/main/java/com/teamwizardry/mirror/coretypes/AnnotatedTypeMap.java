/*
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or at <a href="http://www.apache.org/licenses/LICENSE-2">apache.org</a>.
 */

package com.teamwizardry.mirror.coretypes;

import java.lang.reflect.AnnotatedType;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A {@link Map} implementation keyed by {@link AnnotatedType}.
 * The standard maps do not usually suffice as {@link AnnotatedType} implements neither {@code equals} nor {@code hashCode}.
 * This implementation overcomes that limitation by transparently turning each {@link AnnotatedType} used as the key
 * into the canonical form using {@link GenericTypeReflector#GenericTypeReflector.toCanonical(AnnotatedType)}.
 * By default, {@code AnnotatedTypeMap} instances are backed by a {@link HashMap}, but any map can be used instead.
 * The guarantees of {@code AnnotatedTypeMap} are then the same as of the map it is backed by.
 *
 * @param <V> the type of mapped values
 *
 * @see AnnotatedTypeSet
 */
public class AnnotatedTypeMap<K extends AnnotatedType, V> implements Map<K, V> {

    private final Map<K, V> inner;

    /**
     * Constructs an instance backed by a {@link HashMap}
     */
    public AnnotatedTypeMap() {
        this(new HashMap<>());
    }

    /**
     * Constructs an instance backed by the provided map, keeping its guarantees
     *
     * @param inner A non-null map instance that will back the constructed {@code AnnotatedTypeMap}
     */
    @SuppressWarnings("WeakerAccess")
    public AnnotatedTypeMap(Map<K, V> inner) {
        Objects.requireNonNull(inner);
        if (!inner.isEmpty()) {
            throw new IllegalArgumentException("The provided map must be empty");
        }
        this.inner = inner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return inner.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        return key instanceof AnnotatedType && inner.containsKey(GenericTypeReflector.toCanonical((AnnotatedType) key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
        return inner.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(Object key) {
        return key instanceof AnnotatedType ? inner.get(GenericTypeReflector.toCanonical((AnnotatedType) key)) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(K key, V value) {
        return inner.put(GenericTypeReflector.toCanonical(key), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V remove(Object key) {
        return key instanceof AnnotatedType ? inner.remove(GenericTypeReflector.toCanonical((AnnotatedType) key)) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        Map<? extends K, ? extends V> canonical =  m.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(GenericTypeReflector.toCanonical(e.getKey()), e.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        inner.putAll(canonical);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        inner.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<K> keySet() {
        return inner.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V> values() {
        return inner.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return inner.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
        return inner.equals(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return inner.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return key instanceof AnnotatedType ? inner.getOrDefault(GenericTypeReflector.toCanonical((AnnotatedType) key), defaultValue) : defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        inner.forEach(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        inner.replaceAll(function);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V putIfAbsent(K key, V value) {
        return inner.putIfAbsent(GenericTypeReflector.toCanonical(key), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object key, Object value) {
        return key instanceof AnnotatedType && inner.remove(GenericTypeReflector.toCanonical((AnnotatedType) key), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return inner.replace(GenericTypeReflector.toCanonical(key), oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V replace(K key, V value) {
        return inner.replace(GenericTypeReflector.toCanonical(key), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return inner.computeIfAbsent(GenericTypeReflector.toCanonical(key), mappingFunction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return inner.computeIfPresent(GenericTypeReflector.toCanonical(key), remappingFunction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return inner.compute(GenericTypeReflector.toCanonical(key), remappingFunction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return inner.merge(GenericTypeReflector.toCanonical(key), value, remappingFunction);
    }
}
