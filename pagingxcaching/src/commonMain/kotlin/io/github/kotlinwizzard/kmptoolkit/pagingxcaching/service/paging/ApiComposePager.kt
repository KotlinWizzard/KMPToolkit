package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.RemoteMediator
import androidx.paging.cachedIn
import androidx.paging.map
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.infrastructure.BasicApiPagingViewModelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/***
 * @param PageDTO is the paged object from the backend
 * @param DTO is the DTO used in PageDTO
 * @param Actual is the actual model, which will be mapped
 */
@OptIn(ExperimentalPagingApi::class)
class ApiComposePager<Local : Any, Actual : Any>(
    var sourceProvider: PagingSourceProvider<Local, Actual>,
    val viewModel: BasicApiPagingViewModelService<Local, Actual>,
    private val remoteMediator: RemoteMediator<Int, Local>? = null,
    private val pagingConfig: PagingConfig = DEFAULT_PAGING_CONFIG,
) {
    @OptIn(ExperimentalPagingApi::class)
    private val pager
        get() =
            Pager<Int, Local>(
                config = pagingConfig,
                remoteMediator = remoteMediator,
                pagingSourceFactory = { sourceProvider.source() },
            )

    init {
        val flow =
            pager
                .flow
                .cachedIn(viewModel.viewModelServiceScope)
                .map { it.map { local -> sourceProvider.map(local) } }
        viewModel.viewModelServiceScope.launch(Dispatchers.IO) {
            flow.collectLatest {
                viewModel.flow.value = it
            }
        }
    }

    companion object {
        private val DEFAULT_PAGING_CONFIG = PagingConfig(pageSize = 20, enablePlaceholders = true)
    }
}

