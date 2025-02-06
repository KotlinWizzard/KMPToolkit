package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.kotlinwizzard.kmptoolkit.core.extensions.clickableWithoutRipple
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitTheme
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResult
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.state.error.ErrorState

@Composable
fun ToolkitErrorOverlayLayout(
    modifier: Modifier = Modifier,
    errorState: ErrorState,
    errorDefaults: ToolkitErrorDefaults= LocalErrorDefaults.current,
    content: @Composable () -> Unit
) {
    ToolkitOverlayLayout(modifier, content = {
        content()
    }, overlay = {
        if (errorState.isVisible) {
            Box(
                Modifier.matchParentSize().clickableWithoutRipple(onClick = {})
                    .background(errorDefaults.backgroundColor())
            ) {
                val errorType = errorState.errorType
                when {
                    errorState.isLoading -> {
                        errorDefaults.loadingBar(this)
                    }

                    errorType != null -> {
                        errorDefaults.errorOverlay(this, errorType)
                    }
                }
            }
        }
    })
}


data class ToolkitErrorDefaults(
    val backgroundColor: @Composable () -> Color = { ToolkitErrorDefaults.backgroundColor },
    val loadingBar: @Composable BoxScope.() -> Unit = {
        ToolkitLoadingBar()
    },
    val errorOverlay: @Composable BoxScope.(errorType: ApiResult.Error) -> Unit
) {
    companion object {
        val backgroundColor: Color
            @Composable
            get() = ToolkitTheme.colorScheme.background

        val loadingBarColor: Color
            @Composable
            get() = ToolkitTheme.colorScheme.primary
    }
}

val LocalErrorDefaults = compositionLocalOf<ToolkitErrorDefaults>(defaultFactory = {
    ToolkitErrorDefaults(
        errorOverlay = {}
    )
})


