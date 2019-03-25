package com.teamwizardry.mirror.coretypes;

import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;



/**
 * A {@link Set} implementation suited for maintaining {@link AnnotatedType} elements.
 * The standard sets do not usually suffice as {@link AnnotatedType} implements neither {@code equals} nor {@code hashCode}.
 * This implementation overcomes that limitation by transparently turning each {@link AnnotatedType}
 * into the canonical form using {@link GenericTypeReflector#toCanonical(AnnotatedType)}.
 * By default, {@code AnnotatedTypeSet} instances are backed by a {@link HashSet}, but any set can be used instead.
 * The guarantees of {@code AnnotatedTypeSet} are then the same as of the set it is backed by.
 *
 * @param <E> the type of the elements maintained by this set
 *
 * @see AnnotatedTypeMap
 */
public class AnnotatedTypeSet<E extends AnnotatedType> implements Set<E> {

    private final Set<E> inner;

    public AnnotatedTypeSet() {
        this(new HashSet<>());
    }

    @SuppressWarnings("WeakerAccess")
    public AnnotatedTypeSet(Set<E> inner) {
        this.inner = inner;
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof AnnotatedType && inner.contains(GenericTypeReflector.toCanonical((AnnotatedType) o));
    }

    @Override
    public Iterator<E> iterator() {
        return inner.iterator();
    }

    @Override
    public Object[] toArray() {
        return inner.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return inner.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return inner.add(GenericTypeReflector.toCanonical(e));
    }

    @Override
    public boolean remove(Object o) {
        return o instanceof AnnotatedType && inner.remove(GenericTypeReflector.toCanonical((AnnotatedType) o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return inner.containsAll(canonical(c));
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return inner.addAll(c.stream()
                .map(GenericTypeReflector::toCanonical)
                .collect(Collectors.toList()));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return inner.retainAll(canonical(c));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return inner.removeAll(canonical(c));
    }

    @Override
    public void clear() {
        inner.clear();
    }

    private Collection<?> canonical(Collection<?> c) {
        return c.stream()
                .map(e -> e instanceof AnnotatedType ? GenericTypeReflector.toCanonical((AnnotatedType) e) : e)
                .collect(Collectors.toList());
    }
}
