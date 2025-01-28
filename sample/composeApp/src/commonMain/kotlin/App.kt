import androidx.compose.material.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.kmptoolkit.core.presentation.theme.LocalScreenSize
import com.kmptoolkit.core.presentation.theme.ToolkitTheme


@Composable
fun App() {
    ToolkitTheme(
        lightColorScheme = { lightColorScheme() },
        darkColorScheme = { darkColorScheme() },
    ) {
        val screenSize = LocalScreenSize.current
        Text("ScreenSize: $screenSize")
    }
}
