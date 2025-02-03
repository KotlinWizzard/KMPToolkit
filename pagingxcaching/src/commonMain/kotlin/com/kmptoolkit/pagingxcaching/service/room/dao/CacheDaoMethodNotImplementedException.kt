package com.kmptoolkit.pagingxcaching.service.room.dao

class CacheDaoMethodNotImplementedException(
    method: String,
) : Exception(
    "Method not implemented: Method name = $method",
)