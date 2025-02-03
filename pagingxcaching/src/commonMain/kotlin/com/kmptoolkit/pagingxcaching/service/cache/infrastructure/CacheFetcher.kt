package com.kmptoolkit.pagingxcaching.service.cache.infrastructure

import com.kmptoolkit.pagingxcaching.service.api.ApiResult
import com.kmptoolkit.pagingxcaching.service.paging.PageImpl
import com.kmptoolkit.pagingxcaching.service.room.key.PagingQueryKey
import com.kmptoolkit.pagingxcaching.service.room.key.QueryKey


sealed class CacheFetcher<Key : QueryKey, Network : Any> {
    class LocalOnly<Key : QueryKey, Network : Any> : CacheFetcher<Key, Network>()

    class NetworkSingle<Key : QueryKey, Network : Any>(
        val fetchData: suspend (Key) -> ApiResult<Network>,
    ) : CacheFetcher<Key, Network>()

    class NetworkList<Key : QueryKey, Network : Any>(
        val fetchData: suspend (Key) -> ApiResult<Array<Network>>,
    ) : CacheFetcher<Key, Network>()

    class NetworkPage<Key : PagingQueryKey, Model : Any>(
        val fetchData: suspend (pagingKey: Key, page: Int) -> PageImpl<Model>,
    ) : CacheFetcher<Key, Model>()
}