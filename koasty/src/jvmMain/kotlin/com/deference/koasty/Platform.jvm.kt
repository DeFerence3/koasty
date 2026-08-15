package com.deference.koasty

private class JvmPlatform : Platform {
    override val name: String = "Desktop JVM"
    override val platform: Platforms = Platforms.DESKTOP
}

actual fun getPlatform(): Platform = JvmPlatform()
