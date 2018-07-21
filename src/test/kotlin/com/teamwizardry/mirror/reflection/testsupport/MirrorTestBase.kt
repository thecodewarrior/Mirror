package com.teamwizardry.mirror.reflection.testsupport

import com.teamwizardry.mirror.reflection.Mirror
import com.teamwizardry.mirror.reflection.MirrorCache
import org.junit.jupiter.api.BeforeEach

internal open class MirrorTestBase {
    @BeforeEach
    fun initializeForTest() {
        Mirror.cache = MirrorCache()
    }
}