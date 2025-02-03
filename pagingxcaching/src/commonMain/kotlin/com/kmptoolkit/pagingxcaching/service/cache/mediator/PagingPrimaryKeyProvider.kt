package com.kmptoolkit.pagingxcaching.service.cache.mediator

import com.kmptoolkit.pagingxcaching.service.room.key.PagingPrimaryKey

interface PagingPrimaryKeyProvider {
    fun getPagingPrimaryKey(): PagingPrimaryKey
}