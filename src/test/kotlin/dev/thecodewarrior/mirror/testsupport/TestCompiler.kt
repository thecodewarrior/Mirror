package dev.thecodewarrior.mirror.testsupport

import dev.thecodewarrior.mirror.joor.Compile
import dev.thecodewarrior.mirror.joor.CompileOptions
import kotlin.reflect.KProperty

/**
 *
 */
class TestCompiler {
    private val classes = mutableMapOf<String, String>()
    var options: CompileOptions = CompileOptions()

    /**
     * Adds the passed class to this compiler. This method automatically prepends the necessary `package` declaration
     * to the passed string and adds a wildcard import for the "root" package, `gen`, if needed.
     *
     * @param name The qualified name relative to the "root" package (`gen`)
     * @param code The code to compile into that class
     * @return this compiler, to facilitate chaining calls
     */
    fun add(name: String, code: String): TestCompiler {
        if("gen.$name" in classes)
            throw IllegalArgumentException("Class name $name already exists")

        classes["gen.$name"] = if(name.contains('.'))
            "package gen.${name.substringBeforeLast('.')};import gen.*;\n$code"
        else
            "package gen;\n$code"

        return this
    }

    /**
     * Compiles the collection of class files into a final set of classes. The returned set of classes will be
     * accessible both under their true package (relative to the `gen` package) and under their relative name
     */
    fun compile(): TestClasses {
        if(classes.isEmpty())
            return TestClasses(null)
        else
            return TestClasses(Compile.compile(classes, options))
    }

}

/**
 * The result of a [TestCompiler]
 */
class TestClasses(val classLoader: Compile.RuntimeClassLoader?) {

    operator fun get(name: String): Class<*> {
        if(classLoader == null)
            throw ClassNotFoundException("gen.$name")
        return Class.forName("gen.$name", true, classLoader)
    }
}