import androidx.compose.material.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.kmptoolkit.core.presentation.theme.LocalScreenSize
import com.kmptoolkit.core.presentation.theme.ToolkitTheme
import com.kmptoolkit.core.service.image.CacheServiceProvider
import com.kmptoolkit.navigation.AppNavigator
import screens.TemplateScreen


@Composable
fun App() {
    ToolkitTheme(
        lightColorScheme = { lightColorScheme() },
        darkColorScheme = { darkColorScheme() },
    ) {
        CacheServiceProvider {
            AppNavigator(TemplateScreen())
        }
    }
}
