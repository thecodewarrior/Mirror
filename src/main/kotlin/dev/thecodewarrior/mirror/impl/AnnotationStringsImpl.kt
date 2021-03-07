package dev.thecodewarrior.mirror.impl

import dev.thecodewarrior.mirror.impl.utils.MethodHandleHelper
import java.lang.IllegalArgumentException
import java.lang.reflect.Method

internal class AnnotationStringsImpl(val type: Class<out Annotation>, val cache: AnnotationStringsCache) {
    private val parameters: List<ParameterStringConverter> by lazy {
        type.declaredMethods.sortedBy { it.name }.map {
            ParameterStringConverter(it, cache)
        }
    }

    private val isValueOnly: Boolean by lazy {
        parameters.size == 1 && parameters[0].name == "value"
    }

    fun toJavaString(annotation: Annotation): String {
        var value = "@${type.canonicalName}"
        if (parameters.isEmpty())
            return value
        value += "("
        value += parameters.joinToString(", ") { it.toJavaString(annotation, isValueOnly) }
        value += ")"
        return value
    }

    fun toKotlinString(annotation: Annotation): String {
        var value = "@${type.canonicalName}"
        if (parameters.isEmpty())
            return value
        val contents = parameters.joinToString(", ") { it.toKotlinString(annotation, isValueOnly) }

        // zero-length 'value' arrays result in empty parentheses, which means they can be dropped entirely
        if(contents != "") {
            value += "($contents)"
        }

        return value
    }

    private class ParameterStringConverter(method: Method, cache: AnnotationStringsCache) {
        val name: String = method.name
        val handle: (Any, Array<Any?>) -> Any?
        val subAnnotation: AnnotationStringsImpl?

        init {
            method.isAccessible = true
            handle = MethodHandleHelper.wrapperForMethod(method)
            @Suppress("UNCHECKED_CAST")
            subAnnotation = if (method.returnType.isAnnotation) {
                cache.getStringConverter(method.returnType as Class<out Annotation>)
            } else if (method.returnType.isArray && method.returnType.componentType.isAnnotation) {
                cache.getStringConverter(method.returnType.componentType as Class<out Annotation>)
            } else {
                null
            }
        }

        fun toJavaString(instance: Any, inline: Boolean): String {
            var result = if (inline) "" else "$name="
            result += abstractValueToString(
                instance,
                { if(it.size == 1) it[0] else "{${it.joinToString(", ")}}" },
                { "${it.canonicalName}.class" },
                { subAnnotation!!.toJavaString(it) }
            )
            return result
        }

        fun toKotlinString(instance: Any, inline: Boolean): String {
            var result = if (inline) "" else "$name="
            result += abstractValueToString(
                instance,
                // a inline array can be used like a vararg, so when inline we don't use brackets
                { if(inline) it.joinToString(", ") else "[${it.joinToString(", ")}]" },
                { "${it.canonicalName}::class" },
                { subAnnotation!!.toKotlinString(it) }
            )
            return result
        }

        inline fun abstractValueToString(
            instance: Any,
            crossinline arrayFormatter: (List<String>) -> String,
            crossinline classConverter: (Class<*>) -> String,
            crossinline annotationConverter: (Annotation) -> String
        ): String {
            return when (val value = handle(instance, emptyArray())!!) {
                is Number, is Enum<*> -> value.toString()
                is String -> "\"$value\""
                is Annotation -> annotationConverter(value)
                is Class<*> -> classConverter(value)

                is ByteArray -> arrayFormatter(value.map { it.toString() })
                is ShortArray -> arrayFormatter(value.map { it.toString() })
                is CharArray -> arrayFormatter(value.map { it.toString() })
                is IntArray -> arrayFormatter(value.map { it.toString() })
                is LongArray -> arrayFormatter(value.map { it.toString() })
                is FloatArray -> arrayFormatter(value.map { it.toString() })
                is DoubleArray -> arrayFormatter(value.map { it.toString() })

                is Array<*> -> {
                    val component = value.javaClass.componentType
                    when {
                        component == String::class.java -> {
                            arrayFormatter(value.map { "\"$it\"" })
                        }
                        component.isEnum -> {
                            arrayFormatter(value.map { it.toString() })
                        }
                        component.isAnnotation -> {
                            arrayFormatter(value.map { annotationConverter(it as Annotation) })
                        }
                        component == Class::class.java -> {
                            arrayFormatter(value.map { classConverter(it as Class<*>) })
                        }
                        else -> throw IllegalArgumentException("Invalid annotation parameter value type ${value.javaClass.canonicalName}")
                    }
                }
                else -> throw IllegalArgumentException("Invalid annotation parameter value type ${value.javaClass.canonicalName}")
            }
        }
    }
}