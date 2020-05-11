package dev.thecodewarrior.mirror.utils

import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Method

private fun executableSortHash(executable: Executable): Int {
    var result = executable.declaringClass.name.hashCode()
    result = 31 * result + executable.name.hashCode()
    executable.parameterTypes.forEach {
        result = 31 * result + it.name.hashCode()
    }
    if(executable is Method)
        result = 31 * result + executable.returnType.name.hashCode()
    return result
}

private fun fieldSortHash(field: Field): Int {
    var result = field.declaringClass.name.hashCode()
    result = 31 * result + field.name.hashCode()
    return result
}

internal fun stableSort(methods: Array<Method>): List<Method> {
    return methods.sortedBy { executableSortHash(it) }
}

internal fun stableSort(constructors: Array<Constructor<*>>): List<Constructor<*>> {
    return constructors.sortedBy { executableSortHash(it) }
}

internal fun stableSort(fields: Array<Field>): List<Field> {
    return fields.sortedBy { fieldSortHash(it) }
}
