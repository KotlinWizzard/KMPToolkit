package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api

import io.github.kotlinwizzard.kmptoolkit.core.util.io
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


suspend fun <R> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.io,
    observer: io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResultObserver = io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResultObserver.Companion.Empty,
    apiCall: suspend () -> R?,
) = withContext(dispatcher) {
    try {
        observer.onApiCallStarted()
        val result = apiCall.invoke()
        if (result != null) {
            if (io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.isBodyEmpty(result)) {
                io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResult.Empty
            } else {
                io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResult.Success(
                    result
                )
            }
        } else {
            io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResult.Empty
        }
    } catch (throwable: Throwable) {
        when (throwable) {
            is kotlinx.io.IOException -> {
                io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResult.NetworkError
            }

            is ClientRequestException -> {
                io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResult.HttpError(
                    throwable.response.status.value,
                    throwable.response.bodyAsText(),
                )
            }

            is ServerResponseException -> {
                io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResult.HttpError(
                    throwable.response.status.value,
                    throwable.response.bodyAsText(),
                )
            }

            is ResponseException -> {
                io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResult.HttpError(
                    throwable.response.status.value,
                    throwable.response.bodyAsText(),
                )
            }

            else -> {
                io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResult.GenericError(
                    "Unknown error",
                )
            }
        }
    }
}.apply {
    observer.onApiResult(this)
}

private fun <T> isBodyEmpty(body: T): Boolean {
    return when {
        body is Unit -> false
        body is Array<*> && body.isEmpty() -> true
        body is List<*> && body.isEmpty() -> true
        body is Collection<*> && body.isEmpty() -> true
        else -> false
    }
}
