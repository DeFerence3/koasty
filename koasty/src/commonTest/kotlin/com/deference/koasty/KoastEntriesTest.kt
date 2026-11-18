package com.deference.koasty

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KoastEntriesTest {

    private fun List<KoastEntry>.titles() = map { it.koast.title }

    @Test
    fun sync_addsQueuedKoastsInQueueOrder() {
        val manager = KoastManager()
        val entries = KoastEntries()
        manager.show("First")
        manager.show("Second")

        val drawn = entries.sync(manager.queue)

        assertEquals(listOf("First", "Second"), drawn.titles())
        assertTrue(drawn.none { it.exiting })
    }

    @Test
    fun sync_keepsDismissedKoastAsExiting() {
        val manager = KoastManager()
        val entries = KoastEntries()
        manager.show("Only")
        entries.sync(manager.queue)

        manager.dismiss()
        val drawn = entries.sync(manager.queue)

        assertEquals(listOf("Only"), drawn.titles())
        assertTrue(drawn.single().exiting)
    }

    @Test
    fun sync_exitingKoastRemembersItsStackPosition() {
        val manager = KoastManager()
        val entries = KoastEntries()
        manager.show("First")
        val secondId = manager.show("Second")
        manager.show("Third")
        entries.sync(manager.queue)

        manager.dismiss(secondId)
        val drawn = entries.sync(manager.queue)

        val second = drawn.single { it.koast.id == secondId }
        assertTrue(second.exiting)
        assertEquals(1, second.exitIndex)
        assertEquals(1, drawn.single { it.koast.title == "Third" }.activeIndex)
    }

    @Test
    fun sync_dismissAllMarksEveryKoastExitingWithDistinctPositions() {
        val manager = KoastManager()
        val entries = KoastEntries()
        repeat(3) { manager.show("Koast $it") }
        entries.sync(manager.queue)

        manager.dismissAll()
        val drawn = entries.sync(manager.queue)

        assertTrue(drawn.all { it.exiting })
        assertEquals(listOf(0, 1, 2), drawn.map { it.exitIndex })
    }

    @Test
    fun sync_isIdempotentForTheSameQueue() {
        val manager = KoastManager()
        val entries = KoastEntries()
        manager.show("First")
        manager.show("Second")
        entries.sync(manager.queue)
        manager.dismiss()

        val once = entries.sync(manager.queue)
        val twice = entries.sync(manager.queue)

        assertEquals(once, twice)
        assertEquals(0, twice.first().exitIndex)
        assertEquals(0, twice.last().activeIndex)
    }

    @Test
    fun remove_dropsExitedKoast() {
        val manager = KoastManager()
        val entries = KoastEntries()
        manager.show("Only")
        entries.sync(manager.queue)
        manager.dismiss()
        val exiting = entries.sync(manager.queue).single()

        entries.remove(exiting)

        assertTrue(entries.sync(manager.queue).isEmpty())
    }

    @Test
    fun sync_newKoastJoinsWhileOldOneIsStillExiting() {
        val manager = KoastManager()
        val entries = KoastEntries()
        manager.show("Old")
        entries.sync(manager.queue)
        manager.dismiss()
        entries.sync(manager.queue)

        manager.show("New")
        val drawn = entries.sync(manager.queue)

        assertEquals(listOf("Old", "New"), drawn.titles())
        assertTrue(drawn[0].exiting)
        assertFalse(drawn[1].exiting)
        assertEquals(0, drawn[1].activeIndex)
    }

    @Test
    fun sync_keyedReplacementUpdatesEntryInPlace() {
        val manager = KoastManager()
        val entries = KoastEntries()
        manager.show("Uploading 40%", key = "upload")
        val before = entries.sync(manager.queue).single()

        manager.show("Uploading 80%", key = "upload")
        val after = entries.sync(manager.queue).single()

        assertTrue(before === after)
        assertFalse(after.exiting)
        assertEquals("Uploading 80%", after.koast.title)
    }

    @Test
    fun sync_koastMovingLanes_exitsOldLaneEntersNew() {
        val config = KoastyHostConfig(position = KoastPosition.TOP_CENTER)
        val manager = KoastManager()
        val top = KoastEntries()
        val bottom = KoastEntries()
        manager.show("Copying", key = "copy")
        top.sync(manager.queue.inLane(KoastPosition.TOP_CENTER, config))
        bottom.sync(manager.queue.inLane(KoastPosition.BOTTOM_CENTER, config))

        manager.showToast("Copied", key = "copy")
        val topDrawn = top.sync(manager.queue.inLane(KoastPosition.TOP_CENTER, config))
        val bottomDrawn = bottom.sync(manager.queue.inLane(KoastPosition.BOTTOM_CENTER, config))

        assertEquals(listOf("Copying"), topDrawn.titles())
        assertTrue(topDrawn.single().exiting)
        assertEquals(listOf("Copied"), bottomDrawn.titles())
        assertFalse(bottomDrawn.single().exiting)
    }
}
