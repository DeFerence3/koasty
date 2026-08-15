package com.deference.koasty

import kotlin.time.Duration

sealed interface KoastLifetime {

    data class Timed(
        val millis: Duration
    ) : KoastLifetime {
        init {
            require(millis.isPositive())
        }
    }

    data object UntilDismissed : KoastLifetime
}