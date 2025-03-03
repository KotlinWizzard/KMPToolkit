package screens.camera

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

                SimpleTextButton(onClick = {
                    mediaPickerState.launch(
                        mediaPickerMediaSelectionType = MediaPickerSelectionType.Video,
                        mediaPickerSelectionMode = MediaPickerSelectionMode.Single
                    )
                }, text = "Pick single video")

                SimpleTextButton(onClick = {
                    mediaPickerState.launch(
                        mediaPickerMediaSelectionType = MediaPickerSelectionType.ImageAndVideo,
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
                        mediaPickerMediaSelectionType = MediaPickerSelectionType.ImageAndVideo,
                        mediaPickerSelectionMode = MediaPickerSelectionMode.Multiple()
                    )
                }, text = "Pick multiple images or videos")

                LazyColumn(Modifier.weight(1F).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(imagePaths.value?: emptyList()) {
                        AsyncImage(url = it, modifier = Modifier.size(80.dp))
                    }
                }
            }
        }
    }
}



@Composable
fun AsyncImage(
    modifier: Modifier=Modifier,
   url:String,
    contentDescription: String = "Image from url",
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    colorFilter: ColorFilter? = null,
    defaultImageColorFilter: ColorFilter? = null,
    alpha: Float = 1F,
    animateImageChange: Boolean = true,
) {
    val asyncState = rememberAsyncImageState()
    val asyncPainter =
        rememberAsyncImagePainter(
            state = asyncState,
            request =
            ComposableImageRequest(url) {
                crossfade(20)
                crossfade(enable = animateImageChange)
                resizeOnDraw(false)
                if (!url.contains(".svg")) {
                    precision(Precision.EXACTLY)
                }
                components {
                    addDecoder(SvgDecoder.Factory())
                }
            },
        )

    val currentColorFilter by rememberUpdatedState(
        when (asyncState.loadState) {
            is LoadState.Canceled, is LoadState.Error, is LoadState.Started, null -> defaultImageColorFilter
            is LoadState.Success -> colorFilter
        },
    )
    Image(
        painter = asyncPainter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
        alpha = alpha,
        colorFilter = currentColorFilter,
    )
}