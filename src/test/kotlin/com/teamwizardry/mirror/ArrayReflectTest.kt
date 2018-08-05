package com.teamwizardry.mirror

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class ArrayReflectTest {
    @Test
    fun getLength_withAllArrayTypes_shouldReturnTheirLength() {
        assertEquals(3, ArrayReflect.getLength(arrayOf("", "", "")))
        assertEquals(3, ArrayReflect.getLength(booleanArrayOf(false, false, false)))
        assertEquals(3, ArrayReflect.getLength(byteArrayOf(0, 0, 0)))
        assertEquals(3, ArrayReflect.getLength(charArrayOf(' ', ' ', ' ')))
        assertEquals(3, ArrayReflect.getLength(shortArrayOf(0, 0, 0)))
        assertEquals(3, ArrayReflect.getLength(intArrayOf(0, 0, 0)))
        assertEquals(3, ArrayReflect.getLength(longArrayOf(0, 0, 0)))
        assertEquals(3, ArrayReflect.getLength(floatArrayOf(0f, 0f, 0f)))
        assertEquals(3, ArrayReflect.getLength(doubleArrayOf(0.0, 0.0, 0.0)))
    }

    @Test
    fun getLength_withNonArrayType_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            ArrayReflect.getLength(Any())
        }
    }

    @Test
    fun getLength_withNullArray_shouldThrowIllegalArgumentException() {
        assertThrows(NullPointerException::class.java) {
            ArrayReflect.getLength(null)
        }
    }

    @Test
    fun getBoolean_withValidArrayType_shouldReturnBooleanValue() {
        assertEquals(false, ArrayReflect.getBoolean(booleanArrayOf(false), 0))
    }
    @Test
    fun getBoolean_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        assertThrows(NullPointerException::class.java) { ArrayReflect.getBoolean(null, 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getBoolean(arrayOf(""), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getBoolean(byteArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getBoolean(charArrayOf(' '), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getBoolean(shortArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getBoolean(intArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getBoolean(longArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getBoolean(floatArrayOf(0f), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getBoolean(doubleArrayOf(0.0), 0) }
    }

    @Test
    fun getByte_withValidArrayType_shouldReturnByteValue() {
        assertEquals(0, ArrayReflect.getByte(byteArrayOf(0), 0))
    }
    @Test
    fun getByte_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        assertThrows(NullPointerException::class.java) { ArrayReflect.getByte(null, 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getByte(arrayOf(""), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getByte(booleanArrayOf(false), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getByte(charArrayOf(' '), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getByte(shortArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getByte(intArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getByte(longArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getByte(floatArrayOf(0f), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getByte(doubleArrayOf(0.0), 0) }
    }

    @Test
    fun getChar_withValidArrayType_shouldReturnCharValue() {
        assertEquals(' ', ArrayReflect.getChar(charArrayOf(' '), 0))
    }
    @Test
    fun getChar_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        assertThrows(NullPointerException::class.java) { ArrayReflect.getChar(null, 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getChar(arrayOf(""), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getChar(booleanArrayOf(false), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getChar(byteArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getChar(shortArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getChar(intArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getChar(longArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getChar(floatArrayOf(0f), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getChar(doubleArrayOf(0.0), 0) }
    }

    @Test
    fun getShort_withValidArrayType_shouldReturnShortValue() {
        assertEquals(0, ArrayReflect.getShort(byteArrayOf(0), 0))
        assertEquals(0, ArrayReflect.getShort(shortArrayOf(0), 0))
    }
    @Test
    fun getShort_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        assertThrows(NullPointerException::class.java) { ArrayReflect.getShort(null, 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getShort(arrayOf(""), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getShort(booleanArrayOf(false), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getShort(charArrayOf(' '), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getShort(intArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getShort(longArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getShort(floatArrayOf(0f), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getShort(doubleArrayOf(0.0), 0) }
    }

    @Test
    fun getInt_withValidArrayType_shouldReturnIntValue() {
        assertEquals(0, ArrayReflect.getInt(byteArrayOf(0), 0))
        assertEquals(' '.toInt(), ArrayReflect.getInt(charArrayOf(' '), 0))
        assertEquals(0, ArrayReflect.getInt(shortArrayOf(0), 0))
        assertEquals(0, ArrayReflect.getInt(intArrayOf(0), 0))
    }
    @Test
    fun getInt_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        assertThrows(NullPointerException::class.java) { ArrayReflect.getInt(null, 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getInt(arrayOf(""), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getInt(booleanArrayOf(false), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getInt(longArrayOf(0), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getInt(floatArrayOf(0f), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getInt(doubleArrayOf(0.0), 0) }
    }

    @Test
    fun getLong_withValidArrayType_shouldReturnLongValue() {
        assertEquals(0, ArrayReflect.getLong(byteArrayOf(0), 0))
        assertEquals(' '.toLong(), ArrayReflect.getLong(charArrayOf(' '), 0))
        assertEquals(0, ArrayReflect.getLong(shortArrayOf(0), 0))
        assertEquals(0, ArrayReflect.getLong(intArrayOf(0), 0))
        assertEquals(0, ArrayReflect.getLong(longArrayOf(0), 0))
    }
    @Test
    fun getLong_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        assertThrows(NullPointerException::class.java) { ArrayReflect.getLong(null, 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getLong(arrayOf(""), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getLong(booleanArrayOf(false), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getLong(floatArrayOf(0f), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getLong(doubleArrayOf(0.0), 0) }
    }

    @Test
    fun getFloat_withValidArrayType_shouldReturnFloatValue() {
        assertEquals(0f, ArrayReflect.getFloat(byteArrayOf(0), 0))
        assertEquals(' '.toFloat(), ArrayReflect.getFloat(charArrayOf(' '), 0))
        assertEquals(0f, ArrayReflect.getFloat(shortArrayOf(0), 0))
        assertEquals(0f, ArrayReflect.getFloat(intArrayOf(0), 0))
        assertEquals(0f, ArrayReflect.getFloat(longArrayOf(0), 0))
        assertEquals(0f, ArrayReflect.getFloat(floatArrayOf(0f), 0))
    }
    @Test
    fun getFloat_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        assertThrows(NullPointerException::class.java) { ArrayReflect.getFloat(null, 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getFloat(arrayOf(""), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getFloat(booleanArrayOf(false), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getFloat(doubleArrayOf(0.0), 0) }
    }

    @Test
    fun getDouble_withValidArrayType_shouldReturnDoubleValue() {
        assertEquals(0.0, ArrayReflect.getDouble(byteArrayOf(0), 0))
        assertEquals(' '.toDouble(), ArrayReflect.getDouble(charArrayOf(' '), 0))
        assertEquals(0.0, ArrayReflect.getDouble(shortArrayOf(0), 0))
        assertEquals(0.0, ArrayReflect.getDouble(intArrayOf(0), 0))
        assertEquals(0.0, ArrayReflect.getDouble(longArrayOf(0), 0))
        assertEquals(0.0, ArrayReflect.getDouble(floatArrayOf(0f), 0))
        assertEquals(0.0, ArrayReflect.getDouble(doubleArrayOf(0.0), 0))
    }
    @Test
    fun getDouble_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        assertThrows(NullPointerException::class.java) { ArrayReflect.getDouble(null, 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getDouble(arrayOf(""), 0) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.getDouble(booleanArrayOf(false), 0) }
    }

    @Test
    fun get_withValidArrayType_shouldReturnValue() {
        assertEquals("", ArrayReflect.get(arrayOf(""), 0))
        assertEquals(false, ArrayReflect.get(booleanArrayOf(false), 0))
        assertEquals(0.toByte(), ArrayReflect.get(byteArrayOf(0), 0))
        assertEquals(' ', ArrayReflect.get(charArrayOf(' '), 0))
        assertEquals(0.toShort(), ArrayReflect.get(shortArrayOf(0), 0))
        assertEquals(0.toInt(), ArrayReflect.get(intArrayOf(0), 0))
        assertEquals(0.toLong(), ArrayReflect.get(longArrayOf(0), 0))
        assertEquals(0f, ArrayReflect.get(floatArrayOf(0f), 0))
        assertEquals(0.0, ArrayReflect.get(doubleArrayOf(0.0), 0))
    }

    @Test
    fun setBoolean_withValidArrayType_shouldSetValue() {
        val value: Boolean = true
        val booleanArray = booleanArrayOf(false); ArrayReflect.setBoolean(booleanArray, 0, value); assertEquals(value, booleanArray[0])
    }
    @Test
    fun setBoolean_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        val value: Boolean = true
        assertThrows(NullPointerException::class.java) { ArrayReflect.setBoolean(null, 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setBoolean(arrayOf(""), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setBoolean(byteArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setBoolean(charArrayOf(' '), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setBoolean(shortArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setBoolean(intArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setBoolean(longArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setBoolean(floatArrayOf(0f), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setBoolean(doubleArrayOf(0.0), 0, value) }
    }

    @Test
    fun setByte_withValidArrayType_shouldSetValue() {
        val value: Byte = 1
        val byteArray = byteArrayOf(0); ArrayReflect.setByte(byteArray, 0, value); assertEquals(value, byteArray[0])
        val shortArray = shortArrayOf(0); ArrayReflect.setByte(shortArray, 0, value); assertEquals(value.toShort(), shortArray[0])
        val intArray = intArrayOf(0); ArrayReflect.setByte(intArray, 0, value); assertEquals(value.toInt(), intArray[0])
        val longArray = longArrayOf(0); ArrayReflect.setByte(longArray, 0, value); assertEquals(value.toLong(), longArray[0])
        val floatArray = floatArrayOf(0f); ArrayReflect.setByte(floatArray, 0, value); assertEquals(value.toFloat(), floatArray[0])
        val doubleArray = doubleArrayOf(0.0); ArrayReflect.setByte(doubleArray, 0, value); assertEquals(value.toDouble(), doubleArray[0])
    }
    @Test
    fun setByte_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        val value: Byte = 1
        assertThrows(NullPointerException::class.java) { ArrayReflect.setByte(null, 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setByte(arrayOf(""), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setByte(booleanArrayOf(false), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setByte(charArrayOf(' '), 0, value) }
    }

    @Test
    fun setChar_withValidArrayType_shouldSetValue() {
        val value: Char = 'x'
        val charArray = charArrayOf(' '); ArrayReflect.setChar(charArray, 0, value); assertEquals(value, charArray[0])
        val intArray = intArrayOf(0); ArrayReflect.setChar(intArray, 0, value); assertEquals(value.toInt(), intArray[0])
        val longArray = longArrayOf(0); ArrayReflect.setChar(longArray, 0, value); assertEquals(value.toLong(), longArray[0])
        val floatArray = floatArrayOf(0f); ArrayReflect.setChar(floatArray, 0, value); assertEquals(value.toFloat(), floatArray[0])
        val doubleArray = doubleArrayOf(0.0); ArrayReflect.setChar(doubleArray, 0, value); assertEquals(value.toDouble(), doubleArray[0])
    }
    @Test
    fun setChar_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        val value: Char = 'x'
        assertThrows(NullPointerException::class.java) { ArrayReflect.setChar(null, 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setChar(arrayOf(""), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setChar(booleanArrayOf(false), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setChar(byteArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setChar(shortArrayOf(0), 0, value) }
    }

    @Test
    fun setShort_withValidArrayType_shouldSetValue() {
        val value: Short = 1
        val shortArray = shortArrayOf(0); ArrayReflect.setShort(shortArray, 0, value); assertEquals(value, shortArray[0])
        val intArray = intArrayOf(0); ArrayReflect.setShort(intArray, 0, value); assertEquals(value.toInt(), intArray[0])
        val longArray = longArrayOf(0); ArrayReflect.setShort(longArray, 0, value); assertEquals(value.toLong(), longArray[0])
        val floatArray = floatArrayOf(0f); ArrayReflect.setShort(floatArray, 0, value); assertEquals(value.toFloat(), floatArray[0])
        val doubleArray = doubleArrayOf(0.0); ArrayReflect.setShort(doubleArray, 0, value); assertEquals(value.toDouble(), doubleArray[0])
    }
    @Test
    fun setShort_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        val value: Short = 1
        assertThrows(NullPointerException::class.java) { ArrayReflect.setShort(null, 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setShort(arrayOf(""), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setShort(booleanArrayOf(false), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setShort(byteArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setShort(charArrayOf(' '), 0, value) }
    }

    @Test
    fun setInt_withValidArrayType_shouldSetValue() {
        val value: Int = 1
        val intArray = intArrayOf(0); ArrayReflect.setInt(intArray, 0, value); assertEquals(value, intArray[0])
        val longArray = longArrayOf(0); ArrayReflect.setInt(longArray, 0, value); assertEquals(value.toLong(), longArray[0])
        val floatArray = floatArrayOf(0f); ArrayReflect.setInt(floatArray, 0, value); assertEquals(value.toFloat(), floatArray[0])
        val doubleArray = doubleArrayOf(0.0); ArrayReflect.setInt(doubleArray, 0, value); assertEquals(value.toDouble(), doubleArray[0])
    }
    @Test
    fun setInt_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        val value: Int = 1
        assertThrows(NullPointerException::class.java) { ArrayReflect.setInt(null, 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setInt(arrayOf(""), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setInt(booleanArrayOf(false), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setInt(byteArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setInt(charArrayOf(' '), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setInt(shortArrayOf(0), 0, value) }
    }

    @Test
    fun setLong_withValidArrayType_shouldSetValue() {
        val value: Long = 1
        val longArray = longArrayOf(0); ArrayReflect.setLong(longArray, 0, value); assertEquals(value, longArray[0])
        val floatArray = floatArrayOf(0f); ArrayReflect.setLong(floatArray, 0, value); assertEquals(value.toFloat(), floatArray[0])
        val doubleArray = doubleArrayOf(0.0); ArrayReflect.setLong(doubleArray, 0, value); assertEquals(value.toDouble(), doubleArray[0])
    }
    @Test
    fun setLong_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        val value: Long = 1
        assertThrows(NullPointerException::class.java) { ArrayReflect.setLong(null, 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setLong(arrayOf(""), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setLong(booleanArrayOf(false), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setLong(byteArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setLong(charArrayOf(' '), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setLong(shortArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setLong(intArrayOf(0), 0, value) }
    }

    @Test
    fun setFloat_withValidArrayType_shouldSetValue() {
        val value: Float = 1f
        val floatArray = floatArrayOf(0f); ArrayReflect.setFloat(floatArray, 0, value); assertEquals(value, floatArray[0])
        val doubleArray = doubleArrayOf(0.0); ArrayReflect.setFloat(doubleArray, 0, value); assertEquals(value.toDouble(), doubleArray[0])
    }
    @Test
    fun setFloat_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        val value: Float = 1f
        assertThrows(NullPointerException::class.java) { ArrayReflect.setFloat(null, 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setFloat(arrayOf(""), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setFloat(booleanArrayOf(false), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setFloat(byteArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setFloat(charArrayOf(' '), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setFloat(shortArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setFloat(intArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setFloat(longArrayOf(0), 0, value) }
    }

    @Test
    fun setDouble_withValidArrayType_shouldSetValue() {
        val value: Double = 1.0
        val doubleArray = doubleArrayOf(0.0); ArrayReflect.setDouble(doubleArray, 0, value); assertEquals(value, doubleArray[0])
    }
    @Test
    fun setDouble_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        val value: Double = 1.0
        assertThrows(NullPointerException::class.java) { ArrayReflect.setDouble(null, 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setDouble(arrayOf(""), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setDouble(booleanArrayOf(false), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setDouble(byteArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setDouble(charArrayOf(' '), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setDouble(shortArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setDouble(intArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setDouble(longArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.setDouble(floatArrayOf(0f), 0, value) }
    }

    @Test
    fun set_withValidArrayType_shouldSetValue() {
        val value: String = "x"
        val objectArray = arrayOf(""); ArrayReflect.set(objectArray, 0, value); assertEquals(value, objectArray[0])
    }
    @Test
    fun set_withInvalidArrayType_shouldThrowIllegalArgumentException() {
        val value: Any = Any()
        assertThrows(NullPointerException::class.java) { ArrayReflect.set(null, 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.set(booleanArrayOf(false), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.set(byteArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.set(charArrayOf(' '), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.set(shortArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.set(intArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.set(longArrayOf(0), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.set(floatArrayOf(0f), 0, value) }
        assertThrows(IllegalArgumentException::class.java) { ArrayReflect.set(doubleArrayOf(0.0), 0, value) }
    }

    @Test
    fun newInstanceRaw_withPrimitiveTypes_shouldReturnArraysWithRespectiveTypes() {
        assertEquals(BooleanArray::class.java, ArrayReflect.newInstanceRaw(Boolean::class.javaPrimitiveType!!, 0).javaClass)
        assertEquals(ByteArray::class.java, ArrayReflect.newInstanceRaw(Byte::class.javaPrimitiveType!!, 0).javaClass)
        assertEquals(ByteArray::class.java, ArrayReflect.newInstanceRaw(Byte::class.javaPrimitiveType!!, 0).javaClass)
        assertEquals(CharArray::class.java, ArrayReflect.newInstanceRaw(Char::class.javaPrimitiveType!!, 0).javaClass)
        assertEquals(ShortArray::class.java, ArrayReflect.newInstanceRaw(Short::class.javaPrimitiveType!!, 0).javaClass)
        assertEquals(IntArray::class.java, ArrayReflect.newInstanceRaw(Int::class.javaPrimitiveType!!, 0).javaClass)
        assertEquals(LongArray::class.java, ArrayReflect.newInstanceRaw(Long::class.javaPrimitiveType!!, 0).javaClass)
        assertEquals(FloatArray::class.java, ArrayReflect.newInstanceRaw(Float::class.javaPrimitiveType!!, 0).javaClass)
        assertEquals(DoubleArray::class.java, ArrayReflect.newInstanceRaw(Double::class.javaPrimitiveType!!, 0).javaClass)
    }

    @Test
    fun newInstanceRaw_withObjectTypes_shouldReturnArraysWithRespectiveTypes() {
        assertEquals(Array<Any>::class.java, ArrayReflect.newInstanceRaw(Any::class.java, 0).javaClass)
        assertEquals(Array<String>::class.java, ArrayReflect.newInstanceRaw(String::class.java, 0).javaClass)
    }

}
