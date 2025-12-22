import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitTheme
import io.github.kotlinwizzard.kmptoolkit.core.service.media.CacheServiceProvider
import io.github.kotlinwizzard.kmptoolkit.navigation.AppNavigator
import screens.TemplateScreen


@Composable
fun App() {
    ToolkitTheme(
        lightColorScheme = { lightColorScheme() },
        darkColorScheme = { darkColorScheme() },
        darkTheme = false
    ) {
        CacheServiceProvider {
            AppNavigator(TemplateScreen())
        }
    }
}
