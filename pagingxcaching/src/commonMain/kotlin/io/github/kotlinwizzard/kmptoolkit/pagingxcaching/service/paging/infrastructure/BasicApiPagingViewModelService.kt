package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.infrastructure

import androidx.compose.runtime.Composable
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.RemoteMediator
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.ApiComposePager
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.PagingSourceProvider
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.state.error.ErrorState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagingApi::class)
abstract class BasicApiPagingViewModelService<Local : Any, Actual : Any> : ViewModelServiceWithRefresh() {
    val flow = MutableStateFlow<PagingData<Actual>>(PagingData.empty())
    private val coroutineScope
        get() = viewModelServiceScope
    val errorState = ErrorState()

    protected abstract val pagingSourceProvider: PagingSourceProvider<Local, Actual>

    protected abstract val remoteMediator: RemoteMediator<Int, Local>?
    protected var pager: ApiComposePager<*, Actual>? = null

    val lazyPagingItems: LazyPagingItems<Actual>
        @Composable
        get() = flow.collectAsLazyPagingItems()

    abstract fun getKey(data: Actual): Any

    fun launchPaging() =
        coroutineScope.launch(Dispatchers.IO) {
            if (pager == null) {
                pager =
                    ApiComposePager(
                        sourceProvider = pagingSourceProvider,
                        viewModel = this@BasicApiPagingViewModelService,
                        remoteMediator = remoteMediator,
                    )
            }
        }
}