package com.deference.koasty

private class JsPlatform : Platform {
    override val name: String = "JavaScript"
    override val platform: Platforms = Platforms.WEB
}

actual fun getPlatform(): Platform = JsPlatform()
