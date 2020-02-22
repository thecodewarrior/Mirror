package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MTest
import dev.thecodewarrior.mirror.testsupport.TestSources
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@Suppress("LocalVariableName")
internal class IsAssignableTest: MTest() {

    @Test
    fun arrayMirror_shouldBeAssignable_fromItself() {
        val type = Mirror.reflect<Array<Any>>()
        assertTrue(type.isAssignableFrom(type))
    }

    @Test
    fun primitiveArrayMirror_shouldBeAssignable_fromItself() {
        val type = Mirror.reflect<IntArray>()
        assertTrue(type.isAssignableFrom(type))
    }

    @Test
    fun primitiveArrayMirror_shouldBeAssignable_fromBoxedArrayMirror() {
        assertFalse(Mirror.reflect<IntArray>().isAssignableFrom(Mirror.reflect<Array<Int>>()))
    }

    @Test
    fun object_shouldBeAssignable_fromItself() {
        assertTrue(
            Mirror.reflect<Any>().isAssignableFrom(
                Mirror.reflect<Any>()
            )
        )
    }

    @Test
    fun object_shouldBeAssignable_fromClass() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        sources.compile()

        assertTrue(
            Mirror.reflect<Any>().isAssignableFrom(
                Mirror.reflect(X)
            )
        )
    }

    @Test
    fun object_shouldBeAssignable_fromGeneric() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val types = sources.types {
            +"Generic<X>"
        }
        sources.compile()

        assertTrue(
            Mirror.reflect<Any>().isAssignableFrom(
                Mirror.reflect(types["Generic<X>"])
            )
        )
    }

    @Test
    fun object_shouldBeAssignable_fromArray() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"X[]"
        }
        sources.compile()

        assertTrue(
            Mirror.reflect<Any>().isAssignableFrom(
                Mirror.reflect(types["X[]"])
            )
        )
    }

    @Test
    fun object_shouldNotBeAssignable_fromPrimitive() {
        assertFalse(
            Mirror.reflect<Any>().isAssignableFrom(
                Mirror.types.int
            )
        )
    }

    interface ClassASuperInterface
    interface ClassAInterface
    interface ClassASubInterface
    open class ClassASuper: ClassASuperInterface
    open class ClassA: ClassASuper(), ClassAInterface
    open class ClassASub: ClassA(), ClassASubInterface

    @Test
    @DisplayName("A class mirror should be assignable from itself")
    fun classAssignableFromSelf() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        sources.compile()
        assertTrue(
            Mirror.reflect(X).isAssignableFrom(
                Mirror.reflect(X)
            )
        )
    }

    @Test
    @DisplayName("A class mirror should be assignable from a subclass")
    fun classAssignableFromSubclass() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        sources.compile()
        assertTrue(
            Mirror.reflect(X).isAssignableFrom(
                Mirror.reflect(Y)
            )
        )
    }

    @Test
    @DisplayName("A class mirror should not be assignable from a superclass")
    fun classNotAssignableFromSuperclass() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        sources.compile()
        assertFalse(
            Mirror.reflect(Y).isAssignableFrom(
                Mirror.reflect(X)
            )
        )
    }

    @Test
    @DisplayName("A class mirror should not be assignable from an unrelated class")
    fun classNotAssignableFromUnrelatedClass() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y {}")
        sources.compile()
        assertFalse(
            Mirror.reflect(X).isAssignableFrom(
                Mirror.reflect(Y)
            )
        )
    }

    @Test
    @DisplayName("An interface mirror should be assignable from an implementing class")
    fun interfaceAssignableFromImplementingClass() {
        val sources = TestSources()
        val I by sources.add("I", "interface I {}")
        val X by sources.add("X", "class X implements I {}")
        sources.compile()
        assertTrue(
            Mirror.reflect(I).isAssignableFrom(
                Mirror.reflect(X)
            )
        )
    }

    @Test
    @DisplayName("An interface should be assignable from the subclass of an implementing class")
    fun interfaceAssignableFromSubClassOfImplementingClass() {
        val sources = TestSources()
        val I by sources.add("I", "interface I {}")
        val X by sources.add("X", "class X implements I {}")
        val Y by sources.add("Y", "class Y extends X {}")
        sources.compile()
        assertTrue(
            Mirror.reflect(I).isAssignableFrom(
                Mirror.reflect(Y)
            )
        )
    }

    @Test
    @DisplayName("A generic class should be assignable from itself")
    fun genericClassAssignableFromSelf() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val types = sources.types {
            +"Generic<X>"
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["Generic<X>"]).isAssignableFrom(
                Mirror.reflect(types["Generic<X>"])
            )
        )
    }

    @Test
    @DisplayName("A generic class should be assignable from itself with subclassed type parameters")
    fun genericClassAssignableFromSubclassedParameters() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val types = sources.types {
            +"Generic<X>"
            +"Generic<Y>"
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["Generic<X>"]).isAssignableFrom(
                Mirror.reflect(types["Generic<Y>"])
            )
        )
    }

    @Test
    @DisplayName("A generic class should not be assignable from itself with superclassed type parameters")
    fun genericClassNotAssignableFromSuperclassedParameters() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val types = sources.types {
            +"Generic<X>"
            +"Generic<Y>"
        }
        sources.compile()
        assertFalse(
            Mirror.reflect(types["Generic<Y>"]).isAssignableFrom(
                Mirror.reflect(types["Generic<X>"])
            )
        )
    }

    @Test
    @DisplayName("A generic class should be assignable from a subclass that specifies type parameters explicitly")
    fun genericClassAssignableFromSubclassWithExplicitParameters() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val GenericX by sources.add("GenericX", "class GenericX extends Generic<X> {}")
        val types = sources.types {
            +"Generic<X>"
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["Generic<X>"]).isAssignableFrom(
                Mirror.reflect(GenericX)
            )
        )
    }

    @Test
    @DisplayName("A generic class should be assignable from a subclass that specifies type parameters dynamically")
    fun genericClassAssignableFromSubclassWithDynamicParameters() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val GenericSub by sources.add("GenericSub", "class GenericSub<T> extends Generic<Generic<T>> {}")
        val types = sources.types {
            +"Generic<Generic<X>>"
            +"GenericSub<X>"
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["Generic<Generic<X>>"]).isAssignableFrom(
                Mirror.reflect(types["GenericSub<X>"])
            )
        )
    }

    @Test
    @DisplayName("A generic class should be assignable from a subclass that specifies incorrect type parameters dynamically")
    fun genericClassNotAssignableFromSubclassWithIncorrectDynamicParameters() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y {}")
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val GenericSub by sources.add("GenericSub", "class GenericSub<T> extends Generic<Generic<T>> {}")
        val types = sources.types {
            +"Generic<Generic<X>>"
            +"GenericSub<Y>"
        }
        sources.compile()
        assertFalse(
            Mirror.reflect(types["Generic<Generic<X>>"]).isAssignableFrom(
                Mirror.reflect(types["GenericSub<Y>"])
            )
        )
    }

    @Test
    @DisplayName("A generic class should be assignable from a raw version of itself")
    fun genericClassNotAssignableFromRawSelf() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val types = sources.types {
            +"Generic<X>"
        }
        sources.compile()
        assertFalse(
            Mirror.reflect(types["Generic<X>"]).isAssignableFrom(
                Mirror.reflect(Generic)
            )
        )
    }

    @Test
    @DisplayName("A raw generic class should be assignable from a raw version of itself")
    fun rawGenericClassAssignableFromRawSelf() {
        val sources = TestSources()
        val Generic by sources.add("Generic", "class Generic<T> {}")
        sources.compile()
        assertTrue(
            Mirror.reflect(Generic).raw.isAssignableFrom(
                Mirror.reflect(Generic)
            )
        )
    }

    @Test
    @DisplayName("A raw generic class should be assignable from a specialized version of itself")
    fun rawGenericClassAssignableFromSpecializedSelf() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val types = sources.types {
            +"Generic<X>"
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(Generic).isAssignableFrom(
                Mirror.reflect(types["Generic<X>"])
            )
        )
    }

    @Test
    @DisplayName("A class should be assignable from a wildcard with an equal upper bound")
    fun classAssignableFromEqualUpperBoundedWildcard() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        val types = sources.types {
            +"? extends Y"
        }
        sources.compile()
        assertFalse(
            Mirror.reflect(Y).isAssignableFrom(
                Mirror.reflect(types["? extends Y"])
            )
        )
    }

    @Test
    @DisplayName("A class should be assignable from a wildcard with a more subclass as a upper bound")
    fun classAssignableFromSubclassUpperBoundedWildcard() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        val types = sources.types {
            +"? extends Y"
        }
        sources.compile()
        assertFalse(
            Mirror.reflect(X).isAssignableFrom(
                Mirror.reflect(types["? extends Y"])
            )
        )
    }

    @Test
    @DisplayName("The void mirror should be assignable from itself")
    fun voidAssignableFromSelf() {
        assertTrue(
            Mirror.types.void.isAssignableFrom(
                Mirror.types.void
            )
        )
    }

    @Test
    @DisplayName("The void mirror should not be assignable from other types")
    fun voidNotAssignableFromOthers() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val types = sources.types {
            +"int[]"
            +"Generic<X>"
            +"? super Y"
            +"? extends X"
        }
        sources.compile()
        assertFalse(
            Mirror.types.void.isAssignableFrom(
                Mirror.types.any
            )
        )
        assertFalse(
            Mirror.types.void.isAssignableFrom(
                Mirror.reflect(types["int[]"])
            )
        )
        assertFalse(
            Mirror.types.void.isAssignableFrom(
                Mirror.reflect(types["Generic<X>"])
            )
        )
        assertFalse(
            Mirror.types.void.isAssignableFrom(
                Mirror.reflect(types["? super Y"])
            )
        )
        assertFalse(
            Mirror.types.void.isAssignableFrom(
                Mirror.reflect(types["? extends X"])
            )
        )
    }

    @Test
    @DisplayName("The Object mirror should be assignable from wildcard mirrors")
    fun objectAssignableFromWildcard() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        val types = sources.types {
            +"? super Y"
            +"? extends X"
        }
        sources.compile()
        assertTrue(
            Mirror.reflect<Any>().isAssignableFrom(
                Mirror.reflect(types["? super Y"])
            )
        )
        assertTrue(
            Mirror.reflect<Any>().isAssignableFrom(
                Mirror.reflect(types["? extends X"])
            )
        )
    }

    @Test
    @DisplayName("Lower-bounded wildcard mirrors should be assignable from mirrors of their supertype")
    fun lowerWildcardAssignableFromSupertype() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        val types = sources.types {
            +"? super Y"
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["? super Y"]).isAssignableFrom(
                Mirror.reflect(X)
            )
        )
    }

    @Test
    @DisplayName("Lower-bounded wildcard mirrors should be assignable from mirrors of their bound")
    fun lowerWildcardAssignableFromBound() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        val types = sources.types {
            +"? super Y"
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["? super Y"]).isAssignableFrom(
                Mirror.reflect(Y)
            )
        )
    }

    @Test
    @DisplayName("Lower-bounded wildcard mirrors should not be assignable from mirrors of their subtypes")
    fun lowerWildcardNotAssignableFromSubtype() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val types = sources.types {
            +"? super X"
        }
        sources.compile()
        assertFalse(
            Mirror.reflect(types["? super X"]).isAssignableFrom(
                Mirror.reflect(Y)
            )
        )
    }

    @Test
    @DisplayName("Upper-bounded wildcard mirrors should not be assignable from mirrors of their supertype")
    fun upperWildcardNotAssignableFromSupertype() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        val types = sources.types {
            +"? extends Y"
        }
        sources.compile()
        assertFalse(
            Mirror.reflect(types["? extends Y"]).isAssignableFrom(
                Mirror.reflect(X)
            )
        )
    }

    @Test
    @DisplayName("Upper-bounded wildcard mirrors should be assignable from mirrors of their bound")
    fun upperWildcardAssignableFromBound() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            +"? extends X"
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["? extends X"]).isAssignableFrom(
                Mirror.reflect(X)
            )
        )
    }

    @Test
    @DisplayName("Upper-bounded wildcard mirrors should be assignable from mirrors of their subtypes")
    fun upperWildcardAssignableFromSubtype() {
        val sources = TestSources()
        val X by sources.add("X", "class X {}")
        val Y by sources.add("Y", "class Y extends X {}")
        val Generic by sources.add("Generic", "class Generic<T> {}")
        val types = sources.types {
            +"? extends X"
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["? extends X"]).isAssignableFrom(
                Mirror.reflect(Y)
            )
        )
    }

    @Test
    @DisplayName("Variable mirrors should be assignable from mirrors of their bounds")
    fun variableAssignableFromSameType() {
        val sources = TestSources()
        val I by sources.add("I", "interface I {}")
        val types = sources.types {
            typeVariables("T extends I") {
                +"T"
            }
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["T"]).isAssignableFrom(
                Mirror.reflect(I)
            )
        )
    }

    @Test
    @DisplayName("Variable mirrors should be assignable from subtypes of their bounds")
    fun variableAssignableFromSubtype() {
        val sources = TestSources()
        val I by sources.add("I", "interface I {}")
        val X by sources.add("X", "class X implements I {}")
        val types = sources.types {
            typeVariables("T extends I") {
                +"T"
            }
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["T"]).isAssignableFrom(
                Mirror.reflect(X)
            )
        )
    }

    @Test
    @DisplayName("Variable mirrors should be assignable from mirrors that implement all of their bounds")
    fun variableAssignableFromDoubleImplement() {
        val sources = TestSources()
        val I by sources.add("I", "interface I {}")
        val I2 by sources.add("I2", "interface I2 {}")
        val I3 by sources.add("I3", "interface I3 extends I, I2 {}")
        val types = sources.types {
            typeVariables("T extends I & I2") {
                +"T"
            }
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["T"]).isAssignableFrom(
                Mirror.reflect(I3)
            )
        )
    }

    @Test
    @DisplayName("Variable mirrors should be assignable from mirrors that subclass all of their bounds")
    fun variableAssignableFromDoubleSubtype() {
        val sources = TestSources()
        val I by sources.add("I", "interface I {}")
        val I2 by sources.add("I2", "interface I2 {}")
        val X by sources.add("X", "class X implements I, I2{}")
        val types = sources.types {
            typeVariables("T extends I & I2") {
                +"T"
            }
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["T"]).isAssignableFrom(
                Mirror.reflect(X)
            )
        )
    }

    @Test
    @DisplayName("Variable mirrors should not be assignable from mirrors that superclass their bounds")
    fun variableNotAssignableFromSupertype() {
        val sources = TestSources()
        val I by sources.add("I", "interface I {}")
        val I2 by sources.add("I2", "interface I2 extends I {}")
        val types = sources.types {
            typeVariables("T extends I2") {
                +"T"
            }
        }
        sources.compile()
        assertFalse(
            Mirror.reflect(types["T"]).isAssignableFrom(
                Mirror.reflect(I)
            )
        )
    }

    @Test
    @DisplayName("Variable mirrors should not be assignable from mirrors unrelated to their bounds")
    fun variableNotAssignableFromUnrelated() {
        val sources = TestSources()
        val I by sources.add("I", "interface I {}")
        val X by sources.add("X", "class X {}")
        val types = sources.types {
            typeVariables("T extends I") {
                +"T"
            }
        }
        sources.compile()
        assertFalse(
            Mirror.reflect(types["T"]).isAssignableFrom(
                Mirror.reflect(X)
            )
        )
    }

    @Test
    @DisplayName("Variable mirrors should be assignable from themselves")
    fun variableAssignableFromSelf() {
        val sources = TestSources()
        val types = sources.types {
            typeVariables("T") {
                +"T"
            }
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["T"]).isAssignableFrom(
                Mirror.reflect(types["T"])
            )
        )
    }

    @Test
    @DisplayName("Variable mirrors should be assignable from variables with subclassed bounds")
    fun variableAssignableFromSubtypeBounds() {
        val sources = TestSources()
        val I by sources.add("I", "interface I {}")
        val I2 by sources.add("I2", "interface I2 extends I {}")
        val types = sources.types {
            typeVariables("T extends I", "T2 extends I2") {
                +"T"
                +"T2"
            }
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["T"]).isAssignableFrom(
                Mirror.reflect(types["T2"])
            )
        )
    }

    @Test
    @DisplayName("Variable mirrors should be assignable from variables with multiple subclassed bounds")
    fun variableAssignableFromMultipleSubtypeBounds() {
        val sources = TestSources()
        val I by sources.add("I", "interface I {}")
        val J by sources.add("J", "interface J {}")
        val I2 by sources.add("I2", "interface I2 extends I {}")
        val J2 by sources.add("J2", "interface J2 extends J {}")
        val types = sources.types {
            typeVariables("T extends I & J", "T2 extends I2 & J2") {
                +"T"
                +"T2"
            }
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["T"]).isAssignableFrom(
                Mirror.reflect(types["T2"])
            )
        )
    }

    @Test
    @DisplayName("Variable mirrors should be assignable from variables with double subclassed bounds")
    fun variableAssignableFromDoubleSubtypeBounds() {
        val sources = TestSources()
        val I by sources.add("I", "interface I {}")
        val J by sources.add("J", "interface J {}")
        val IJ by sources.add("IJ", "interface IJ extends I, J {}")
        val types = sources.types {
            typeVariables("T extends I & J", "T2 extends IJ") {
                +"T"
                +"T2"
            }
        }
        sources.compile()
        assertTrue(
            Mirror.reflect(types["T"]).isAssignableFrom(
                Mirror.reflect(types["T2"])
            )
        )
    }
}