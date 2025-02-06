package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.infrastructure

import kotlinx.coroutines.CoroutineScope

interface ViewModelService{
    val viewModelServiceScope: CoroutineScope
}