package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api

sealed class ApiResult<out T> {
    data class Success<out T>(
        val value: T,
    ) : ApiResult<T>()

    sealed class Error : ApiResult<Nothing>()

    data class GenericError(
        val error: String? = null,
    ) : Error() {
        override fun toString(): String = "GenericError: error=$error"
    }

    data class HttpError(
        val code: Int? = null,
        val error: String? = null,
    ) : Error() {
        override fun toString(): String = "GenericError: Code = $code ; error=$error"
    }

    data object Empty : Error()

    data object NetworkError : Error()

    data object NoInternetError : Error()

    fun isSuccess(): Boolean = this is Success || this is Empty

    val successResultOrNull get() = (this as? Success)?.value
}