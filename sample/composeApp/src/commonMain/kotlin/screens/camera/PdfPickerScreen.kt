package screens.camera

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.pdf.PdfPickerResult
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.pdf.PdfPickerResultData
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.pdf.PdfPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.pdf.rememberPdfPickerState
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitScaffold
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitTheme
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.spacing
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.decodeToImageVector
import presentation.BackButtonToolbar
import screens.SimpleTextButton


class PdfPickerScreen : Screen {
    @Composable
    override fun Content() {
        ToolkitScaffold(topBar = {
            BackButtonToolbar("Pdf")
        }) {
            val pdfPickerState = rememberPdfPickerState()
            pdfPickerState.RegisterLauncher()
            var currentResult by remember { mutableStateOf<PdfPickerResult.Data?>(null) }
            pdfPickerState.ListenPdfPickerResult { result ->
                when (result) {
                    PdfPickerResult.Cancelled -> Unit
                    is PdfPickerResult.Data -> currentResult = result
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
                    pdfPickerState.launch(
                        PdfPickerSelectionMode.Single
                    )
                }, text = "Pick single pdf")

                SimpleTextButton(onClick = {
                    pdfPickerState.launch(
                        PdfPickerSelectionMode.Multiple
                    )
                }, text = "Pick multiple pdf")

                Column(Modifier.fillMaxWidth().weight(1F)) {
                    Text("Current Previews:")
                    LazyRow(Modifier.weight(1F).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val items = currentResult?.results
                        if(items!=null) {
                            items(items){ data->
                                PdfPickerResultPreview(
                                    modifier = Modifier.size(60.dp),
                                    pdfPickerResultData = data
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
        Box(modifier) {
            Image(bitmap, contentDescription = null, modifier.matchParentSize())
            Text(
                "${pdfPickerResultData.pages}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomEnd).background(ToolkitTheme.colorScheme.background)
            )
        }
    }
}