package dev.thecodewarrior.mirror.util

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.AssertionUtils
import dev.thecodewarrior.mirror.testsupport.MTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.fail

@Suppress("LocalVariableName")
internal class AnnotationToStringTest : MTest() {

    /**
     * Tests the Java and Kotlin `annotationTo*String` methods. Returns a callback suitable for use in [assertAll].
     *
     * Yes, this method has the actual value first and the expected values after. I can just write the method arguments
     * more cleanly by doing it this way, and they are very obviously different types so it isn't ambiguous either.
     */
    private fun assertToString(annotation: Annotation, java: String, kotlin: String): () -> Unit {
        return {
            val toJava = Mirror.annotationToJavaString(annotation)
            val toKotlin = Mirror.annotationToKotlinString(annotation)
            val errors = listOfNotNull(
                if (toJava == java) null else AssertionUtils.format(null, java, toJava, "Java string"),
                if (toKotlin == kotlin) null else AssertionUtils.format(null, kotlin, toKotlin, "Kotlin string"),
            )
            if (errors.isNotEmpty()) {
                fail(errors.joinToString("\n"), null)
            }
        }
    }

    @Test
    fun `'annotationToString' for an empty annotation should not include parentheses`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A {}")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"@A X"
        }
        sources.compile()

        assertAll(
            assertToString(
                types["@A X"].annotations[0],
                java = "@gen.A",
                kotlin = "@gen.A"
            ),
        )
    }

    @Test
    fun `'annotationToString' with a single 'value' parameter should not include its name`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { int value(); }")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"@A(10) X"
        }
        sources.compile()

        assertAll(
            assertToString(
                types["@A(10) X"].annotations[0],
                java = "@gen.A(10)",
                kotlin = "@gen.A(10)"
            )
        )
    }

    @Test
    fun `'annotationToString' with a single non-'value' parameter should include its name`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { int param(); }")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"@A(param=10) X"
        }
        sources.compile()

        assertAll(
            assertToString(
                types["@A(param=10) X"].annotations[0],
                java = "@gen.A(param=10)",
                kotlin = "@gen.A(param=10)"
            ),
        )
    }

    @Test
    fun `'annotationToString' with an array parameter should only use brackets when necessary`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { int[] value(); }")
        val B by sources.add("B", "@rt(TYPE_USE) @interface B { int[] array(); int extra(); }")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"@A(10) X"
            +"@A({10, 20}) X"
            +"@A({}) X"
            +"@B(array=10, extra=0) X"
            +"@B(array={10, 20}, extra=0) X"
        }
        sources.compile()

        assertAll(
            assertToString(
                types["@A(10) X"].annotations[0],
                // in Java, if you're only using one value you can exclude the brackets
                java = "@gen.A(10)",
                // in Kotlin, 'value' arrays are treated like 'vararg' parameters
                kotlin = "@gen.A(10)"
            ),
            assertToString(
                types["@A({10, 20}) X"].annotations[0],
                // in Java, if you're passing more than one element you do have to include the brackets
                java = "@gen.A({10, 20})",
                // in Kotlin, again, 'value' arrays are treated like 'vararg' parameters
                kotlin = "@gen.A(10, 20)"
            ),
            assertToString(
                types["@A({}) X"].annotations[0],
                // in Java, zero elements means you need to pass the brackets
                java = "@gen.A({})",
                // in Kotlin, zero elements means even the parentheses are optional
                kotlin = "@gen.A"
            ),
            assertToString(
                types["@B(array=10, extra=0) X"].annotations[0],
                // in Java, a single-element array can *always* be written without the brackets
                java = "@gen.B(array=10, extra=0)",
                // in Kotlin, unless it's the 'value' parameter, you *have* to write the brackets
                kotlin = "@gen.B(array=[10], extra=0)"
            ),
            assertToString(
                types["@B(array={10, 20}, extra=0) X"].annotations[0],
                // Java uses curly brackets to write arrays in annotations
                java = "@gen.B(array={10, 20}, extra=0)",
                // Kotlin uses square brackets to write arrays in annotations
                kotlin = "@gen.B(array=[10, 20], extra=0)"
            ),
        )
    }

    @Test
    fun `'annotationToString' for an annotation with multiple parameters should include them with their names`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { int value(); int extra(); }")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"@A(value=10, extra=100) X"
        }
        sources.compile()

        assertAll(
            assertToString(
                types["@A(value=10, extra=100) X"].annotations[0],
                java = "@gen.A(extra=100, value=10)",
                kotlin = "@gen.A(extra=100, value=10)"
            ),
            assertToString(
                types["@A(value=10, extra=100) X"].annotations[0],
                java = "@gen.A(extra=100, value=10)",
                kotlin = "@gen.A(extra=100, value=10)"
            ),
        )
    }

    @Test
    fun `'annotationToJavaString' for should include default values`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { int value(); int optional() default 5; }")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"@A(10) X"
        }
        sources.compile()

        assertEquals("@gen.A(optional=5, value=10)", Mirror.annotationToJavaString(types["@A(10) X"].annotations[0]))
    }
}