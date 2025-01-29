package com.kmptoolkit.pagingxcaching.service.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kmptoolkit.pagingxcaching.service.paging.exceptions.ResponseEmptyException


data class ApiPagingSource<D : Any>(
    var loadData: suspend (pageNumber: Int) -> PageImpl<D>,
) : PagingSource<Int, D>() {
    private val startKey = 0

    override fun getRefreshKey(state: PagingState<Int, D>) = startKey

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, D> {
        try {
            val nextPageNumber = params.key ?: startKey
            val response = loadData(nextPageNumber)
            if (nextPageNumber == startKey && response.content.isEmpty()) {
                throw ResponseEmptyException()
            }
            val itemsAfter =
                if (response.nextPageNumber == null) {
                    0
                } else {
                    response.content.size
                }

            return LoadResult.Page(
                data = response.content,
                prevKey = response.previousPageNumber,
                nextKey = response.nextPageNumber,
                itemsBefore = 0,
                itemsAfter = itemsAfter,
            )
        } catch (e: kotlinx.io.IOException) {
            return LoadResult.Error(e)
        } catch (e: ResponseEmptyException) {
            return LoadResult.Error(e)
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}
