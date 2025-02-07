package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.remotekey

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlin.jvm.JvmOverloads

@Entity
open class RemoteKey(
    @PrimaryKey
    open val remoteKeyData: String,
    open val type: String,
    open val queryHash: String,
    open var currentPage: Int,
    open var previousPage: Int?,
    open val nextPage: Int?,
    open val creationTime: Long = Clock.System.now().toEpochMilliseconds(),
)