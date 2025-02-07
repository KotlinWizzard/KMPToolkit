package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.remotekey

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlin.jvm.JvmOverloads

@Entity(tableName = "remote_keys")
data class RemoteKey @JvmOverloads constructor(
    @PrimaryKey
    val remoteKeyData: String,
    val type: String,
    val queryHash: String,
    var currentPage: Int,
    var previousPage: Int?,
    val nextPage: Int?,
    val creationTime: Long = Clock.System.now().toEpochMilliseconds(),
)