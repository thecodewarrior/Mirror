package dev.thecodewarrior.mirror

import dev.thecodewarrior.mirror.coretypes.AnnotationFormatException
import dev.thecodewarrior.mirror.member.ExecutableMirror
import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.ParameterMirror
import dev.thecodewarrior.mirror.testsupport.AnnotationWithParameter
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.streams.asStream

internal object StressTest: MTest() { // extending MTest for its helpers, not for the JUnit functionality
    @JvmStatic
    fun main(args: Array<String>) {
        val classes = javaClass.classLoader.getResourceAsStream("allclasses.txt").bufferedReader().lineSequence().toList()
        val times = LongArray(classes.size)
        println("starting")
        val start = System.currentTimeMillis()
        classes.forEachIndexed { i, name ->
            try {
                val classStart = System.nanoTime()
                loadLazies(loadClass(name))
                val classEnd = System.nanoTime()
                times[i] = classEnd - classStart
            } catch (e: IllegalAccessException) {
                // skip
            } catch (e: ClassNotFoundException) {
                // skip
            } catch (e: Exception) {
                System.err.println("Error loading $name")
                e.printStackTrace()
            }
        }
        val end = System.currentTimeMillis()
        val delta = end - start


        val millis = delta % 1_000
        val seconds = (delta/1_000) % 60
        val minutes = (delta/60_000) % 60
        println("Finished in ${minutes}m ${seconds}s ${millis}ms")
        println("Directly loaded ${classes.size} classes (average of ${delta.toDouble() / classes.size}ms per class)")

        val mirrorCache = Mirror._get<MirrorCache>("cache")
        cacheStats<TypeMirror>(mirrorCache.types, "type", "types")
        cacheStats<ExecutableMirror>(mirrorCache.executables, "executable", "executables")
        cacheStats<FieldMirror>(mirrorCache.fields, "field", "fields")
        cacheStats<ParameterMirror>(mirrorCache.parameters, "parameter", "parameters") {
            "${it.declaringExecutable} > ${it.index}"
        }
    }

    fun <T> cacheStats(cache: Any, kind: String, kinds: String, stringify: (T) -> String = { "$it" }) {
        val rawCache = cache._get<Map<*, T>>("rawCache")
        val specializedCache = cache._get<Map<Pair<T, *>, *>>("specializedCache")

        val specializedCount = mutableMapOf<T, Int>()
        specializedCache.forEach { (key, _), _ ->
            specializedCount[key] = specializedCount.getOrDefault(key, 0) + 1
        }
        val sorted = specializedCount.entries.sortedByDescending { it.value }

        println()
        println("Cached $kinds:")
        println("- raw $kinds: ${rawCache.size}")
        println("- total specialized $kinds: ${specializedCount.size}")
        println("- total $kind specializations: ${specializedCache.size}")
        println("- most specialized $kinds:")
        sorted.take(5).forEach { (value, count) ->
            println(" - $count * ${stringify(value)}")
        }

        File("$kind-specializations.csv").printWriter().use { w ->
            sorted.forEach { (value, count) ->
                w.println("$count, ${stringify(value)}")
            }
        }
    }

    fun loadClass(name: String): ClassMirror {
        return Mirror.reflectClass(Class.forName(name, false, javaClass.classLoader))
    }

    fun loadLazies(mirror: ClassMirror) {
        mirror.superclass
        mirror.interfaces
        mirror.typeParameters
        mirror.enclosingClass
        mirror.enclosingExecutable
        mirror.enumType
        (mirror.declaredMethods +
            mirror.inheritedMethods +
            mirror.publicMethods +
            mirror.visibleMethods +
            mirror.methods
            ).forEach {
            it.overrides
            it.parameterTypes
            it.returnType
        }
        (mirror.declaredFields +
            mirror.publicFields +
            mirror.fields
            ).forEach {
            it.type
        }
        (mirror.declaredConstructors + mirror.publicConstructors).forEach {
            it.parameterTypes
            it.returnType
        }
        mirror.declaredMemberClasses
        mirror.memberClasses
        mirror.publicMemberClasses
    }
}
