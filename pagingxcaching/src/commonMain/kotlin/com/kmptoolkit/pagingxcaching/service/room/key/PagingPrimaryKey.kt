package com.kmptoolkit.pagingxcaching.service.room.key

sealed class PagingPrimaryKey {
    abstract fun asString():String
    data class LongKey(val value: Long) : PagingPrimaryKey(){
        override fun asString(): String = value.toString()
    }
    data class StringKey(val value: String) : PagingPrimaryKey(){
        override fun asString(): String = value

    }
    companion object{
        fun fromString(value: String): PagingPrimaryKey {
            value.toLongOrNull()?.let {
                return LongKey(it)
            }
            return StringKey(value)
        }
    }
}