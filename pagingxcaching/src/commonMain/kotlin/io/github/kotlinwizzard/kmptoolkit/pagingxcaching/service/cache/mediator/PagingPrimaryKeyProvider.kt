package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.mediator

import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.PagingPrimaryKey

interface PagingPrimaryKeyProvider {
    fun getPagingPrimaryKey(): PagingPrimaryKey
}