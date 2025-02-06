package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.mediator

import androidx.compose.runtime.State
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.PagingQueryKey

interface PagingKeyProvider<Key : PagingQueryKey> {
    val pagingKey: State<Key?>
}