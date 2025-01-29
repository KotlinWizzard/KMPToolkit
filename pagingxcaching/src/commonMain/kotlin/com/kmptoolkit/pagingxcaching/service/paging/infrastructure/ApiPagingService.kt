package com.kmptoolkit.pagingxcaching.service.paging.infrastructure

import com.kmptoolkit.pagingxcaching.service.paging.ApiPagingSource
import com.kmptoolkit.pagingxcaching.service.paging.PageImpl

/***
 * @param PageDTO is the paged object from the backend
 * @param DTO is the DTO used in PageDTO
 * @param Actual is the actual model, which will be mapped
 */
abstract class ApiPagingService<PageDTO : Any, DTO : Any, Actual : Any>(
) : BasicApiNetworkPagingService<Actual>() {
    override fun getPagingSource() = ApiPagingSource<Actual> { getPaginatedData(it) }

    private suspend fun getPaginatedData(page: Int): PageImpl<Actual> =
        mapPage(fetchPagingData(page).toPage())

    abstract fun PageDTO.toPage(): PageImpl<DTO>

    abstract fun DTO.toActual(): Actual

    private fun mapPage(pageImpl: PageImpl<DTO>): PageImpl<Actual> =
        pageImpl.mapNotNull { it.toActual() }

    abstract suspend fun fetchPagingData(page: Int): PageDTO
}