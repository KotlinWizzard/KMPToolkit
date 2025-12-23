package io.github.kotlinwizzard.kmptoolkit.core.service.media

internal actual fun MediaCacheService.getUriPath(
    path: String
): String {
   return path
}

actual val MediaCacheService.Companion.CUSTOM_SCHEME: String
    get() = ""