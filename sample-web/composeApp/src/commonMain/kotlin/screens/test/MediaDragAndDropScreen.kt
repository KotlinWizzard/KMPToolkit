package screens.test

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.painter.asPainter
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ImageOptions
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.PdfPickerResult
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.PdfPickerResultData
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.PdfPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.rememberPdfPickerState
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitScaffold
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitTheme
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.spacing
import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerMediaType
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerResult
import io.github.kotlinwizzard.kmptoolkit.gallery.rememberMediaPickerState
import io.github.kotlinwizzard.kmptoolkit.gallery.ui.MediaImageOrPdfDragAndDropContainer
import io.github.kotlinwizzard.kmptoolkit.gallery.ui.MediaImageOrPdfDragAndDropLayout
import io.github.kotlinwizzard.kmptoolkit.gallery.ui.PdfDragAndDropContainer
import io.github.kotlinwizzard.kmptoolkit.gallery.ui.PdfDragAndDropLayout
import io.github.kotlinwizzard.kmptoolkit.image.core.sketch.supportLocalCache
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import presentation.BackButtonToolbar
import screens.SimpleTextButton


class MediaDragAndDropScreen : Screen {
    @Composable
    override fun Content() {
        ToolkitScaffold(topBar = {
            BackButtonToolbar("Pdf")
        }) {
            val pdfPickerState = rememberPdfPickerState()
            val mediaPickerState = rememberMediaPickerState()
            pdfPickerState.RegisterLauncher()
            mediaPickerState.RegisterLauncher()
            var currentPdfResult by remember { mutableStateOf<PdfPickerResult.Data?>(null) }
            pdfPickerState.ListenPdfPickerResult { result ->
                when (result) {
                    PdfPickerResult.Cancelled -> Unit
                    is PdfPickerResult.Data -> currentPdfResult = result
                }
            }
            val imagePaths = remember { mutableStateOf<List<String>?>(null) }
            mediaPickerState.ListenMediaPickerResult { result ->
                println("TEST_RESULT result=$result")
                when (result) {
                    MediaPickerResult.Cancelled -> Unit
                    is MediaPickerResult.Data -> {
                        result.results.mapNotNull { media -> media.filePath.takeIf { media.mediaType == MediaPickerMediaType.Image } }
                            .let { paths ->
                                imagePaths.value = paths
                            }
                    }
                }
            }
            Column(
                Modifier.fillMaxSize().padding(top = it.calculateTopPadding()).verticalScroll(
                    rememberScrollState()
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    ToolkitTheme.spacing.medium
                )
            ) {
               MediaImageOrPdfDragAndDropLayout(
                    modifier = Modifier.fillMaxWidth(0.8F).height(100.dp)
                        .background(Color.LightGray),
                    pdfPickerState = pdfPickerState,
                   mediaPickerState = mediaPickerState,
                    pickFilesOnClick = true,
                ) {
                    Text("Drag files here")
                }

                Column(Modifier.fillMaxWidth().height(500.dp)) {
                    Text("Current Previews:")
                    LazyColumn(
                        Modifier.weight(1F).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val items = currentPdfResult?.results
                        if (items != null) {
                            items(items) { data ->
                                PdfPickerResultPreview(
                                    modifier = Modifier.size(160.dp),
                                    pdfPickerResultData = data
                                )
                            }
                        }
                        val imageItems = imagePaths.value
                        if (imageItems != null) {
                            items(imageItems) { path ->
                                AsyncImage(
                                    modifier = Modifier.size(160.dp),
                                    uri = path,
                                    contentDescription = null,
                                    state = rememberAsyncImageState(options = ImageOptions({
                                        addComponents {
                                            supportLocalCache()
                                        }
                                    })),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun PdfPickerResultPreview(
        modifier: Modifier,
        pdfPickerResultData: PdfPickerResultData
    ) {
        val bitmap = pdfPickerResultData.readPreviewImageBytes()?.decodeToImageBitmap() ?: return
        Column(Modifier.wrapContentSize()) {
            Box(modifier) {
                Image(bitmap, contentDescription = null, modifier.matchParentSize())
                Text(
                    "${pdfPickerResultData.pages}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .background(ToolkitTheme.colorScheme.background)
                )
            }
            Text(pdfPickerResultData.filename)
        }
    }
}
