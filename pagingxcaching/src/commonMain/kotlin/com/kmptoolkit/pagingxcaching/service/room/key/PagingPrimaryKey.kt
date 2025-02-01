package com.kmptoolkit.pagingxcaching.service.room.key

sealed class PagingPrimaryKey {
    data class LongKey(val value: Long) : PagingPrimaryKey()
    data class StringKey(val value: String) : PagingPrimaryKey()
    companion object{
        fun fromString(value: String): PagingPrimaryKey {
            value.toLongOrNull()?.let {
                return LongKey(it)
            }
            return StringKey(value)
        }
    }
}