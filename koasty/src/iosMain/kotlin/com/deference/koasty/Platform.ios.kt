package com.deference.koasty

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val platform: Platforms = Platforms.IOS
}

actual fun getPlatform(): Platform = IOSPlatform()