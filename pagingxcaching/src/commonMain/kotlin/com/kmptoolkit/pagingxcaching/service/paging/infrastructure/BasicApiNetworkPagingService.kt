package com.kmptoolkit.pagingxcaching.service.paging.infrastructure

import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator
import com.kmptoolkit.pagingxcaching.service.paging.ApiPagingSource
import com.kmptoolkit.pagingxcaching.service.paging.PagingSourceProvider

abstract class BasicApiNetworkPagingService<Actual : Any> :
    BasicApiPagingService<Actual, Actual>() {
    protected abstract fun getPagingSource(): ApiPagingSource<Actual>

    override val pagingSourceProvider: PagingSourceProvider<Actual, Actual>
        get() = PagingSourceProvider.NetworkPagingSource(source = { getPagingSource() })

    @OptIn(ExperimentalPagingApi::class)
    override val remoteMediator: RemoteMediator<Int, Actual>?
        get() = null
}