package com.deference.utils

import android.util.Log

internal actual fun logPlatform(level: String, tag: String, message: String) {
    when (level) {
        "WARN" -> Log.w(tag, message)
        "INFO" -> Log.i(tag, message)
        "DEBUG" -> Log.d(tag, message)
        "RAW" -> Log.i(tag, message)
        else -> Log.i(tag, message)
    }
}
