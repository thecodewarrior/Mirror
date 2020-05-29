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
 *     typeVariables("T") {
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
        "java.lang.annotation.RetentionPolicy"
    )

    /**
     * Adds the passed class to this compiler. This method automatically prepends the necessary `package` declaration
     * to the passed string and adds a wildcard import for the "root" package, `gen`, if needed.
     *
     * Any occurrences of `@rt(targets)` in the text will be replaced with the annotations for runtime annotation
     * retention and the passed element targets.
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
        fullSource += processedCode

        javaSources["gen.$name"] = fullSource

        return TestClass<Any>("gen.$name")
    }

    fun types(packageName: String? = null, block: TypeSetDefinition.() -> Unit): TypeSet {
        requireNotCompiled()
        val builder = TypeSetDefinition()
        builder.block()
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
            typeCache[name]?.also { return it }

            val typeDef = definition.find(name)
            val blockName = "block_${typeDef.blockIndex}"
            val block = holder.declaredClasses.find { it.simpleName == blockName }
                ?: throw IllegalStateException("Unable to find block `$blockName` in type holder class")
            val defName = "type_${typeDef.index}"
            val field = block.getDeclaredField(defName)
                ?: throw IllegalStateException("Unable to find type ${typeDef.index} in type block")

            val type = (field.annotatedType as AnnotatedParameterizedType).annotatedActualTypeArguments[0]
            typeCache[name] = type
            return type
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
            definition.imports.forEach {
                classText += "import $it;"
            }
            classText += "\nclass $className {\n"

            classText += definition.blocks.joinToString("") { block ->
                var blockText = "    class block_${block.index}"
                if(block.variables.isNotEmpty())
                    blockText += "<${block.variables.joinToString(", ")}>"
                blockText += " {\n"
                blockText += block.definitions.joinToString("") { def ->
                    "        __<${def.type}> type_${def.index}; // ${def.name.replace("\n", " ")}\n"
                }
                blockText += "    }\n"
                blockText
            }

            classText += "    final class __<T> {}\n"
            classText += "}"

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
 * typeVariables("K", "V") {
 *     +"Generic<K>"
 *     +"Generic<V>"
 * }
 * ```
 */
@TypeSetDSL
class TypeSetDefinition {
    val imports: MutableList<String> = mutableListOf()
    val defaultBlock: TypeBlock = TypeBlock(0)
    val blocks: MutableList<TypeBlock> = mutableListOf(defaultBlock)
    private val names: MutableSet<String> = mutableSetOf()

    fun import(vararg imports: String) {
        this.imports.addAll(imports)
    }

    inline fun typeVariables(vararg variables: String, config: TypeBlock.() -> Unit) {
        val block = TypeBlock(blocks.size, *variables)
        block.config()
        blocks.add(block)
    }

    operator fun String.unaryPlus(): Unit = defaultBlock.add(this, this)
    fun add(name: String, type: String): Unit = defaultBlock.add(name, type)

    fun find(name: String): TypeDefinition {
        return blocks.asSequence().flatMap { it.definitions.asSequence() }.find { it.name == name }
            ?: throw IllegalArgumentException("No such type found: `$name`")
    }

    @TypeSetDSL
    inner class TypeBlock(val index: Int, vararg val variables: String) {
        val definitions: MutableList<TypeDefinition> = mutableListOf()

        operator fun String.unaryPlus(): Unit = add(this, this)
        fun add(name: String, type: String) {
            if(this@TypeSetDefinition.names.add(name))
                definitions.add(TypeDefinition(index, definitions.size, name, type))
            else
                throw IllegalArgumentException("A type named `$name` already exists")
        }
    }

    data class TypeDefinition(val blockIndex: Int, val index: Int, val name: String, val type: String)
}
