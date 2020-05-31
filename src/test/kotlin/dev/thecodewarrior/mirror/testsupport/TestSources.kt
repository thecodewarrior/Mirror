package dev.thecodewarrior.mirror.testsupport

import dev.thecodewarrior.mirror.joor.Compile
import dev.thecodewarrior.mirror.joor.CompileOptions
import org.intellij.lang.annotations.Language
import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.IllegalStateException
import kotlin.reflect.KProperty

/**
 * Runtime compilation of test cases.
 *
 * ## Basic usage
 * ```kotlin
 * val sources = TestSources()
 * val X: Class<*> by sources.add("X", "class X {}")
 * val A: Class<Annotation> by sources.add("A", "@interface A {}")
 * val types = sources.types {
 *     +"? extends X"
 *     block("T") {
 *         +"T"
 *     }
 * }
 * sources.compile()
 *
 * types["? extends X"]
 * types["T"]
 * ```
 */
class TestSources {

    private val javaSources = mutableMapOf<String, String>()
    private val typeSets = mutableListOf<TypeSet>()
    var options: MutableList<String> = mutableListOf()
    var classLoader: ClassLoader? = null

    init {
        options.add("-parameters")
    }

    /**
     * Imports to be added to all files. Set these _before_ adding the files using [add]. Defaults to a set of generally
     * useful or commonly used types.
     */
    var globalImports: MutableList<String> = mutableListOf(
        "java.util.*",
        "java.lang.annotation.ElementType",
        "java.lang.annotation.Target",
        "java.lang.annotation.Retention",
        "java.lang.annotation.RetentionPolicy",
        "dev.thecodewarrior.mirror.testsupport.TestSourceNopException"
    )

    /**
     * Adds the passed class to this compiler. This method automatically prepends the necessary `package` declaration
     * to the passed string and adds a wildcard import for the "root" package, `gen`, if needed.
     *
     * ### Expansions
     * This method will perform these expansions in the source code:
     * - Any occurrences of `@rt(targets)` in the text will be replaced with the annotations for runtime annotation
     *   retention and the passed element targets.
     * - Any occurrences of `NOP;` in the text will be replaced with a throw statement
     *
     * ### Useful reference
     * - Annotation: `@Retention(RetentionPolicy.RUNTIME) @Target(ElementType...) @interface A { int value(); }`
     *   - ElementTypes: `TYPE`, `FIELD`, `METHOD`, `PARAMETER`, `CONSTRUCTOR`, `LOCAL_VARIABLE`, `ANNOTATION_TYPE`,
     *     `PACKAGE`, `TYPE_PARAMETER`, `TYPE_USE`
     *
     * @param name The qualified name relative to the "root" package (`gen`)
     * @param code The code to compile into that class
     * @return A property delegate to access the test class once [compile] has been called
     */
    fun add(name: String, @Language("java") code: String, trimIndent: Boolean = true): TestClass<*> {
        requireNotCompiled()
        if("gen.$name" in javaSources)
            throw IllegalArgumentException("Class name $name already exists")

        var fullSource = ""
        if(name.contains('.'))
            fullSource += "package gen.${name.substringBeforeLast('.')};import gen.*;"
        else
            fullSource += "package gen;"

        fullSource += globalImports.joinToString("") { "import $it;" }
        fullSource += "\n"
        var processedCode = if(trimIndent) code.trimIndent() else code
        processedCode = processedCode.replace("""@rt\((\w+(?:,\s*\w+)*)\)""".toRegex()) { match ->
            val types = match.groupValues[1].split(",").joinToString(", ") { "ElementType.${it.trim()}" }
            "@Retention(RetentionPolicy.RUNTIME) @Target({ $types })"
        }
        processedCode = processedCode.replace("NOP;", "throw new TestSourceNopException();")
        fullSource += processedCode

        javaSources["gen.$name"] = fullSource

        return TestClass<Any>("gen.$name")
    }

    fun types(packageName: String? = null, block: TypeBlock.() -> Unit): TypeSet {
        requireNotCompiled()
        val builder = TypeSetDefinition()
        builder.rootBlock.block()
        val set = TypeSet(packageName?.let { "gen.$it" } ?: "gen", "__Types_${typeSets.size}", builder)
        typeSets.add(set)
        return set
    }

    fun compile() {
        requireNotCompiled()
        this.classLoader = Compile.compile(javaSources + typeSets.associate { set ->
            set.fullClassName to set.classText
        }, CompileOptions().options(options))
    }

    fun getClass(name: String): Class<*> {
        return Class.forName(name, true, requireCompiled())
    }

    private fun requireNotCompiled() {
        if(classLoader != null)
            throw IllegalStateException("The sources have already been compiled")
    }
    private fun requireCompiled(): ClassLoader {
        return classLoader
            ?: throw IllegalStateException("The sources have not been compiled")
    }

