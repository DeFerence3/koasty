package com.deference.koasty.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform