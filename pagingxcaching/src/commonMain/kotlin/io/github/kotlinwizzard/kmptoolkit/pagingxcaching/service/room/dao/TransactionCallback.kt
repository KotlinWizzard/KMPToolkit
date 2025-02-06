package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.dao

interface TransactionCallback {
    suspend fun transaction()
}