package com.kmptoolkit.pagingxcaching.service.room.key

import kotlinx.serialization.Serializable

@Serializable
abstract class QueryKey {
    abstract fun getQueryIdentifier(): String
}