package io.github.kotlinwizzard.kmptoolkit.image.core.sketch

import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.source.ByteArrayDataSource
import com.github.panpf.sketch.source.DataFrom
import com.github.panpf.sketch.util.Uri
import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService


class LocalCacheFetcher(val requestUri: String) : Fetcher {
    override suspend fun fetch(): Result<FetchResult> {
        val bytes =   MediaCacheService.readCachedFileOrNull(requestUri) ?: return Result.failure(
            Exception("Cannot find local file"))
        val fetchResult = FetchResult(
            dataSource =   ByteArrayDataSource(bytes, DataFrom.MEMORY_CACHE),
            mimeType = "image/*"
        )
        return Result.success(fetchResult)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as LocalCacheFetcher
        if (requestUri != other.requestUri) return false
        return true
    }

    override fun hashCode(): Int {
        return requestUri.hashCode()
    }

    override fun toString(): String {
        return "LocalCacheFetcher(requestUri='$requestUri')"
    }

    class Factory : Fetcher.Factory {

        override fun create(requestContext: RequestContext): LocalCacheFetcher? {
            val uri: Uri = requestContext.request.uri
            if (!isLocalCacheUri(uri.toString())) return null
            return LocalCacheFetcher(uri.toString())
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String = "BlurHashUriFetcher"
    }
}