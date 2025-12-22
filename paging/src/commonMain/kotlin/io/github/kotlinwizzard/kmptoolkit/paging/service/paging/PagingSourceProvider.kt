package io.github.kotlinwizzard.kmptoolkit.paging.service.paging

import androidx.paging.PagingSource

sealed class PagingSourceProvider<Local : Any, Actual : Any>(
    val source: () -> PagingSource<Int, Local>,
    val map: (Local) -> Actual,
) {
    class NetworkPagingSource<Actual : Any>(
        source: () -> ApiPagingSource<Actual>,
    ) : PagingSourceProvider<Actual, Actual>(source, map = { it })

    class LocalPagingSource<Local : Any, Actual : Any>(
        source: () -> PagingSource<Int, Local>,
        map: (Local) -> Actual,
    ) : PagingSourceProvider<Local, Actual>(source = source, map = map)
}