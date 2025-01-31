package com.kmptoolkit.pagingxcaching.service.room.remotekey

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock

@Entity
data class RemoteKey(
    @PrimaryKey
    val remoteKeyData: String,
    val type: String,
    val queryHash: String,
    var currentPage: Int,
    var previousPage: Int?,
    val nextPage: Int?,
    val creationTime: Long = Clock.System.now().toEpochMilliseconds(),
)