package com.kmptoolkit.pagingxcaching.service.room.key


import kotlinx.serialization.Serializable

@Serializable
abstract class PagingQueryKey : QueryKey() {
    open fun getQueryHash() = hashCode().toString()

    override fun getQueryIdentifier(): String = getQueryHash()

    open fun getQueryType(): String = this::class.simpleName ?: ""
}
