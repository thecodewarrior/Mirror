package dev.thecodewarrior.mirror.type

import java.util.Comparator

/**
 * A comparator that compares the "specificity" of a [TypeMirror]. Essentially a more specific type can be cast to a
 * less specific type, but not the other way around. If neither type can be cast to the other or both types can be cast
 * to each other they have equal specificity.
 *
 * Precisely, `a` is more specific than `b` when `b.isAssignableFrom(a) && !a.isAssignableFrom(b)`.
 */
public object TypeSpecificityComparator: Comparator<TypeMirror> {
    override fun compare(o1: TypeMirror, o2: TypeMirror): Int {
        if(o1 == o2) return 0
        val assignable = o1.isAssignableFrom(o2)
        val reverseAssignable = o2.isAssignableFrom(o1)

        if(assignable == reverseAssignable)
            return 0

        return when {
            assignable  -> -1
            reverseAssignable  -> 1
            else -> 0
        }
    }
}