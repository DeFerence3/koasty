package com.deference.koasty

internal data class KoastManagerState(
    val sequence: Long = 0L,
    val current: KoastRequest? = null
)
