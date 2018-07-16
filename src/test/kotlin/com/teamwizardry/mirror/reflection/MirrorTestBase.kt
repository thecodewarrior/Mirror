package com.teamwizardry.mirror.reflection

import org.junit.jupiter.api.BeforeEach

internal open class MirrorTestBase {
    @BeforeEach
    fun initializeForTest() {
        Mirror.cache = MirrorCache()
    }
}