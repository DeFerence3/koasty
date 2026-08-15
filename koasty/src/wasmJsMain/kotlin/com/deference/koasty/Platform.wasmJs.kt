package com.deference.koasty

private class WasmPlatform : Platform {
    override val name: String = "WebAssembly"
    override val platform: Platforms = Platforms.WEB
}

actual fun getPlatform(): Platform = WasmPlatform()
