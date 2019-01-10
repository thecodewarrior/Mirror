package com.teamwizardry.mirror

import com.teamwizardry.mirror.testsupport.MirrorTestBase
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class MirrorTest: MirrorTestBase() {

    @Test
    @DisplayName("newAnnotation delegates to an external tested library")
    fun newAnnotation_needsNoTests() {
    }
}
