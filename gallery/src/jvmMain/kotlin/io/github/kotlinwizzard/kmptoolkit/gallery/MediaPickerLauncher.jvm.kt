package io.github.kotlinwizzard.kmptoolkit.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

@androidx.compose.runtime.Composable
internal actual fun launchMediaPicker(
    onResult: (List<kotlin.Pair<ByteArray, io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerMediaType>>) -> Unit,
    mediaPickerStatus: io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerStatus.LaunchRequested,
    mediaPickerLauncherState: io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerLauncherState
) {
    LaunchMediaPicker(
        onResult,
        mediaPickerStatus.mediaPickerMediaSelectionType,
        mediaPickerStatus.mediaPickerSelectionMode,
        mediaPickerLauncherState
    )
}

@Composable
internal fun LaunchMediaPicker(
    onResult: (List<kotlin.Pair<ByteArray, io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerMediaType>>) -> Unit,
    mediaPickerMediaSelectionType: MediaPickerSelectionType,
    mediaPickerSelectionMode: MediaPickerSelectionMode,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
    val status = mediaPickerLauncherState.status
    LaunchedEffect(status) {
        when (status) {
            MediaPickerLauncherStatus.LaunchRequested -> {
                chooseFile(
                    onResult = onResult,
                    mediaPickerMediaSelectionType = mediaPickerMediaSelectionType,
                    mediaPickerSelectionMode = mediaPickerSelectionMode
                )
                mediaPickerLauncherState.launch()
            }

            else -> Unit
        }
    }
}

internal fun chooseFile(
    onResult: (List<kotlin.Pair<ByteArray, io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerMediaType>>) -> Unit,
    mediaPickerMediaSelectionType: MediaPickerSelectionType,
    mediaPickerSelectionMode: MediaPickerSelectionMode,
) {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    val chooser = JFileChooser().apply {
        isMultiSelectionEnabled = mediaPickerSelectionMode is MediaPickerSelectionMode.Multiple
        fileFilter = when (mediaPickerMediaSelectionType) {
            MediaPickerSelectionType.Image -> FileNameExtensionFilter(
                "Images",
                "jpg",
                "jpeg",
                "png",
                "webp"
            )

            MediaPickerSelectionType.Video -> FileNameExtensionFilter("Videos", "mp4", "mov", "mkv")
            MediaPickerSelectionType.ImageAndVideo ->
                FileNameExtensionFilter(
                    "Images",
                    "jpg",
                    "jpeg",
                    "png",
                    "webp",
                    "Videos", "mp4", "mov", "mkv"
                )
        }

    }

    val result = chooser.showOpenDialog(null)


    if (result == JFileChooser.APPROVE_OPTION) {
        val files: List<File> =
            if (chooser.isMultiSelectionEnabled) {
                chooser.selectedFiles.toList()
            } else {
                listOfNotNull(chooser.selectedFile)
            }

        val picked: List<Pair<ByteArray, MediaPickerMediaType>> =
            files.mapNotNull { file ->
                val bytes = file.readBytes()
                val type = file.toMediaTypeOrNull() ?: return@mapNotNull null
                bytes to type
            }

        // 2) Ergebnis nach außen geben
        onResult(picked)
    } else {
        // ggf. leere Liste oder gar nichts tun, je nach deinem State-Handling
        onResult(emptyList())
    }
}

private fun File.toMediaTypeOrNull(): MediaPickerMediaType? {
    val nameLower = name.lowercase()
    return when {
        nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") || nameLower.endsWith(".png") || nameLower.endsWith(
            ".webp"
        ) ->
            MediaPickerMediaType.Image

        nameLower.endsWith(".mp4") || nameLower.endsWith(".mov") || nameLower.endsWith(".mkv") ->
            MediaPickerMediaType.Video

        else -> null
    }
}