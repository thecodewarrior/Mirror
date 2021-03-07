package dev.thecodewarrior.mirror.impl.util

import dev.thecodewarrior.mirror.impl.utils.ComparatorChain
import dev.thecodewarrior.mirror.impl.utils.HashChain
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * The comparators here sort first by a hash value, because there is no order guaranteed by the interfaces, and we
 * don't want to mislead by making them happen to be alphabetical or something.
 */
internal object StableSortImpl {

    fun stableSort(constructors: Array<Constructor<*>>) {
        constructors.sortWith(StableConstructorComparator)
    }

    fun stableSort(methods: Array<Method>) {
        methods.sortWith(StableMethodComparator)
    }

    fun stableSort(fields: Array<Field>) {
        fields.sortWith(StableFieldComparator)
    }

    fun stableSort(classes: Array<Class<*>>) {
        classes.sortWith(StableClassComparator)
    }

    private object StableConstructorComparator : Comparator<Constructor<*>> {
        override fun compare(o1: Constructor<*>?, o2: Constructor<*>?): Int {
            if (o1 == null || o2 == null)
                return (o1 == null).compareTo(o2 == null)
            return ComparatorChain()
                .chain(o1, o2) { it.declaringClass.typeName }
                .chainEach(o1.parameterTypes, o2.parameterTypes) { it.typeName }
                .finish()
        }
    }

    private object StableMethodComparator : Comparator<Method> {
        override fun compare(o1: Method?, o2: Method?): Int {
            if (o1 == null || o2 == null)
                return (o1 == null).compareTo(o2 == null)
            return ComparatorChain()
                .chain(o1, o2) { it.declaringClass.typeName }
                .chain(o1, o2) { it.name }
                .chainEach(o1.parameterTypes, o2.parameterTypes) { it.typeName }
                .chain(o1, o2) { it.returnType.typeName }
                .finish()
        }
    }

    private object StableFieldComparator : Comparator<Field> {
        override fun compare(o1: Field?, o2: Field?): Int {
            if (o1 == null || o2 == null)
                return (o1 == null).compareTo(o2 == null)

            return ComparatorChain()
                .chain(o1, o2) { it.declaringClass.typeName }
                .chain(o1, o2) { it.name }
                .chain(o1, o2) { it.type.typeName }
                .finish()
        }
    }

    private object StableClassComparator : Comparator<Class<*>> {
        override fun compare(o1: Class<*>?, o2: Class<*>?): Int {
            if (o1 == null || o2 == null)
                return (o1 == null).compareTo(o2 == null)
            return ComparatorChain()
                .chain(o1, o2) { it.typeName }
                .finish()
        }
    }
}
