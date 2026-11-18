package com.deference.koasty

@ConsistentCopyVisibility
data class KoastRequest internal constructor(
    val id: Long,
    val title: String,
    val message: String?,
    val lifetime: KoastLifetime,
    val dismissible: Boolean,
    val dismissOnClick: Boolean,
    val onClick: (() -> Unit)?,
    /**
     * Set by `show*(key = ...)`. A later koast with the same key replaces
     * this one in place.
     */
    val key: String? = null
)