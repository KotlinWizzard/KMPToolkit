package com.kmptoolkit.pagingxcaching.service.room.key

data object NotInitialized : PagingQueryKey() {
    override fun getQueryIdentifier(): String = "NotInitialized"
}