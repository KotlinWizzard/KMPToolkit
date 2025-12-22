package screens.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerMediaType
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerResult
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionType
import io.github.kotlinwizzard.kmptoolkit.gallery.rememberMediaPickerState
import io.github.kotlinwizzard.kmptoolkit.image.processing.ImageEditor
import io.github.kotlinwizzard.kmptoolkit.image.processing.ImageTextAnalyzerState
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitScaffold
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitTheme
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.spacing
import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService
import kotlinx.coroutines.launch
import presentation.BackButtonToolbar
import screens.SimpleTextButton


class ImageEditorScreen : Screen {
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
            var transformedImageBytes = remember { mutableStateOf<ByteArray?>(null) }
            mediaPickerState.ListenMediaPickerResult { result ->
                when (result) {
                    MediaPickerResult.Cancelled -> Unit
                    is MediaPickerResult.Data -> {
                        result.results.mapNotNull { media -> media.filePath.takeIf { media.mediaType == MediaPickerMediaType.Image } }
                            .let {
                                imagePaths.value = it
                                it.firstOrNull()?.let { path ->
                                    imageBytes.value = MediaCacheService.readCachedFileOrNull(path)
                                    transformedImageBytes.value = imageBytes.value
                                }
                            }
                    }
                }
            }
            val coroutineScope = rememberCoroutineScope()
            Column(
                Modifier.fillMaxSize().padding(top = it.calculateTopPadding()).verticalScroll(
                    rememberScrollState()
                ),
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


                transformedImageBytes.value?.let { bytes ->
                    AsyncImage(imageBytes = bytes, modifier = Modifier.size(80.dp))

                    SimpleTextButton(onClick = {
                        transformedImageBytes.value = imageBytes.value
                    }, text = "Reset")

                    SimpleTextButton(onClick = {
                        coroutineScope.launch {
                            transformedImageBytes.value = ImageEditor.applyGreyscale(bytes)
                        }
                    }, text = "Apply greyscale")

                    SimpleTextButton(onClick = {
                        coroutineScope.launch {
                            transformedImageBytes.value = ImageEditor.applySepia(bytes)
                        }
                    }, text = "Apply sepia")

                    SimpleInputButton(onButtonClick = { value ->
                        coroutineScope.launch {
                            transformedImageBytes.value = ImageEditor.applyBrightness(
                                bitmapData = bytes,
                                factor = value
                            )
                        }
                    }, buttonText = "Apply brigthness", default = 1F)

                    SimpleInputButton(onButtonClick = { value ->
                        coroutineScope.launch {
                            transformedImageBytes.value = ImageEditor.applyContrast(
                                bitmapData = bytes,
                                factor = value
                            )
                        }
                    }, buttonText = "Apply Contrast", default = 1F)

                    SimpleInputButton(onButtonClick = { value ->
                        coroutineScope.launch {
                            transformedImageBytes.value = ImageEditor.applySaturation(
                                bitmapData = bytes,
                                factor = value
                            )
                        }
                    }, buttonText = "Apply Saturation", default = 1F)

                    SimpleInputButton(onButtonClick = { value ->
                        coroutineScope.launch {
                            transformedImageBytes.value = ImageEditor.applyExposure(
                                bitmapData = bytes,
                                ev = value
                            )
                        }
                    }, buttonText = "Apply Exposure", default = 0F)

                    SimpleInputButton(onButtonClick = { value ->
                        coroutineScope.launch {
                            transformedImageBytes.value = ImageEditor.applyTemperature(
                                bitmapData = bytes,
                                temperature = value
                            )
                        }
                    }, buttonText = "Apply Temperature", default = 0F)

                    SimpleInputButton(onButtonClick = { value ->
                        coroutineScope.launch {
                            transformedImageBytes.value = ImageEditor.applyHue(
                                bitmapData = bytes,
                                angleDegrees = value
                            )
                        }
                    }, buttonText = "Apply Hue", default = 0F)
                }

            }
        }
    }

    @Composable
    fun SimpleInputButton(default: Float, buttonText: String, onButtonClick: (Float) -> Unit) {
        val textFieldState = rememberTextFieldState(initialText = default.toString())
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicTextField(
                state = textFieldState, textStyle = TextStyle.Default,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1F,false)
            )
            SimpleTextButton(text = buttonText, onClick = {
                textFieldState.text.toString().toFloatOrNull()?.let {
                    onButtonClick(it)
                }
            })
        }
    }
}



