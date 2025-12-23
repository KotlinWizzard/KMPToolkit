package io.github.kotlinwizzard.kmptoolkit.image.core.sketch

import com.github.panpf.sketch.ComponentRegistry
import io.github.kotlinwizzard.kmptoolkit.core.service.media.CUSTOM_SCHEME
import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService

fun ComponentRegistry.Builder.supportLocalCache(): ComponentRegistry.Builder = apply {
    addFetcher(LocalCacheFetcher.Factory())
}


internal fun isLocalCacheUri(uri: String): Boolean = uri.startsWith(MediaCacheService.CUSTOM_SCHEME)
