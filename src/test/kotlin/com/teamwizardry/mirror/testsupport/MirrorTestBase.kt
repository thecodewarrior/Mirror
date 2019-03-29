package com.teamwizardry.mirror.testsupport

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.MirrorCache
import org.junit.jupiter.api.BeforeEach

open class MirrorTestBase {
    @BeforeEach
    fun beforeEachTest() {
        this.initializeForTest()
    }

    open fun initializeForTest() {
        Mirror.cache = MirrorCache()
        Mirror._types = Mirror.Types()
    }
}