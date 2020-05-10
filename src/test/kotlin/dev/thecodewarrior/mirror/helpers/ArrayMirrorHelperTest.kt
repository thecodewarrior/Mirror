package dev.thecodewarrior.mirror.helpers

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.TestSources
import dev.thecodewarrior.mirror.type.ArrayMirror
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.reflect.AnnotatedArrayType


internal class ArrayMirrorHelperTest: MTest() {

    private inline fun <reified T> reflectArray() = Mirror.reflect<T>() as ArrayMirror

    @Test
    fun newInstance_onPrimitiveArray_shouldReturnPrimitiveArray() {
        assertEquals(BooleanArray::class.java, reflectArray<BooleanArray>().newInstance(0).javaClass)
        assertEquals(ByteArray::class.java, reflectArray<ByteArray>().newInstance(0).javaClass)
        assertEquals(CharArray::class.java, reflectArray<CharArray>().newInstance(0).javaClass)
        assertEquals(ShortArray::class.java, reflectArray<ShortArray>().newInstance(0).javaClass)
        assertEquals(IntArray::class.java, reflectArray<IntArray>().newInstance(0).javaClass)
        assertEquals(LongArray::class.java, reflectArray<LongArray>().newInstance(0).javaClass)
        assertEquals(FloatArray::class.java, reflectArray<FloatArray>().newInstance(0).javaClass)
        assertEquals(DoubleArray::class.java, reflectArray<DoubleArray>().newInstance(0).javaClass)
    }

    @Test
    fun newInstance_onObjectArray_shouldReturnTypedArray() {
        assertEquals(Array<Any>::class.java, reflectArray<Array<Any>>().newInstance(0).javaClass)
        assertEquals(Array<Object1>::class.java, reflectArray<Array<Object1>>().newInstance(0).javaClass)
    }

    @Test
    fun newInstance_onGenericArray_shouldReturnObjectArray() {
        val sources = TestSources()
        val types = sources.types {
            typeVariables("T") {
                +"T[]"
            }
        }
        sources.compile()
        val type = Mirror.reflect(types["T[]"]) as ArrayMirror
        assertEquals(Array<Any>::class.java, type.newInstance(0).javaClass)
    }
}
