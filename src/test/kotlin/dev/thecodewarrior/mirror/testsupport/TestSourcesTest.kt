package dev.thecodewarrior.mirror.testsupport

import dev.thecodewarrior.mirror.joor.CompileException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestSourcesTest {
    @Test
    fun gettingClass_withoutCompiling_shouldThrowIllegalStateException() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        assertThrows<IllegalStateException> {
            val resolved = X
        }
    }

    @Test
    fun gettingClass_afterCompiling_shouldReturnClass() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        sources.compile()
        assertInstanceOf<Class<*>>(X)
    }

    @Test
    fun compiling_withSyntaxError_shouldThrow_withBuildOutput() {
        val sources = TestSources()
        val X by sources.add("X", "class X { whoops! }")
        assertThrows<CompileException> {
            sources.compile()
        }.assertCause<CompileException>().assertMessage("""
Compilation error:
/gen/X.java:2: error: <identifier> expected
class X { whoops! }
                ^
1 error

        """.trimIndent())
    }

    @Test
    fun compiling_withMissingType_shouldThrow() {
        val sources = TestSources()
        val X by sources.add("X", "class X extends Missing { }")
        assertThrows<CompileException> {
            sources.compile()
        }
    }

    @Test
    fun compiling_withMultipleTypes_shouldNotThrow() {
        val sources = TestSources()
        val X by sources.add("X", "class X { }")
        val Y by sources.add("Y", "class Y { }")
        sources.compile()
    }

    @Test
    fun compiling_withMultipleDependingTypes_shouldNotThrow() {
        val sources = TestSources()
        val X by sources.add("X", "class X { Y other; }")
        val Y by sources.add("Y", "class Y { X other; }")
        sources.compile()
    }

    @Test
    fun compiling_withTypesInPackages_shouldPutTypesInRelativePackage_andImportRootGenPackage() {
        val sources = TestSources()
        val X by sources.add("relative.X", "public class X { Y other; }")
        val Y by sources.add("Y", "import gen.relative.X; public class Y { X other; }")
        sources.compile()
    }

    @Test
    fun compiling_withGlobalImports_shouldAddImports() {
        val sources = TestSources()
        sources.globalImports.add("dev.thecodewarrior.mirror.Mirror")
        val X by sources.add("X", "class X { void method() { Class foo = Mirror.class; } }")
        sources.compile()
    }

    @Test
    fun compiling_shouldIncludeParameterNames() {
        val sources = TestSources()
//        sources.options.clear()
//        sources.options.add( "-version")
        val X by sources.add("X", "class X { void method(int named) {} }")
        sources.compile()
        assertEquals("named", X.getDeclaredMethod("method", Int::class.javaPrimitiveType).parameters[0].name)
    }

    @Test
    fun compiling_shouldIncludeTypeAnnotations() {
        val sources = TestSources()
        val A by sources.add("A", "@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.RUNTIME) @interface A {}").typed<Annotation>()
        val X by sources.add("X", "class X { @A Object method() { return null; } }")
        sources.compile()
        assertTrue(X.getDeclaredMethod("method").annotatedReturnType.isAnnotationPresent(A))
    }
}