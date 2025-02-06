package io.github.kotlinwizzard.kmptoolkit.core.service.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalCache = staticCompositionLocalOf<MediaCache> {
    MediaCache(
        MediaCacheService.Image(),
        MediaCacheService.Video()
    )
}

@Composable
fun CacheServiceProvider(
    imageCacheBasePath: String = MediaCacheService.Image.DEFAULT_BASE_PATH,
    videoCacheBasePath: String = MediaCacheService.Video.DEFAULT_BASE_PATH,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCache provides MediaCache(
            MediaCacheService.Image(imageCacheBasePath),
            MediaCacheService.Video(videoCacheBasePath)
        ),
        content
    )
}

data class MediaCache(
    val imageCache: MediaCacheService.Image,
    val videoCache: MediaCacheService.Video
)