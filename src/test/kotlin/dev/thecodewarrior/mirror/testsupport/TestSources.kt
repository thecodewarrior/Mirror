package dev.thecodewarrior.mirror.testsupport

import dev.thecodewarrior.mirror.joor.Compile
import dev.thecodewarrior.mirror.joor.CompileOptions
import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import kotlin.reflect.KProperty

/**
 *
 */
class TestSources {

    private val sources = mutableMapOf<String, String>()
    private val typeSets = mutableListOf<TypeSet>()
    var options: CompileOptions = CompileOptions()
    var classLoader: Compile.RuntimeClassLoader? = null

    /**
     * Imports to be added to all files. Set these _before_ adding the files using [add]. Defaults to a set of generally
     * useful or commonly used types.
     */
    var globalImports: MutableList<String> = mutableListOf(
        "java.util.*",
        "java.lang.annotation.ElementType",
        "java.lang.annotation.Target"
    )

    /**
     * Adds the passed class to this compiler. This method automatically prepends the necessary `package` declaration
     * to the passed string and adds a wildcard import for the "root" package, `gen`, if needed.
     *
     * @param name The qualified name relative to the "root" package (`gen`)
     * @param code The code to compile into that class
     * @return A property delegate to access the test class once [compile] has been called
     */
    fun add(name: String, code: String): TestClass {
        requireNotCompiled()
        if("gen.$name" in sources)
            throw IllegalArgumentException("Class name $name already exists")

        var fullSource = ""
        if(name.contains('.'))
            fullSource += "package gen.${name.substringBeforeLast('.')};import gen.*;\n"
        else
            fullSource += "package gen;\n"

        fullSource += globalImports.joinToString("") { "import $it;\n" }
        fullSource += code

        sources["gen.$name"] = fullSource

        return TestClass("gen.$name")
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
        this.classLoader = Compile.compile(sources + typeSets.associate { set ->
            set.fullClassName to set.classText
        }, options)
    }

    private fun requireNotCompiled() {
        if(classLoader != null)
            throw IllegalStateException("The sources have already been compiled")
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
                if(classLoader == null)
                    throw ClassNotFoundException("Sources not compiled yet")
                return Class.forName(fullClassName, true, classLoader).also { holderCache = it }
            }

        fun createClassText(): String {
            var classText = ""
            classText += "package $packageName;\n"
            if(packageName != "gen") {
                classText += "import gen.*;" + globalImports.joinToString("") { "import $it;" }
            }
            definition.imports.forEach {
                classText += "import $it;"
            }
            classText += "class $className {\n"

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

    inner class TestClass(val name: String) {
        private var cache: Class<*>? = null

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Class<*> {
            cache?.also { return it }
            if(classLoader == null)
                throw ClassNotFoundException("Sources not compiled yet")
            return Class.forName(name, true, classLoader).also { cache = it }
        }
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
            ?: throw IllegalArgumentException("No such type found")
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
