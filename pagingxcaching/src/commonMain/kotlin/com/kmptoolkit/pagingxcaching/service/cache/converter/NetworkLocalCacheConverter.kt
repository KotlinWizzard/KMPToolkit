package com.kmptoolkit.pagingxcaching.service.cache.converter

abstract class NetworkLocalCacheConverter<Network : Any, Local : Any> :
    CacheConverter<Network, Local, Network>() {
    override fun mapNetworkToOutput(data: Network): Network = data
}