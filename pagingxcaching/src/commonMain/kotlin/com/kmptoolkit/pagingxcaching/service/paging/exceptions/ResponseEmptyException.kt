package com.kmptoolkit.pagingxcaching.service.paging.exceptions

open class ResponseEmptyException(
    additional: String? = null,
) : Exception("$MESSAGE ${additional ?: ""}") {
    companion object {
        private const val MESSAGE = "Paging List is empty"
    }
}