package com.deference.koasty

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KoastManagerKeyTest {

    private fun KoastManager.titles() = queue.map { it.title }

    @Test
    fun show_sameKeyReplacesInPlaceKeepingId() {
        val manager = KoastManager()
        val firstId = manager.show("Uploading 40%", key = "upload")

        val secondId = manager.show("Uploading 80%", key = "upload")

        assertEquals(firstId, secondId)
        assertEquals(listOf("Uploading 80%"), manager.titles())
        assertEquals("upload", manager.currentKoast?.key)
    }

    @Test
    fun show_sameKeyKeepsQueuePosition() {
        val manager = KoastManager()
        manager.show("Front")
        manager.show("Uploading 40%", key = "upload")
        manager.show("Back")

        manager.show("Uploading 80%", key = "upload")

        assertEquals(listOf("Front", "Uploading 80%", "Back"), manager.titles())
    }

    @Test
    fun show_differentKeysQueueSeparately() {
        val manager = KoastManager()
        manager.show("Upload", key = "upload")
        manager.show("Download", key = "download")

        assertEquals(listOf("Upload", "Download"), manager.titles())
    }

    @Test
    fun show_nullKeyNeverReplaces() {
        val manager = KoastManager()
        manager.show("First")
        manager.show("Second")

        assertEquals(listOf("First", "Second"), manager.titles())
    }

    @Test
    fun show_keyedKoastDoesNotReplaceUnkeyedOne() {
        val manager = KoastManager()
        manager.show("Plain")
        manager.show("Keyed", key = "k")

        assertEquals(listOf("Plain", "Keyed"), manager.titles())
    }

    @Test
    fun show_replacementTakesNewLifetimeAndBehavior() {
        val manager = KoastManager()
        manager.showPersistent("Uploading", key = "upload")

        manager.show("Done", key = "upload")

        val koast = manager.currentKoast!!
        assertTrue(koast.lifetime is KoastLifetime.Timed)
        assertTrue(koast.dismissible)
    }

    @Test
    fun show_afterKeyedKoastWasDismissedQueuesNewOne() {
        val manager = KoastManager()
        val oldId = manager.show("Old", key = "k")
        manager.dismiss(oldId)

        val newId = manager.show("New", key = "k")

        assertTrue(newId != oldId)
        assertEquals(listOf("New"), manager.titles())
    }

    @Test
    fun dismissById_removesReplacedKoast() {
        val manager = KoastManager()
        val id = manager.show("Uploading 40%", key = "upload")
        manager.show("Uploading 80%", key = "upload")

        assertTrue(manager.dismiss(id))
        assertFalse(manager.isShowing)
    }

    @Test
    fun dismissRequest_ignoresStaleRequestAfterReplacement() {
        val manager = KoastManager()
        manager.show("Uploading 40%", key = "upload")
        val stale = manager.currentKoast!!
        manager.show("Uploading 80%", key = "upload")

        assertFalse(manager.dismiss(stale))
        assertEquals(listOf("Uploading 80%"), manager.titles())
    }

    @Test
    fun dismissRequest_removesCurrentRequest() {
        val manager = KoastManager()
        manager.show("Only", key = "k")

        assertTrue(manager.dismiss(manager.currentKoast!!))
        assertFalse(manager.isShowing)
    }
}
