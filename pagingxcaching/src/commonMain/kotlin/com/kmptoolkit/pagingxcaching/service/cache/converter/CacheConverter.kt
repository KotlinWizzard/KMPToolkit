package com.kmptoolkit.pagingxcaching.service.cache.converter

abstract class CacheConverter<Network : Any, Local : Any, Output : Any> {
    abstract fun mapNetworkToOutput(data: Network): Output

    abstract fun mapOutputToLocal(data: Output): Local

    abstract fun mapLocalToOutput(local: Local): Output
}