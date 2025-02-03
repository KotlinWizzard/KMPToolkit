package com.kmptoolkit.pagingxcaching.service.api

interface ApiResultObserver {
    fun onApiCallStarted()
    fun onApiResult(result: ApiResult<*>)
    fun onSuccess()

    companion object {
        data object Empty : ApiResultObserver {
            override fun onApiCallStarted() {
            }

            override fun onApiResult(result: ApiResult<*>) {
            }

            override fun onSuccess() {
            }

        }
    }
}