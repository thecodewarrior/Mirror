package com.teamwizardry.mirror.testsupport

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.MirrorCache
import org.junit.jupiter.api.BeforeEach

internal open class MirrorTestBase {
    @BeforeEach
    fun initializeForTest() {
        Mirror.cache = MirrorCache()
    }
}