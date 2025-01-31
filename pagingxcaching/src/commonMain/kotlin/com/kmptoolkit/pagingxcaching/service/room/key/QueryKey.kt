package com.kmptoolkit.pagingxcaching.service.room.key

abstract class QueryKey {
    abstract fun getQueryIdentifier(): String
}