package dev.thecodewarrior.mirror.util

import dev.thecodewarrior.mirror.impl.util.StableSortImpl
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Some utility functions used by Mirror that are more widely applicable.
 */
public object MirrorUtils {

    /**
     * Sorts the given methods into a consistent and stable order. This method sorts the array in place.
     *
     * Java Core Reflection doesn't return class members in any particular order, so that order may change between
     * versions or VMs. This method uses a custom comparison function to ensure the order is stable between versions
     * and VMs.
     *
     * @param methods The array of methods to sort
     * @return The input array
     */
    public fun stableSort(methods: Array<Method>): Array<Method> {
        StableSortImpl.stableSort(methods)
        return methods
    }

    /**
     * Sorts the given constructors into a consistent and stable order. This method sorts the array in place.
     *
     * Java Core Reflection doesn't return class members in any particular order, so that order may change between
     * versions or VMs. This method uses a custom comparison function to ensure the order is stable between versions
     * and VMs.
     *
     * @param constructors The array of constructors to sort
     * @return The input array
     */
    public fun stableSort(constructors: Array<Constructor<*>>): Array<Constructor<*>> {
        StableSortImpl.stableSort(constructors)
        return constructors
    }

    /**
     * Sorts the given fields into a consistent and stable order. This method sorts the array in place.
     *
     * Java Core Reflection doesn't return class members in any particular order, so that order may change between
     * versions or VMs. This method uses a custom comparison function to ensure the order is stable between versions
     * and VMs.
     *
     * @param fields The array of fields to sort
     * @return The input array
     */
    public fun stableSort(fields: Array<Field>): Array<Field> {
        StableSortImpl.stableSort(fields)
        return fields
    }

    /**
     * Sorts the given classes into a consistent and stable order. This method sorts the array in place.
     *
     * Java Core Reflection doesn't return class members in any particular order, so that order may change between
     * versions or VMs. This method uses a custom comparison function to ensure the order is stable between versions
     * and VMs.
     *
     * @param classes The array of classes to sort
     * @return The input array
     */
    public fun stableSort(classes: Array<Class<*>>): Array<Class<*>> {
        StableSortImpl.stableSort(classes)
        return classes
    }
}
