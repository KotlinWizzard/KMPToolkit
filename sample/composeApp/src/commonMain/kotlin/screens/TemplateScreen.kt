package screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.kmptoolkit.core.extensions.currentOrThrow
import com.kmptoolkit.core.presentation.theme.ToolkitScaffold
import com.kmptoolkit.navigation.LocalAppNavigator
import screens.camera.CameraScreen


class TemplateScreen : Screen {
    @Composable
    override fun Content() {
        ToolkitScaffold {
            Column(Modifier.fillMaxSize()) {
                TemplateItem(CameraScreen(), "Camera Test")
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