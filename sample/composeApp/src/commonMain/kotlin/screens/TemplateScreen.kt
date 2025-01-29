package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.kmptoolkit.core.extensions.currentOrThrow
import com.kmptoolkit.core.presentation.theme.ToolkitScaffold
import com.kmptoolkit.core.presentation.theme.ToolkitTheme
import com.kmptoolkit.core.presentation.theme.spacing
import com.kmptoolkit.navigation.LocalAppNavigator
import screens.camera.CameraScreen
import screens.camera.GalleryScreen


class TemplateScreen : Screen {
    @Composable
    override fun Content() {
        ToolkitScaffold {
            Column(
                Modifier.fillMaxSize().padding(ToolkitTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(
                    ToolkitTheme.spacing.medium
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TemplateItem(CameraScreen(), "Camera sample")
                TemplateItem(GalleryScreen(), "Gallery sample")
            }
        }
    }
}

@Composable
private fun TemplateItem(screen: Screen, text: String) {
    val appNavigator = LocalAppNavigator.currentOrThrow
    Button(onClick = {
        appNavigator.push(screen)
    }) {
        Text(text)
    }
}

@Composable
internal fun SimpleTextButton(onClick: () -> Unit, text: String) {
    Button(onClick = onClick) {
        Text(text)
    }
}