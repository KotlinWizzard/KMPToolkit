package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResult
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResultObserver
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.exceptions.ResponseEmptyException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText


@Composable
fun <T : Any> LazyPagingItems<T>.doOnRefresh(
    onLoading: () -> Unit = {},
    onLoaded: () -> Unit = {},
) {
    val loadState = loadState.refresh
    LaunchedEffect(
        loadState,
    ) {
        when (loadState) {
            is LoadState.Loading -> {
                onLoading.invoke()
            }

            is LoadState.NotLoading, is LoadState.Error -> {
                onLoaded.invoke()
            }
        }
    }
}

@Composable
fun <T : Any> LazyPagingItems<T>.handleObserver(observer: ApiResultObserver) {
    val isCached = loadState.mediator != null
    val isEmpty by rememberUpdatedState(itemCount <= 0)
    val isCachedAndNotEmpty = isCached && !isEmpty

    val mediator = loadState.mediator?.refresh != null
    val loadState = loadState.refresh
    LaunchedEffect(loadState, isEmpty) {
        when (loadState) {
            is LoadState.Loading -> observer.onApiCallStarted()

            is LoadState.NotLoading -> {
                if (isCached && isEmpty) {
                    observer.onApiResult(ApiResult.Empty)
                } else {
                    observer.onSuccess()
                }
            }

            is LoadState.Error -> {
                val error =
                    when (val throwable = loadState.error) {
                        is kotlinx.io.IOException -> {
                            ApiResult.NetworkError
                        }

                        is ResponseEmptyException -> {
                            ApiResult.Empty
                        }

                        is ClientRequestException -> {
                            ApiResult.HttpError(
                                throwable.response.status.value,
                                throwable.response.bodyAsText(),
                            )
                        }

                        is ServerResponseException -> {
                            ApiResult.HttpError(
                                throwable.response.status.value,
                                throwable.response.bodyAsText(),
                            )
                        }

                        is ResponseException -> {
                            ApiResult.HttpError(
                                throwable.response.status.value,
                                throwable.response.bodyAsText(),
                            )
                        }

                        else -> {
                            ApiResult.GenericError(throwable.message)
                        }
                    }
                if (isCachedAndNotEmpty) {
                    observer.onSuccess()
                } else {
                    observer.onApiResult(error)
                }
            }
        }
    }
}
