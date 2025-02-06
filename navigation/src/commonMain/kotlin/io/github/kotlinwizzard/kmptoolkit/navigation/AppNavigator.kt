package io.github.kotlinwizzard.kmptoolkit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.OnBackPressed
import cafe.adriel.voyager.transitions.SlideTransition

val LocalAppNavigator: ProvidableCompositionLocal<Navigator?> = staticCompositionLocalOf { null }

@Composable
fun AppNavigator(
    screen: Screen,
    navigatorDisposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = null,
    content: @Composable (Navigator) -> Unit
) {
    Navigator(
        screen = screen,
        onBackPressed = onBackPressed,
        disposeBehavior = navigatorDisposeBehavior,
        content = { navigator ->
            CompositionLocalProvider(LocalAppNavigator provides navigator) {
                content(navigator)
            }
        },
    )
}


@Composable
fun AppNavigator(
    screen: Screen,
    navigatorDisposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = null,
) {
    AppNavigator(screen = screen,
        navigatorDisposeBehavior = navigatorDisposeBehavior,
        onBackPressed = onBackPressed,
        content = { navigator ->
            SlideTransition(navigator = navigator)
        })
}