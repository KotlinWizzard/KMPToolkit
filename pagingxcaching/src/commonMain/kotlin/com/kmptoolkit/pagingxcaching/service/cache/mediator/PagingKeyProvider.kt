package com.kmptoolkit.pagingxcaching.service.cache.mediator

import androidx.compose.runtime.State
import com.kmptoolkit.pagingxcaching.service.room.key.PagingQueryKey

interface PagingKeyProvider<Key : PagingQueryKey> {
    val pagingKey: State<Key?>
}