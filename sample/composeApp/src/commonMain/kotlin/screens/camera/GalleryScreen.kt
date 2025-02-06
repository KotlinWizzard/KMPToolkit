package screens.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerSelectionType
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.rememberMediaPickerState
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitScaffold
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitTheme
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.spacing
import presentation.BackButtonToolbar
import screens.SimpleTextButton


class GalleryScreen : Screen {
    @Composable
    override fun Content() {
        ToolkitScaffold(topBar = {
            BackButtonToolbar("Gallery")
        }) {
            val mediaPickerState = rememberMediaPickerState()
            mediaPickerState.RegisterLauncher()
            mediaPickerState.ListenMediaPickerResult {
                println("TEST_MEDIA_PICKER_RESULT = $it")
            }
            Column(
                Modifier.fillMaxSize().padding(top = it.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    ToolkitTheme.spacing.medium
                )
            ) {
                SimpleTextButton(onClick = {
                    mediaPickerState.launch(
                        mediaPickerMediaSelectionType = MediaPickerSelectionType.Image,
                        mediaPickerSelectionMode = MediaPickerSelectionMode.Single
                    )
                }, text = "Pick single image")

                SimpleTextButton(onClick = {
                    mediaPickerState.launch(
                        mediaPickerMediaSelectionType = MediaPickerSelectionType.Video,
                        mediaPickerSelectionMode = MediaPickerSelectionMode.Single
                    )
                }, text = "Pick single video")

                SimpleTextButton(onClick = {
                    mediaPickerState.launch(
                        mediaPickerMediaSelectionType = MediaPickerSelectionType.Combined,
                        mediaPickerSelectionMode = MediaPickerSelectionMode.Single
                    )
                }, text = "Pick single image or video")

                SimpleTextButton(onClick = {
                    mediaPickerState.launch(
                        mediaPickerMediaSelectionType = MediaPickerSelectionType.Image,
                        mediaPickerSelectionMode = MediaPickerSelectionMode.Multiple()
                    )
                }, text = "Pick multiple images")

                SimpleTextButton(onClick = {
                    mediaPickerState.launch(
                        mediaPickerMediaSelectionType = MediaPickerSelectionType.Video,
                        mediaPickerSelectionMode = MediaPickerSelectionMode.Multiple()
                    )
                }, text = "Pick multiple videos")

                SimpleTextButton(onClick = {
                    mediaPickerState.launch(
                        mediaPickerMediaSelectionType = MediaPickerSelectionType.Combined,
                        mediaPickerSelectionMode = MediaPickerSelectionMode.Multiple()
                    )
                }, text = "Pick multiple images or videos")
            }
        }
    }
}