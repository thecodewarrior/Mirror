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

/**
 * The descriptor of the class, as used by the Java Virtual Machine
 */
internal val Class<*>.jvmName: String
    get() = when(this) {
        Byte::class.javaPrimitiveType -> "B"
        Char::class.javaPrimitiveType -> "C"
        Double::class.javaPrimitiveType -> "D"
        Float::class.javaPrimitiveType -> "F"
        Int::class.javaPrimitiveType -> "I"
        Long::class.javaPrimitiveType -> "J"
        Short::class.javaPrimitiveType -> "S"
        Boolean::class.javaPrimitiveType -> "Z"
        else -> "L${this.name.replace(".", "/")};"
    }
