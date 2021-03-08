package dev.thecodewarrior.mirror.impl

import dev.thecodewarrior.mirror.impl.utils.MethodHandleHelper
import java.lang.StringBuilder
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
        if (contents != "") {
            value += "($contents)"
        }

        return value
    }

    private class ParameterStringConverter(method: Method, cache: AnnotationStringsCache) {
        val name: String = method.name
        val handle: (Any, Array<Any?>) -> Any?
        val subAnnotation: AnnotationStringsImpl?
        val enumTypeName: String

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
            enumTypeName = if(method.returnType.isEnum) {
                generateSequence(method.returnType) { it.enclosingClass }
                    .toList().asReversed()
                    .joinToString(".") { it.simpleName }
            } else {
                ""
            }
        }

        val Class<*>.enumType: Class<*>
            get() {
                return when {
                    this.isEnum -> this
                    this.superclass?.isEnum == true -> this.superclass
                    else -> this
                }
            }

        fun toJavaString(instance: Any, inline: Boolean): String {
            var result = if (inline) "" else "$name="
            result += abstractValueToString(
                instance,
                charEscaper = { escapeJavaChar(it) },
                stringEscaper = { escapeJavaString(it) },
                // one-element arrays don't need brackets
                arrayFormatter = { if (it.size == 1) it[0] else "{${it.joinToString(", ")}}" },
                enumFormatter = { enumTypeName + "." + it.name },
                classConverter = { "${it.canonicalName}.class" },
                annotationConverter = { subAnnotation!!.toJavaString(it) }
            )
            return result
        }

        fun toKotlinString(instance: Any, inline: Boolean): String {
            var result = if (inline) "" else "$name="
            result += abstractValueToString(
                instance,
                charEscaper = { escapeKotlinChar(it) },
                stringEscaper = { escapeKotlinString(it) },
                // an inline array is treated like a vararg, so it doesn't use brackets
                arrayFormatter = { if (inline) it.joinToString(", ") else "[${it.joinToString(", ")}]" },
                enumFormatter = { enumTypeName + "." + it.name },
                classConverter = { "${it.canonicalName}::class" },
                annotationConverter = { subAnnotation!!.toKotlinString(it) }
            )
            return result
        }

        inline fun abstractValueToString(
            instance: Any,
            crossinline charEscaper: (Char) -> String,
            crossinline stringEscaper: (String) -> String,
            crossinline arrayFormatter: (List<String>) -> String,
            crossinline enumFormatter: (Enum<*>) -> String,
            crossinline classConverter: (Class<*>) -> String,
            crossinline annotationConverter: (Annotation) -> String
        ): String {
            return when (val value = handle(instance, emptyArray())!!) {
                is Char -> "'${charEscaper(value)}'"
                is Number -> value.toString()
                is Enum<*> -> enumFormatter(value)
                is String -> "\"${stringEscaper(value)}\""
                is Annotation -> annotationConverter(value)
                is Class<*> -> classConverter(value)

                is ByteArray -> arrayFormatter(value.map { it.toString() })
                is ShortArray -> arrayFormatter(value.map { it.toString() })
                is CharArray -> arrayFormatter(value.map { "'${charEscaper(it)}'" })
                is IntArray -> arrayFormatter(value.map { it.toString() })
                is LongArray -> arrayFormatter(value.map { it.toString() })
                is FloatArray -> arrayFormatter(value.map { it.toString() })
                is DoubleArray -> arrayFormatter(value.map { it.toString() })

                is Array<*> -> {
                    val component = value.javaClass.componentType
                    when {
                        component == String::class.java -> {
                            arrayFormatter(value.map { "\"${stringEscaper(it as String)}\"" })
                        }
                        component.isEnum -> {
                            arrayFormatter(value.map { enumFormatter(it as Enum<*>) })
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

        companion object {
            /**
             * https://docs.oracle.com/javase/specs/jls/se15/html/jls-3.html#jls-EscapeSequence
             */
            val baseJavaEscapeSequences = mapOf(
                '\u0008'.toInt() to "\\b",
                // '\u0020'.toInt() to "\\s", // literally " ", why the fuck is there an escape sequence for this?
                '\u0009'.toInt() to "\\t",
                '\u000A'.toInt() to "\\n",
                '\u000D'.toInt() to "\\r",
                '\u000C'.toInt() to "\\f",
                '\"'.toInt() to "\\\"",
                '\''.toInt() to "\\'",
                '\\'.toInt() to "\\\\",
            )

            /**
             * https://kotlinlang.org/spec/syntax-and-grammar.html#grammar-rule-EscapedIdentifier
             */
            val baseKotlinEscapeSequences = mapOf(
                '\u0009'.toInt() to "\\t",
                '\u0008'.toInt() to "\\b",
                '\u000D'.toInt() to "\\r",
                '\u000A'.toInt() to "\\n",
                '\''.toInt() to "\\'",
                '\"'.toInt() to "\\\"",
                '\\'.toInt() to "\\\\",
                '$'.toInt() to "\\$"
            )

            // single quotes don't need to be escaped in strings
            val javaStringEscapeSequences = baseJavaEscapeSequences - setOf('\''.toInt())
            val kotlinStringEscapeSequences = baseKotlinEscapeSequences - setOf('\''.toInt())

            // double quotes don't need to be escaped in chars
            val javaCharEscapeSequences = baseJavaEscapeSequences - setOf('"'.toInt())

            // kotlin doesn't have to escape `$` in character literals either
            val kotlinCharEscapeSequences = baseKotlinEscapeSequences - setOf('"'.toInt(), '$'.toInt())

            fun escapeJavaString(raw: String): String {
                return escapeString(raw, javaStringEscapeSequences)
            }

            fun escapeKotlinString(raw: String): String {
                return escapeString(raw, kotlinStringEscapeSequences)
            }

            fun escapeString(raw: String, escapeSequences: Map<Int, String>): String {
                val escaped = StringBuilder(raw.length)

                // codepoint iteration courtesy of https://stackoverflow.com/a/1527891/1541907
                val length: Int = raw.length
                var offset = 0
                while (offset < length) {
                    val codepoint: Int = raw.codePointAt(offset)

                    escapeCodepoint(codepoint, escaped, escapeSequences)

                    offset += Character.charCount(codepoint)
                }

                return escaped.toString()
            }

            /**
             * Escape a full unicode code point, as opposed to [escapeChar], which doesn't have to handle surrogate
             * pairs
             */
            fun escapeCodepoint(codepoint: Int, escaped: StringBuilder, escapeSequences: Map<Int, String>) {
                val characterEscape = escapeSequences[codepoint]
                if (characterEscape != null) {
                    escaped.append(characterEscape)
                    return
                }

                if (codepoint in 0x20 until 0x7f) {
                    escaped.append(codepoint.toChar())
                    return
                }

                if (codepoint > 0xffff) {
                    val surrogatePair = Character.toChars(codepoint)
                    escaped.append(charHex(surrogatePair[0]))
                    escaped.append(charHex(surrogatePair[1]))
                } else {
                    escaped.append(charHex(codepoint.toChar()))
                }
            }

            fun escapeJavaChar(raw: Char): String {
                return escapeChar(raw, javaCharEscapeSequences)
            }

            fun escapeKotlinChar(raw: Char): String {
                return escapeChar(raw, kotlinCharEscapeSequences)
            }

            fun escapeChar(raw: Char, escapeSequences: Map<Int, String>): String {
                val characterEscape = escapeSequences[raw.toInt()]
                if (characterEscape != null) {
                    return characterEscape
                }

                if (raw in '\u0020' until '\u007f') {
                    return "$raw"
                }

                return charHex(raw)
            }

            fun charHex(char: Char): String {
                return "\\u%04X".format(char.toInt())
            }
        }
    }
}