package com.kmptoolkit.pagingxcaching.service.api

import com.kmptoolkit.core.util.io
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


suspend fun <R> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.io,
    observer: ApiResultObserver = ApiResultObserver.Companion.Empty,
    apiCall: suspend () -> R?,
) = withContext(dispatcher) {
    try {
        observer.onApiCallStarted()
        val result = apiCall.invoke()
        if (result != null) {
            if (isBodyEmpty(result)) {
                ApiResult.Empty
            } else {
                ApiResult.Success(result)
            }
        } else {
            ApiResult.Empty
        }
    } catch (throwable: Throwable) {
        when (throwable) {
            is kotlinx.io.IOException -> {
                ApiResult.NetworkError
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
                ApiResult.GenericError(
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
