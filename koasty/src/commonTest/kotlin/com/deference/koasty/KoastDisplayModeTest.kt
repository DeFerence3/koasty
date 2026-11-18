package com.deference.koasty

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class KoastDisplayModeTest {

    @Test
    fun sequential_isStackedShowingOnlyTheFrontCard() {
        val sequential = KoastDisplayMode.Sequential

        assertEquals(1, sequential.maxVisible)
        assertEquals(0.dp, sequential.peekOffset)
        assertEquals(0f, sequential.scaleStep)
        assertFalse(sequential.expandable)
    }

    @Test
    fun hostConfig_defaultsToSequential() {
        assertEquals(KoastDisplayMode.Sequential, KoastyHostConfig().displayMode)
    }
}
