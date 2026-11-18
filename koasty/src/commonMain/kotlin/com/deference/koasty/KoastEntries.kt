package com.deference.koasty

/**
 * A koast the host is drawing. It stays drawn while it animates out,
 * after the manager has already removed it from the queue.
 */
internal class KoastEntry(koast: KoastRequest) {
    // The latest request for this id; a keyed koast can be replaced in place.
    var koast: KoastRequest = koast

    var exiting: Boolean = false

    // Position among the queued (non-exiting) koasts at the last sync.
    var activeIndex: Int = 0

    // Stack position the koast had when it started leaving.
    var exitIndex: Int = 0
}

/**
 * The koasts a host draws: everything in the manager's queue, plus koasts
 * that already left the queue but are still animating out.
 *
 * The queue alone can't drive the host, because a dismissed koast is gone
 * from it immediately and its card would vanish without an exit animation.
 *
 * This is a plain holder, not snapshot state: [sync] runs during
 * composition with the queue the host just read, and is idempotent, so a
 * discarded composition does no harm.
 */
internal class KoastEntries {
    private val entries = mutableListOf<KoastEntry>()

    /**
     * Brings the entries in line with [queue] and returns them in drawing
     * order (queue order, with exiting koasts kept where they were).
     *
     * - A koast new to the queue gets a fresh entry at the end.
     * - A koast replaced by key (same id, new request) keeps its entry,
     *   which picks up the new request.
     * - A koast that left the queue is marked exiting, remembering the
     *   stack position it had, and stays until [remove] is called.
     */
    fun sync(queue: List<KoastRequest>): List<KoastEntry> {
        val queued = queue.associateBy { it.id }
        val knownIds = entries.mapTo(HashSet(entries.size)) { it.koast.id }

        for (entry in entries) {
            if (entry.exiting) continue
            val latest = queued[entry.koast.id]
            if (latest == null) {
                entry.exiting = true
                entry.exitIndex = entry.activeIndex
            } else {
                entry.koast = latest
            }
        }

        for (koast in queue) {
            if (koast.id !in knownIds) entries += KoastEntry(koast)
        }

        var activeIndex = 0
        for (entry in entries) {
            if (!entry.exiting) entry.activeIndex = activeIndex++
        }

        return entries.toList()
    }

    /** Drops an exiting entry once its exit animation has finished. */
    fun remove(entry: KoastEntry) {
        entries.remove(entry)
    }
}
