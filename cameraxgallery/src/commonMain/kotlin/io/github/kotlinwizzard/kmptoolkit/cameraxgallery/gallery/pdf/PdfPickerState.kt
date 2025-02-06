package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerLauncherState
import io.github.kotlinwizzard.kmptoolkit.core.service.media.LocalCache

class PdfPickerState(private val earlyLaunch: Boolean = false) {
    private val mediaPickerLauncherState = MediaPickerLauncherState()
    private var registeredLauncherKey by mutableStateOf<String?>(null)
    var pdfPickerResult by mutableStateOf<PdfPickerResult?>(null)
        private set

    private var launcherCount by mutableIntStateOf(0)
    private var pdfPickerStatus: PdfPickerStatus by mutableStateOf(
        PdfPickerStatus.Idle
    )

    @Composable
    fun RegisterLauncher() {
        val count = remember { mutableStateOf<Int?>(null) }
        DisposableEffect(Unit) {
            count.value = launcherCount++
            onDispose {
            }
        }
        count.value?.let {
            RegisterLauncher(getLauncherKey(it))
        }
    }

    private fun getLauncherKey(count: Int) = "${DEFAULT_LAUNCHER_KEY}_$count"

    @Composable
    private fun RegisterLauncher(launcherKey: String) {
        val cache = LocalCache.current
        val status = pdfPickerStatus
        val key = registeredLauncherKey
        DisposableEffect(key) {
            if (registeredLauncherKey == null) {
                registeredLauncherKey = launcherKey
            }
            onDispose {
                if (key == launcherKey) {
                    registeredLauncherKey = null
                }
            }
        }
        if (key != null && key != launcherKey) return
        if (status is PdfPickerStatus.LaunchRequested) {
            LaunchPdfPicker(
                onResult = ::onResult,
                pdfPickerStatus = status,
                mediaPickerLauncherState = mediaPickerLauncherState
            )
        }
    }

    private fun onResult(data: List<PdfPickerResultData>) {
        pdfPickerResult = when {
            data.isEmpty() -> PdfPickerResult.Cancelled
            else -> PdfPickerResult.Data(
                data
            )
        }
        reset()
    }

    @Composable
    fun ListenPdfPickerResult(onResult: (PdfPickerResult) -> Unit) {
        RegisterLauncher()
        val result = pdfPickerResult
        LaunchedEffect(
            result
        ) {
            if (result != null) {
                onResult(result)
            }
        }
    }

    fun launch(selectionMode: PdfPickerSelectionMode = PdfPickerSelectionMode.Single) {
        if (!earlyLaunch && registeredLauncherKey == null) return
        if (pdfPickerStatus is PdfPickerStatus.LaunchRequested) return
        if (pdfPickerResult != null) {
            pdfPickerResult = null
        }
        pdfPickerStatus = PdfPickerStatus.LaunchRequested(pdfPickerSelectionMode = selectionMode)
        mediaPickerLauncherState.requestLaunch()
    }

    private fun reset() {
        pdfPickerStatus = PdfPickerStatus.Idle
        mediaPickerLauncherState.reset()
    }


    companion object {
        private const val DEFAULT_LAUNCHER_KEY = "PDF_PICKER_LAUNCHER"
    }
}

@Composable
fun rememberPdfPickerState(earlyLaunch: Boolean = false) = remember {
    PdfPickerState(
        earlyLaunch = earlyLaunch

    )
}.apply {
    RegisterLauncher()
}