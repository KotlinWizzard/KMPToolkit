package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key

data object NotInitialized : PagingQueryKey() {
    override fun getQueryIdentifier(): String = "NotInitialized"
}