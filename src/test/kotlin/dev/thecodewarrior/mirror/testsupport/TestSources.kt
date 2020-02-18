package dev.thecodewarrior.mirror.testsupport

import dev.thecodewarrior.mirror.joor.Compile
import dev.thecodewarrior.mirror.joor.CompileOptions
import kotlin.reflect.KProperty

/**
 *
 */
class TestSources {
    private val sources = mutableMapOf<String, String>()
    var options: CompileOptions = CompileOptions()
    var classLoader: Compile.RuntimeClassLoader? = null

    /**
     * Adds the passed class to this compiler. This method automatically prepends the necessary `package` declaration
     * to the passed string and adds a wildcard import for the "root" package, `gen`, if needed.
     *
     * @param name The qualified name relative to the "root" package (`gen`)
     * @param code The code to compile into that class
     * @return A property delegate to access the test class once [compile] has been called
     */
    fun add(name: String, code: String): TestClass {
        if("gen.$name" in sources)
            throw IllegalArgumentException("Class name $name already exists")

        sources["gen.$name"] = if(name.contains('.'))
            "package gen.${name.substringBeforeLast('.')};import gen.*;\n$code"
        else
            "package gen;\n$code"

        return TestClass("gen.$name")
    }

    fun compile() {
        this.classLoader = Compile.compile(sources, options)
    }

    inner class TestClass(val name: String) {

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Class<*> {
            if(classLoader == null)
                throw ClassNotFoundException("Sources not compiled yet")
            return Class.forName(name, true, classLoader)
        }
    }
}

/**
 * The result of a [TestSources]
 */
class TestClasses() {

}