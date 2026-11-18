package com.deference.koasty

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KoastStyleTest {

    private val config = KoastyHostConfig(
        position = KoastPosition.TOP_CENTER,
        compact = false,
        containerColor = Color.Gray,
        contentColor = Color.White,
        horizontalSwipeEnabled = true,
        swipeUpEnabled = true
    )

    @Test
    fun resolve_nullStyleUsesHostConfig() {
        val resolved = config.resolve(null)

        assertEquals(config.position, resolved.position)
        assertEquals(config.compact, resolved.compact)
        assertEquals(config.maxWidth, resolved.maxWidth)
        assertEquals(config.minHeight, resolved.minHeight)
        assertEquals(config.containerColor, resolved.containerColor)
        assertEquals(config.contentColor, resolved.contentColor)
        assertEquals(config.shape, resolved.shape)
        assertEquals(config.shadowElevation, resolved.shadowElevation)
        assertEquals(config.contentPadding, resolved.contentPadding)
        assertEquals(PaddingValues(0.dp), resolved.outerPadding)
        assertEquals(config.titleStyle, resolved.titleStyle)
        assertEquals(config.messageStyle, resolved.messageStyle)
        assertTrue(resolved.horizontalSwipeEnabled)
        assertTrue(resolved.swipeUpEnabled)
        assertTrue(resolved.stackable)
        assertNull(resolved.content)
    }

    @Test
    fun resolve_overridesOnlySetFields() {
        val resolved = config.resolve(
            KoastStyle(containerColor = Color.Red, swipeUpEnabled = false)
        )

        assertEquals(Color.Red, resolved.containerColor)
        assertFalse(resolved.swipeUpEnabled)
        assertEquals(config.contentColor, resolved.contentColor)
        assertEquals(config.position, resolved.position)
        assertEquals(config.shape, resolved.shape)
        assertTrue(resolved.horizontalSwipeEnabled)
    }

    @Test
    fun statusPresets_onlySetColors() {
        for (preset in listOf(KoastStyle.Success, KoastStyle.Error, KoastStyle.Warning, KoastStyle.Info)) {
            assertNotNull(preset.containerColor)
            assertNotNull(preset.contentColor)
            assertEquals(
                KoastStyle(),
                preset.copy(containerColor = null, contentColor = null)
            )
        }
    }

    @Test
    fun toastPreset_bottomCenterWithSwipesOff() {
        val resolved = config.resolve(KoastStyle.Toast)

        assertEquals(KoastPosition.BOTTOM_CENTER, resolved.position)
        assertTrue(resolved.compact)
        assertEquals(CircleShape, resolved.shape)
        assertFalse(resolved.horizontalSwipeEnabled)
        assertFalse(resolved.swipeUpEnabled)
        assertFalse(resolved.stackable)
        assertNotNull(resolved.content)
    }

    @Test
    fun laneDisplayMode_noOptOutUsesHostMode() {
        val stacked = KoastDisplayMode.Stacked(maxVisible = 3)
        val host = config.copy(displayMode = stacked)
        val manager = KoastManager()
        manager.show("Plain")
        manager.showSuccess("Styled")

        assertEquals(stacked, manager.queue.laneDisplayMode(host))
    }

    @Test
    fun laneDisplayMode_toastForcesSequential() {
        val host = config.copy(displayMode = KoastDisplayMode.Stacked(maxVisible = 3))
        val manager = KoastManager()
        manager.show("Plain")
        manager.showToast("Copied")

        assertEquals(KoastDisplayMode.Sequential, manager.queue.laneDisplayMode(host))
    }

    @Test
    fun laneDisplayMode_returnsToHostModeOnceToastLeaves() {
        val stacked = KoastDisplayMode.Stacked(maxVisible = 3)
        val host = config.copy(displayMode = stacked)
        val manager = KoastManager()
        val toastId = manager.showToast("Copied")
        manager.show("Plain")

        manager.dismiss(toastId)

        assertEquals(stacked, manager.queue.laneDisplayMode(host))
    }

    @Test
    fun inLane_splitsByResolvedPositionKeepingOrder() {
        val manager = KoastManager()
        manager.show("Top 1")
        manager.showToast("Bottom 1")
        manager.show("Top 2")
        manager.showToast("Bottom 2")

        val top = manager.queue.inLane(KoastPosition.TOP_CENTER, config)
        val bottom = manager.queue.inLane(KoastPosition.BOTTOM_CENTER, config)

        assertEquals(listOf("Top 1", "Top 2"), top.map { it.title })
        assertEquals(listOf("Bottom 1", "Bottom 2"), bottom.map { it.title })
        assertTrue(manager.queue.inLane(KoastPosition.TOP_END, config).isEmpty())
    }

    @Test
    fun inLane_unstyledKoastUsesHostPosition() {
        val manager = KoastManager()
        manager.show("Plain")
        manager.showSuccess("Styled, no position")
        val bottomHost = config.copy(position = KoastPosition.BOTTOM_START)

        val lane = manager.queue.inLane(KoastPosition.BOTTOM_START, bottomHost)

        assertEquals(listOf("Plain", "Styled, no position"), lane.map { it.title })
        assertTrue(manager.queue.inLane(KoastPosition.TOP_CENTER, bottomHost).isEmpty())
    }
}
