package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import io.github.kotlinwizzard.kmptoolkit.core.extensions.currentOrThrow
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitScaffold
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitTheme
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.spacing
import io.github.kotlinwizzard.kmptoolkit.navigation.LocalAppNavigator
import screens.camera.CameraScreen
import screens.camera.GalleryScreen
import screens.camera.PdfPickerScreen


class TemplateScreen : Screen {
    @Composable
    override fun Content() {
        ToolkitScaffold {
            Column(
                Modifier.fillMaxSize().padding(ToolkitTheme.spacing.medium).verticalScroll(
                    rememberScrollState()
                ),
                verticalArrangement = Arrangement.spacedBy(
                    ToolkitTheme.spacing.medium
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TemplateItem(CameraScreen(), "Camera sample")
                TemplateItem(GalleryScreen(), "Gallery sample")
                TemplateItem(PdfPickerScreen(), "PDF-Picker sample")
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