    inner class TypeSet(val packageName: String, val className: String, private val definition: TypeSetDefinition) {
        val fullClassName: String = "$packageName.$className"
        val classText: String = createClassText()

        private var typeCache = mutableMapOf<String, AnnotatedType>()

        operator fun get(name: String): AnnotatedType {
            return typeCache.getOrPut(name) {
                definition.find(name).getType(holder)
            }
        }

        private var holderCache: Class<*>? = null
        val holder: Class<*>
            get() {
                holderCache?.also { return it }
                return getClass(fullClassName).also { holderCache = it }
            }

        fun createClassText(): String {
            var classText = ""
            classText += "package $packageName;\n"
            if(packageName != "gen") {
                classText += "import gen.*;"
            }
            classText += globalImports.joinToString("") { "import $it;" }
            classText += definition.imports.joinToString("") { "import $it;" }
            classText += "\n"
            classText += definition.createClassText(className)
            return classText
        }
    }

    /**
     * A delegate for a class declaration
     */
    inner class TestClass<T>(val name: String) {
        private var cache: Class<T>? = null

        @Suppress("UNCHECKED_CAST")
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Class<T> {
            cache?.also { return it }
            return (getClass(name) as Class<T>).also { cache = it }
        }

        /**
         * Gets a version of this class with the specified class type.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> typed(): TestClass<T> = this as TestClass<T>
    }
}

@DslMarker
private annotation class TypeSetDSL

/**
 * ```kotlin
 * import("some.package.ClassName")
 * import("java.util.List")
 * add("name", "ClassName[]")
 * +"List<ClassName>"
 * block("K", "V") {
 *     +"Generic<K>"
 *     +"Generic<V>"
 * }
 * ```
 */
class TypeSetDefinition {
    var nextBlockIndex: Int = 1
    val definitions: MutableMap<String, TypeDefinition> = mutableMapOf()
    val imports: MutableSet<String> = mutableSetOf()
    val rootBlock = TypeBlock(this, null, 0)

    fun import(vararg imports: String) {
        this.imports.addAll(imports)
    }

    fun find(name: String): TypeDefinition {
        return definitions[name] ?: throw IllegalArgumentException("No such type found: `$name`")
    }

    fun createClassText(className: String): String {
        var bodyText = ""
        bodyText += rootBlock.createClassText()
        bodyText += "\nfinal class __<T> {}"

        return "class $className {\n${bodyText.prependIndent("    ")}\n}"
    }
}

@TypeSetDSL
class TypeBlock(val root: TypeSetDefinition, val parent: TypeBlock?, val index: Int, vararg val variables: String) {
    val definitions: MutableList<TypeDefinition> = mutableListOf()
    val children: MutableList<TypeBlock> = mutableListOf()

    init {
        val re = """(\w+)(?:\s+extends|\s*$)""".toRegex()
        variables.forEach { variable ->
            re.find(variable)?.also { match ->
                add(variable, match.groupValues[1])
            } ?: throw IllegalArgumentException("Couldn't parse type variable '${variable}' to extract its name")
        }
    }

    operator fun String.unaryPlus(): Unit = add(this, this)
    fun add(name: String, type: String) {
        if(name in root.definitions)
            throw IllegalArgumentException("A type named `$name` already exists")
        val def = TypeDefinition(this, definitions.size, name, type)
        definitions.add(def)
        root.definitions[name] = def
    }

    inline fun block(vararg variables: String, config: TypeBlock.() -> Unit) {
        val block = TypeBlock(root, this, root.nextBlockIndex++, *variables)
        block.config()
        children.add(block)
    }

    fun createClassText(): String {
        var classText = ""

        classText += "class block_$index"
        if(variables.isNotEmpty())
            classText += "<${variables.joinToString(", ")}>"
        classText += " {\n"

        var bodyText = ""
        bodyText += definitions.joinToString("\n") { def ->
            def.createClassText()
        }

        if(definitions.isNotEmpty() && children.isNotEmpty()) {
            bodyText += "\n"
        }

        bodyText += children.joinToString("\n") { block ->
            block.createClassText()
        }

        classText += bodyText.prependIndent("    ")
        classText += "\n}"

        return classText
    }

    fun getClass(rootClass: Class<*>): Class<*> {
        val parentClass = this.parent?.getClass(rootClass) ?: rootClass
        val blockName = "block_$index"
        return parentClass.declaredClasses.find { it.simpleName == blockName }
            ?: throw IllegalStateException("Unable to find block `$blockName`")
    }
}

data class TypeDefinition(val block: TypeBlock, val index: Int, val name: String, val type: String) {
    private val fieldName = "type_${index}"
    fun getType(rootClass: Class<*>): AnnotatedType {
        val blockClass = block.getClass(rootClass)

        val field = blockClass.getDeclaredField(fieldName)
            ?: throw IllegalStateException("Unable to find field $fieldName in type block")

        return (field.annotatedType as AnnotatedParameterizedType).annotatedActualTypeArguments[0]
    }

    fun createClassText(): String {
        return "__<$type> $fieldName; // ${name.replace("\n", " ")}"
    }
}

