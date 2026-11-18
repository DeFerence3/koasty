package com.deference.koasty

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KoastManagerTest {

    private fun KoastManager.titles() = queue.map { it.title }

    @Test
    fun show_queuesKoastsInsteadOfReplacing() {
        val manager = KoastManager()

        manager.show("First")
        manager.show("Second")
        manager.show("Third")

        assertEquals(listOf("First", "Second", "Third"), manager.titles())
        assertEquals("First", manager.currentKoast?.title)
    }

    @Test
    fun dismiss_removesHeadFirstInFirstOut() {
        val manager = KoastManager()
        manager.show("First")
        manager.showUntilDismissed("Second")
        manager.showPersistent("Third")

        manager.dismiss()
        assertEquals("Second", manager.currentKoast?.title)

        manager.dismiss()
        assertEquals("Third", manager.currentKoast?.title)

        manager.dismiss()
        assertNull(manager.currentKoast)
        assertFalse(manager.isShowing)
    }

    @Test
    fun dismiss_onEmptyQueueDoesNothing() {
        val manager = KoastManager()

        manager.dismiss()
        manager.dismissAll()

        assertTrue(manager.queue.isEmpty())
    }

    @Test
    fun dismissById_removesQueuedKoastWithoutTouchingFront() {
        val manager = KoastManager()
        manager.show("First")
        val second = manager.show("Second")
        manager.show("Third")

        assertTrue(manager.dismiss(second))

        assertEquals(listOf("First", "Third"), manager.titles())
    }

    @Test
    fun dismiss_ignoresStaleId() {
        val manager = KoastManager()
        val first = manager.show("First")
        manager.show("Second")

        assertTrue(manager.dismiss(first))
        assertFalse(manager.dismiss(first))

        assertEquals(listOf("Second"), manager.titles())
    }

    @Test
    fun dismissAll_clearsFrontAndQueue() {
        val manager = KoastManager()
        manager.show("First")
        manager.show("Second")

        manager.dismissAll()

        assertTrue(manager.queue.isEmpty())
    }

    @Test
    fun show_returnsIncreasingIdsAfterDismissal() {
        val manager = KoastManager()
        val first = manager.show("First")
        manager.dismissAll()
        val second = manager.show("Second")

        assertTrue(second > first)
        assertEquals(second, manager.currentKoast?.id)
    }

    @Test
    fun show_storesStyle() {
        val manager = KoastManager()
        val style = KoastStyle(position = KoastPosition.BOTTOM_END)

        manager.show("Styled", style = style)
        manager.show("Plain")

        assertEquals(style, manager.queue[0].style)
        assertNull(manager.queue[1].style)
    }

    @Test
    fun showClickable_storesStyle() {
        val manager = KoastManager()

        manager.showClickable("Clickable", style = KoastStyle.Info) {}
        manager.showPersistent("Persistent", style = KoastStyle.Warning)

        assertEquals(listOf(KoastStyle.Info, KoastStyle.Warning), manager.queue.map { it.style })
    }

    @Test
    fun showSuccess_usesSuccessPreset() {
        val manager = KoastManager()

        manager.showSuccess("Saved")
        manager.showError("Failed")
        manager.showToast("Copied")

        assertEquals(
            listOf(KoastStyle.Success, KoastStyle.Error, KoastStyle.Toast),
            manager.queue.map { it.style }
        )
        assertFalse(manager.queue.last().dismissible)
    }

    @Test
    fun show_sameKeyReplacesStyle() {
        val manager = KoastManager()
        val id = manager.showInfo("Uploading", key = "upload")

        val sameId = manager.showSuccess("Uploaded", key = "upload")

        assertEquals(id, sameId)
        assertEquals(KoastStyle.Success, manager.currentKoast?.style)
        assertEquals(1, manager.queue.size)
    }
}
