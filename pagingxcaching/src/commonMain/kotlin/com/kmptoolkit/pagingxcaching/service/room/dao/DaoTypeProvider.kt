package com.kmptoolkit.pagingxcaching.service.room.dao

interface DaoTypeProvider {
    val daoType: DaoType
        get() = DaoType.AppData

    suspend fun clearAll()
}