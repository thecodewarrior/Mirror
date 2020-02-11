package dev.thecodewarrior.mirror.type.classmirror

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.member.Modifier
import dev.thecodewarrior.mirror.testsupport.ClosedObject1
import dev.thecodewarrior.mirror.testsupport.CompanionHolder
import dev.thecodewarrior.mirror.testsupport.DataObject1
import dev.thecodewarrior.mirror.testsupport.EnumClass1
import dev.thecodewarrior.mirror.testsupport.Interface1
import dev.thecodewarrior.mirror.testsupport.KotlinInternalClass
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import dev.thecodewarrior.mirror.testsupport.Object1
import dev.thecodewarrior.mirror.testsupport.SealedClass
import dev.thecodewarrior.mirror.testsupport.simpletypes.JObject1
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.typeholders.classmirror.SimpleHelpersHolder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class SimpleHelpersTest: MirrorTestBase(SimpleHelpersHolder()) {
    val _holder = holder as SimpleHelpersHolder

    @Test
    fun access_ofJavaClass_shouldBeCorrect() {
        Assertions.assertEquals(Modifier.Access.PUBLIC, Mirror.reflectClass(holder.getClass("public class")).access)
        Assertions.assertEquals(Modifier.Access.DEFAULT, Mirror.reflectClass(holder.getClass("default class")).access)
        Assertions.assertEquals(Modifier.Access.PROTECTED, Mirror.reflectClass(holder.getClass("protected class")).access)
        Assertions.assertEquals(Modifier.Access.PRIVATE, Mirror.reflectClass(holder.getClass("private class")).access)
    }

    @Test
    fun access_ofKotlinInternalClass_shouldBePublicAndInternal() {
        Assertions.assertEquals(Modifier.Access.PUBLIC, Mirror.reflectClass<KotlinInternalClass>().access)
        Assertions.assertTrue(Mirror.reflectClass<KotlinInternalClass>().isInternalAccess)
    }

    @Test
    fun modifiers_ofJavaClass_shouldBeCorrect() {
        fun test(name: String, vararg mods: Modifier) = Assertions.assertEquals(setOf(*mods), Mirror.reflectClass(holder.getClass(name)).modifiers)
        test("public class", Modifier.PUBLIC)
        test("default class")
        test("protected class", Modifier.PROTECTED)
        test("private class", Modifier.PRIVATE)
        test("abstract class", Modifier.ABSTRACT)
        test("static class", Modifier.STATIC)
        test("final class", Modifier.FINAL)
        // TODO Strictfp flag missing from java modifiers
        // test("strictfp class", Modifier.STRICT)
    }

    private inline fun <reified T> testFlags(vararg flags: ClassMirror.Flag) {
        Assertions.assertEquals(setOf(*flags), Mirror.reflectClass<T>().flags)
    }

    private fun testFlags(name: String, vararg flags: ClassMirror.Flag) {
        Assertions.assertEquals(setOf(*flags), Mirror.reflectClass(holder.getClass(name)).flags)
    }

    @Test
    fun kotlinFlags_ofKotlinClass_shouldBeCorrect() {
        assertAll(
            { testFlags<ClosedObject1>(ClassMirror.Flag.FINAL) },
            { testFlags<Object1>() },
            { testFlags<CompanionHolder.Companion>(ClassMirror.Flag.FINAL, ClassMirror.Flag.STATIC, ClassMirror.Flag.MEMBER)
                Assertions.assertTrue(Mirror.reflectClass<CompanionHolder.Companion>().isCompanion) },
            { testFlags<DataObject1>(ClassMirror.Flag.FINAL)
                Assertions.assertTrue(Mirror.reflectClass<DataObject1>().isData) },
            { testFlags<SealedClass>(ClassMirror.Flag.ABSTRACT)
                Assertions.assertTrue(Mirror.reflectClass<SealedClass>().isSealed) },
            { testFlags<Interface1>(ClassMirror.Flag.INTERFACE, ClassMirror.Flag.ABSTRACT) }
        )
    }

    @Test
    fun flags_ofClasses_shouldBeCorrect() {
        assertAll(
            { testFlags("public static class", ClassMirror.Flag.MEMBER) },
            { testFlags("public class", ClassMirror.Flag.MEMBER) },
            { testFlags("default class", ClassMirror.Flag.MEMBER) },
            { testFlags("protected class", ClassMirror.Flag.MEMBER) },
            { testFlags("private class", ClassMirror.Flag.MEMBER) },
            { testFlags("abstract class", ClassMirror.Flag.MEMBER, ClassMirror.Flag.ABSTRACT) },
            { testFlags("static class", ClassMirror.Flag.MEMBER, ClassMirror.Flag.STATIC) },
            { testFlags("final class", ClassMirror.Flag.MEMBER, ClassMirror.Flag.FINAL) },
            // TODO Strictfp flag missing from java modifiers
            // { testFlags("strictfp class", Flag.MEMBER, Flag.STRICT) },
            { testFlags("annotation class", ClassMirror.Flag.MEMBER, ClassMirror.Flag.INTERFACE, ClassMirror.Flag.ABSTRACT, ClassMirror.Flag.ANNOTATION, ClassMirror.Flag.STATIC) },
            { testFlags("interface", ClassMirror.Flag.MEMBER, ClassMirror.Flag.STATIC, ClassMirror.Flag.INTERFACE, ClassMirror.Flag.ABSTRACT) },
            { Assertions.assertEquals(setOf(ClassMirror.Flag.ANONYMOUS), Mirror.reflectClass(_holder.innerAnonymous.javaClass).flags) },
            { Assertions.assertEquals(setOf(ClassMirror.Flag.ANONYMOUS), Mirror.reflectClass(_holder.anonymous.javaClass).flags) },
            { Assertions.assertEquals(setOf(ClassMirror.Flag.LOCAL), Mirror.reflectClass(_holder.local).flags) },
            { Assertions.assertEquals(setOf(ClassMirror.Flag.FINAL, ClassMirror.Flag.SYNTHETIC), Mirror.reflectClass(_holder.lambda.javaClass).flags) },
            { Assertions.assertEquals(setOf(ClassMirror.Flag.ABSTRACT, ClassMirror.Flag.FINAL, ClassMirror.Flag.PRIMITIVE), Mirror.types.int.flags) },
            { testFlags<JObject1>() },
            { testFlags<EnumClass1>(ClassMirror.Flag.ENUM) }
        )
    }

    // todo annotations, declaredAnnotations, simpleName, name, canonicalName

    @Test
    fun enumType_ofNonEnum_shouldReturnNull() {
        Assertions.assertNull(Mirror.reflectClass<Object1>().enumType)
    }

    @Test
    fun enumType_ofEnumClass_shouldReturnSelf() {
        Assertions.assertEquals(Mirror.reflectClass<EnumClass1>(), Mirror.reflectClass<EnumClass1>().enumType)
    }

    @Test
    fun enumType_ofAnonymousEnumSubclass_shouldReturnEnumClass() {
        Assertions.assertEquals(Mirror.reflectClass<EnumClass1>(), Mirror.reflectClass(EnumClass1.ANONYMOUS.javaClass).enumType)
    }

    @Test
    fun enumConstants_ofNonEnum_shouldReturnNull() {
        Assertions.assertNull(Mirror.reflectClass<Object1>().enumConstants)
    }

    @Test
    fun enumConstants_ofEnumClass_shouldReturnConstants() {
        Assertions.assertEquals(listOf(*EnumClass1.values()), Mirror.reflectClass<EnumClass1>().enumConstants)
    }

    @Test
    fun enumConstants_ofAnonymousEnumSubclass_shouldReturnNull() {
        Assertions.assertNull(Mirror.reflectClass(EnumClass1.ANONYMOUS.javaClass).enumConstants)
    }

}