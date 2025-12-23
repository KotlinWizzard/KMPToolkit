package io.github.kotlinwizzard.kmptoolkit.core.service.media

internal expect fun MediaCacheService.getUriPath(path: String): String

expect val MediaCacheService.Companion.CUSTOM_SCHEME: String