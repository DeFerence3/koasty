package com.deference.koasty

interface Platform {
    val name: String
    val platform: Platforms
}

enum class Platforms{
    DESKTOP, ANDROID, IOS, WEB
}

expect fun getPlatform(): Platform
