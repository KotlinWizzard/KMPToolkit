package screens.test


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerMediaType
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerResult
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionType
import io.github.kotlinwizzard.kmptoolkit.gallery.rememberMediaPickerState
import io.github.kotlinwizzard.kmptoolkit.image.processing.ImageRotation
import io.github.kotlinwizzard.kmptoolkit.image.processing.ImageTextAnalyzerState
import io.github.kotlinwizzard.kmptoolkit.image.processing.rotateImage
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitScaffold
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitTheme
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.spacing
import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService
import presentation.BackButtonToolbar
import screens.SimpleTextButton


class TextScannerScreen : Screen {
    @Composable
    override fun Content() {
        ToolkitScaffold(topBar = {
            BackButtonToolbar("TextScanner")
        }) {
            val mediaPickerState = rememberMediaPickerState()
            mediaPickerState.RegisterLauncher()
            val textAnalyzer = remember { ImageTextAnalyzerState() }
            val imagePaths = remember { mutableStateOf<List<String>?>(null) }
            var imageBytes = remember { mutableStateOf<ByteArray?>(null) }
            mediaPickerState.ListenMediaPickerResult { result ->
                when (result) {
                    MediaPickerResult.Cancelled -> Unit
                    is MediaPickerResult.Data -> {
                        result.results.mapNotNull { media -> media.filePath.takeIf { media.mediaType == MediaPickerMediaType.Image } }.let {
                            imagePaths.value = it
                            it.firstOrNull()?.let { path->
                                imageBytes.value = MediaCacheService.readCachedFileOrNull(path)
                            }
                        }
                    }
                }
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


                imageBytes.value?.let {  bytes ->
                    SimpleTextButton(onClick = {
                        textAnalyzer.analyze(bytes)
                    }, text = "Analyze text")

                    SimpleTextButton(onClick = {
                        ImageRotation.rotateImage(bytes,ImageRotation.Degree90){
                            imageBytes.value = it
                        }
                    }, text = "Rotate 90°")

                    SimpleTextButton(onClick = {
                        ImageRotation.rotateImage(bytes,ImageRotation.DegreeNegative90){
                            imageBytes.value = it
                        }
                    }, text = "Rotate -90°")

                    Column(Modifier.fillMaxWidth().weight(1F),horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            imageBytes = bytes,
                            modifier = Modifier.size(200.dp)
                        )
                        Text("AnalyzedText:\n${textAnalyzer.text?:""}", modifier = Modifier.fillMaxWidth().weight(1F))
                    }
                }

            }
        }
    }
}

