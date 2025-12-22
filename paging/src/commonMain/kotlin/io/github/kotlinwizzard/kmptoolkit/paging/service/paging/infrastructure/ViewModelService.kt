package io.github.kotlinwizzard.kmptoolkit.paging.service.paging.infrastructure

import kotlinx.coroutines.CoroutineScope

interface ViewModelService{
    val viewModelServiceScope: CoroutineScope
}