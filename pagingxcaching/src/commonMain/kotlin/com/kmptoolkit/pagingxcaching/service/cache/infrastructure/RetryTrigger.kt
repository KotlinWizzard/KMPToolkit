package com.kmptoolkit.pagingxcaching.service.cache.infrastructure

import kotlinx.coroutines.flow.MutableStateFlow

class RetryTrigger {
    enum class State { RETRYING, IDLE }

    val retryEvent = MutableStateFlow(State.IDLE)

    fun retry() {
        retryEvent.value = State.RETRYING
    }

    fun setIdle() {
        retryEvent.value = State.IDLE
    }
}
