package dev.thecodewarrior.mirror

import dev.thecodewarrior.mirror.coretypes.AnnotationFormatException
import dev.thecodewarrior.mirror.testsupport.AnnotationWithParameter
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.type.ClassMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import kotlin.streams.asStream

internal object StressTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val classes = javaClass.classLoader.getResourceAsStream("allclasses.txt").bufferedReader().lineSequence()
        println("starting")
        val start = System.currentTimeMillis()
        classes.forEach {
            try {
                loadLazies(loadClass(it))
            } catch (e: IllegalAccessException) {
                // skip
            } catch (e: ClassNotFoundException) {
                // skip
            } catch (e: Exception) {
                System.err.println("Error loading $it")
                e.printStackTrace()
            }
        }
        val end = System.currentTimeMillis()
        val delta = end - start

        val millis = delta % 1_000
        val seconds = (delta/1_000) % 60
        val minutes = (delta/60_000) % 60
        println("Finished in ${minutes}m ${seconds}s ${millis}ms")
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
