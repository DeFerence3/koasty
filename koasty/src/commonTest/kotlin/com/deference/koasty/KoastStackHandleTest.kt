package com.deference.koasty

import kotlin.test.Test
import kotlin.test.assertEquals

class KoastStackHandleTest {

    @Test
    fun label_usesSingularForOneMoreKoast() {
        assertEquals("1 more koast", stackHandleLabel(moreCount = 1, expanded = false))
    }

    @Test
    fun label_countsKoastsBehindTheFrontOne() {
        assertEquals("9 more koasts", stackHandleLabel(moreCount = 9, expanded = false))
    }

    @Test
    fun label_offersToCollapseWhileExpanded() {
        assertEquals("Show less", stackHandleLabel(moreCount = 9, expanded = true))
    }
}
