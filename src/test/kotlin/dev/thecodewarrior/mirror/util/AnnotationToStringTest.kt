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
    fun `'annotationToString' for an annotation with multiple parameters should include them with their names in alphabetical order`() {
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

    @Test
    fun `'annotationToString' with class parameters should use the appropriate syntax`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { Class<?> value(); }")
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"@A(Y.class) X"
        }
        sources.compile()

        assertAll(
            assertToString(
                types["@A(Y.class) X"].annotations[0],
                java = "@gen.A(gen.Y.class)",
                kotlin = "@gen.A(gen.Y::class)"
            )
        )
    }

    @Test
    fun `'annotationToString' with nested annotation parameters should use the appropriate syntax`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { B value(); }")
        val B by sources.add("B", "@rt(TYPE_USE) @interface B { int value(); }")
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y {}")
        val types = sources.types {
            +"@A(@B(10)) X"
        }
        sources.compile()

        assertAll(
            assertToString(
                types["@A(@B(10)) X"].annotations[0],
                java = "@gen.A(@gen.B(10))",
                kotlin = "@gen.A(@gen.B(10))"
            )
        )
    }

    @Test
    fun `'annotationToString' with string parameters should correctly escape them`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { String value(); }")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"""@A("") X"""
            +"""@A("plain") X"""
            +"""@A("\b\t\n\r\f\"'\\$") X"""
            // 0 = NUL, 1B = ESC, 20 = space, 30 = '0', 7E = '~', 7F = DEL, 80 = <Control>
            +"""@A("\u0000\u001B\u0020\u0030\u007E\u007F\u0080") X"""
        }
        sources.compile()

        assertAll(
            assertToString(
                types["""@A("") X"""].annotations[0],
                java = """@gen.A("")""",
                kotlin = """@gen.A("")"""
            ),
            assertToString(
                types["""@A("plain") X"""].annotations[0],
                java = """@gen.A("plain")""",
                kotlin = """@gen.A("plain")"""
            ),
            assertToString(
                types["""@A("\b\t\n\r\f\"'\\$") X"""].annotations[0],
                // java has all these escapes and doesn't escape the dollar sign
                // the single quote is also not escaped because we're in a double quote string
                java = """@gen.A("\b\t\n\r\f\"'\\$")""",
                // kotlin doesn't have \f and escapes the dollar sign
                kotlin = """@gen.A("\b\t\n\r\u000C\"'\\\$")"""
            ),
            // anything outside the 20..7F range gets encoded, anything in there is used directly
            assertToString(
                // 0 = NUL, 1B = ESC, 20 = space, 30 = '0', 7E = '~', 7F = DEL, 80 = <Control>
                types["""@A("\u0000\u001B\u0020\u0030\u007E\u007F\u0080") X"""].annotations[0],
                java = """@gen.A("\u0000\u001B 0~\u007F\u0080")""",
                kotlin = """@gen.A("\u0000\u001B 0~\u007F\u0080")"""
            ),
        )
    }

    @Test
    fun `'annotationToString' with char parameters should correctly escape them`() {
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { char value(); }")
        val B by sources.add(
            "B", "@rt(TYPE_USE) @interface B { " +
                    "char a(); char b(); char c(); " +
                    "char d(); char e(); char f(); " +
                    "char g(); char h(); char i(); " +
                    "}"
        )
        val C by sources.add(
            "C", "@rt(TYPE_USE) @interface C { " +
                    "char a(); char b(); char c(); " +
                    "char d(); char e(); char f(); " +
                    "char g(); " +
                    "}"
        )
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"""@A('a') X"""
            +"""@B(a='\b', b='\t', c='\n', d='\r', e='\f', f='"', g='\'', h='\\', i='$') X"""
            // 0 = NUL, 1B = ESC, 20 = space, 30 = '0', 7E = '~', 7F = DEL, 80 = <Control>
            +"""@C(a='\u0000', b='\u001B', c='\u0020', d='\u0030', e='\u007E', f='\u007F', g='\u0080') X"""
        }
        sources.compile()

        assertAll(
            assertToString(
                types["""@A('a') X"""].annotations[0],
                java = """@gen.A('a')""",
                kotlin = """@gen.A('a')"""
            ),
            assertToString(
                types["""@B(a='\b', b='\t', c='\n', d='\r', e='\f', f='"', g='\'', h='\\', i='$') X"""].annotations[0],
                // java has all these escapes and doesn't escape the dollar sign
                // the single quote is also not escaped because we're in a double quote string
                java = """@gen.B(a='\b', b='\t', c='\n', d='\r', e='\f', f='"', g='\'', h='\\', i='$')""",
                // kotlin doesn't have \f and escapes the dollar sign
                kotlin = """@gen.B(a='\b', b='\t', c='\n', d='\r', e='\u000C', f='"', g='\'', h='\\', i='$')"""
            ),
            // anything outside the 20..7F range gets encoded, anything in there is used directly
            assertToString(
                // 0 = NUL, 1B = ESC, 20 = space, 30 = '0', 7E = '~', 7F = DEL, 80 = <Control>
                types["""@C(a='\u0000', b='\u001B', c='\u0020', d='\u0030', e='\u007E', f='\u007F', g='\u0080') X"""].annotations[0],
                java = """@gen.C(a='\u0000', b='\u001B', c=' ', d='0', e='~', f='\u007F', g='\u0080')""",
                kotlin = """@gen.C(a='\u0000', b='\u001B', c=' ', d='0', e='~', f='\u007F', g='\u0080')"""
            ),
        )
    }

    @Test
    fun `'annotationToString' with enum parameters should use the appropriate syntax`() {
        val E by sources.add("E", "enum E { PLAIN, SUBCLASS { int field; }; }")
        val A by sources.add("A", "@rt(TYPE_USE) @interface A { E value(); }")
        val B by sources.add("B", "@rt(TYPE_USE) @interface B { Inner value(); enum Inner { A; }}")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"@A(E.PLAIN) X"
            +"@A(E.SUBCLASS) X"
            +"@B(B.Inner.A) X"
        }
        sources.compile()

        assertAll(
            // we don't include the package, for brevity. The type is explicit in the annotation
            assertToString(
                types["@A(E.PLAIN) X"].annotations[0],
                java = "@gen.A(E.PLAIN)",
                kotlin = "@gen.A(E.PLAIN)"
            ),
            assertToString(
                types["@A(E.SUBCLASS) X"].annotations[0],
                java = "@gen.A(E.SUBCLASS)",
                kotlin = "@gen.A(E.SUBCLASS)"
            ),
            // but we should include enclosing classes
            assertToString(
                types["@B(B.Inner.A) X"].annotations[0],
                java = "@gen.B(B.Inner.A)",
                kotlin = "@gen.B(B.Inner.A)"
            ),
        )
    }
}