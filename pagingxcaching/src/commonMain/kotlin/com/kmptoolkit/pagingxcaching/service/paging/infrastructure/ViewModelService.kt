package com.kmptoolkit.pagingxcaching.service.paging.infrastructure

import kotlinx.coroutines.CoroutineScope

interface ViewModelService{
    val viewModelServiceScope: CoroutineScope
}