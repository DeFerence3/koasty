package com.deference.koasty

internal data class KoastManagerState(
    val sequence: Long = 0L,
    //koasts that are queued for showing,
    // the first one is currently showing [val current]
    val queue: List<KoastRequest> = emptyList()
) {
    val current: KoastRequest?
        get() = queue.firstOrNull()
}
