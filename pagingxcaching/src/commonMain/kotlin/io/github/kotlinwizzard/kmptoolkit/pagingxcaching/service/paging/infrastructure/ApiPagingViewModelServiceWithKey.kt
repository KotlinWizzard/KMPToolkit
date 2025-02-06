package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.infrastructure

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.ApiPagingSource
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.PageImpl

abstract class ApiPagingViewModelServiceWithKey<PageDTO : Any, DTO : Any, Actual : Any, Key : Any>(
) : BasicApiNetworkPagingViewModelService<Actual>() {
    var key: Key? by mutableStateOf(null)
        private set

    override fun getPagingSource() = ApiPagingSource { getPaginatedData(it) }

    private suspend fun getPaginatedData(page: Int): PageImpl<Actual> =
        mapPage(fetchPagingData(page, key).toPage())


    abstract fun PageDTO.toPage(): PageImpl<DTO>

    abstract fun DTO.toActual(): Actual

    private fun mapPage(pageImpl: PageImpl<DTO>): PageImpl<Actual> =
        pageImpl.mapNotNull { it.toActual() }

    abstract suspend fun fetchPagingData(
        page: Int,
        key: Key?,
    ): PageDTO

    protected open fun shouldLoadWithKey(key: Key): Boolean = true

    protected open fun areKeysTheSame(
        currentKey: Key,
        lastKey: Key,
    ): Boolean = currentKey == lastKey

    open fun loadWithKey(
        key: Key,
        checkIfKeyChanged: Boolean = false,
    ) {
        val lastKey = this.key
        if (checkIfKeyChanged &&
            lastKey != null &&
            areKeysTheSame(
                currentKey = key,
                lastKey = lastKey,
            )
        ) {
            return
        }
        if (!shouldLoadWithKey(key)) return
        this.key = key
        triggerRefresh()
    }

    fun updateKey(key: Key) {
        this.key = key
    }
}