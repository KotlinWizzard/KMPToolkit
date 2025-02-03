package com.kmptoolkit.pagingxcaching.service.room.dao

interface TransactionCallback {
    suspend fun transaction()
}