package com.kmptoolkit.pagingxcaching.service.room.remotekey

data class RemoteKeyDetails(
    val currentPage: Int,
    val previousPage: Int?,
    val nextPage: Int?,
)