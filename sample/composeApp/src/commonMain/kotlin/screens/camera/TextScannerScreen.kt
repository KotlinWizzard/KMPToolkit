package screens.camera

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.github.panpf.sketch.SketchImage
import com.github.panpf.sketch.decode.SvgDecoder
import com.github.panpf.sketch.painter.asEquitable
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.sketch.request.LoadState
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.state.PainterStateImage
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerMediaType
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerResult
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerSelectionType
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.rememberMediaPickerState
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing.ImageInput
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing.ImageTextAnalyzer
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing.ImageTextAnalyzerState
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitScaffold
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitTheme
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.spacing
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
            mediaPickerState.ListenMediaPickerResult { result ->
                when (result) {
                    MediaPickerResult.Cancelled -> Unit
                    is MediaPickerResult.Data -> {
                            result.results.mapNotNull { media -> media.filePath.takeIf { media.mediaType == MediaPickerMediaType.Image } }.let {
                                imagePaths.value = it
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


                imagePaths.value?.firstOrNull()?.let {  path ->
                    SimpleTextButton(onClick = {
                        val bytes = ImageInput.File(path).readBytes() ?: return@SimpleTextButton
                        textAnalyzer.analyze(bytes)
                    }, text = "Analyze text")

                    Column(Modifier.fillMaxWidth().weight(1F),horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(url = path, modifier = Modifier.size(80.dp))
                        Text("AnalyzedText:\n${textAnalyzer.text?:""}", modifier = Modifier.fillMaxWidth().weight(1F))
                    }
                }

            }
        }
    }
}



