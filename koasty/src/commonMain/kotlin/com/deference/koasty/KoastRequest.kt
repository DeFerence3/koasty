package com.deference.koasty

@ConsistentCopyVisibility
data class KoastRequest internal constructor(
    val id: Long,
    val title: String,
    val message: String?,
    val lifetime: KoastLifetime,
    val dismissible: Boolean,
    val dismissOnClick: Boolean,
    val onClick: (() -> Unit)?
)