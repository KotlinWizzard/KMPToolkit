package io.github.kotlinwizzard.kmptoolkit.core.service.media

import java.io.File

internal actual fun MediaCacheService.getUriPath(path: String): String {
  return  File(path).toURI().toString()
}

actual val MediaCacheService.Companion.CUSTOM_SCHEME: String
    get() = ""