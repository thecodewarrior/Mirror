package dev.thecodewarrior.mirror.type

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.time.Duration

internal class SelfReferentialTest : MirrorTestBase() {

    @Test
    fun reflect_withSelfReferentialTypeParameter_shouldNotDeadlock() {
        open class SelfReferential<T: SelfReferential<T>> { }
        assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            Mirror.reflect<SelfReferential<*>>()
        }
    }

    @Test
    fun reflect_withSelfReferentialConstructorParameter_shouldNotDeadlock() {
        open class SelfReferential(foo: SelfReferential?) { }
        assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            Mirror.reflect<SelfReferential>()
        }
    }

    @Test
    fun reflect_withSelfReferentialMethodParameter_shouldNotDeadlock() {
        open class SelfReferential {
            fun method(foo: SelfReferential?) {}
        }
        assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            Mirror.reflect<SelfReferential>()
        }
    }

    @Test
    fun reflect_withSelfReferentialMethodReturn_shouldNotDeadlock() {
        open class SelfReferential {
            fun method(): SelfReferential = this
        }
        assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            Mirror.reflect<SelfReferential>()
        }
    }

    @Test
    fun reflect_withSelfReferentialField_shouldNotDeadlock() {
        open class SelfReferential {
            @JvmField
            val field = this
        }
        assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            Mirror.reflect<SelfReferential>()
        }
    }

    @Test
    fun reflect_withSelfReferentialInnerClass_shouldNotDeadlock() {
        open class SelfReferential {
            inner class Inner : SelfReferential()
        }
        assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            Mirror.reflect<SelfReferential>()
        }
    }

    @Test
    fun reflect_withSelfReferentialInnerClassGeneric_shouldNotDeadlock() {
        open class SelfReferential {
            inner class Inner<T: SelfReferential>()
        }
        assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            Mirror.reflect<SelfReferential>()
        }
    }


    @Test
    fun reflect_withSelfReferentialSuperClassGeneric_shouldNotDeadlock() {
        open class Generic<T>
        open class SelfReferential: Generic<SelfReferential>()
        assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            Mirror.reflect<SelfReferential>()
        }
    }
}