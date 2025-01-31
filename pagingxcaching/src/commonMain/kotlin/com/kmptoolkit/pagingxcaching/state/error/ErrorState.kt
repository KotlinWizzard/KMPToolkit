package com.kmptoolkit.pagingxcaching.state.error

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kmptoolkit.pagingxcaching.service.api.ApiResult
import com.kmptoolkit.pagingxcaching.service.api.ApiResultObserver

open class ErrorState(private val ignoredErrors: List<ApiResult.Error> = emptyList()) :
    ApiResultObserver {
    var isLoading by mutableStateOf(false)
        protected set
    var errorType by mutableStateOf<ApiResult.Error?>(null)
        protected set

    val isVisible by derivedStateOf {
        isLoading || errorType != null
    }

    override fun onApiCallStarted() {
        isLoading = true
    }

    override fun onApiResult(result: ApiResult<*>) {
        if (result is ApiResult.Error) {
            if (!ignoredErrors.contains(result)) {
                errorType = result
            }
        }
        when(result){
            is ApiResult.Error ->{
                if (!ignoredErrors.contains(result)) {
                    errorType = result
                }
                isLoading = false
            }
            is ApiResult.Success ->{
               onSuccess()
            }
        }
    }

    override fun onSuccess() {
        isLoading = false
        errorType = null
    }
}