package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerSelectionMode.Companion.INFINITY


internal fun isSystemPickerAvailable(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        true
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2
    } else {
        false
    }
}


@SuppressLint("NewApi", "ClassVerificationFailure")
internal fun getMaxItems() =
    if (isSystemPickerAvailable()) {
        MediaStore.getPickImagesMaxLimit()
    } else {
        Integer.MAX_VALUE
    }


@Composable
internal fun pickSingleImages(
    selectionMode: MediaPickerSelectionMode.Single,
    mediaPickerMediaSelectionType: MediaPickerSelectionType,
    onResult: (List<Pair<ByteArray, MediaPickerMediaType>>) -> Unit,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
    val context = LocalContext.current
    val singleMediaPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                onResult(listOfNotNull(uri?.toResult(context)))
            },
        )
    LaunchMediaPicker(
        singleMediaPickerLauncher,
        mediaPickerMediaSelectionType,
        mediaPickerLauncherState
    )
}


@Composable
internal fun pickMultipleImages(
    selectionMode: MediaPickerSelectionMode.Multiple,
    mediaPickerMediaSelectionType: MediaPickerSelectionType,
    onResult: (List<Pair<ByteArray, MediaPickerMediaType>>) -> Unit,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
    val maxSelection =
        if (selectionMode.maxSelection == INFINITY) {
            getMaxItems()
        } else {
            selectionMode.maxSelection
        }
    val context = LocalContext.current
    val multipleMediaPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(maxSelection),
            onResult = { uriList ->
                onResult(uriList.mapNotNull { it.toResult(context = context) })
            },
        )

    LaunchMediaPicker(
        multipleMediaPickerLauncher,
        mediaPickerMediaSelectionType,
        mediaPickerLauncherState
    )
}

@Composable
internal fun LaunchMediaPicker(
    launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, *>,
    mediaPickerMediaSelectionType: MediaPickerSelectionType,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
    val status = mediaPickerLauncherState.status
    LaunchedEffect(status) {
        when (status) {
            MediaPickerLauncherStatus.LaunchRequested -> {
                launcher.launch(
                    PickVisualMediaRequest(mediaPickerMediaSelectionType.toActivityContracts())
                )
                mediaPickerLauncherState.launch()
            }

            else -> Unit
        }
    }
}

private fun Uri.toResult(context: Context): Pair<ByteArray, MediaPickerMediaType>? {
    val contentResolver = context.contentResolver
    return kotlin.runCatching {
        val inputStream = contentResolver.openInputStream(this)
            ?: return null
        inputStream.use {
            val byteArray = it.readBytes()
            val mimeType = contentResolver.getType(this) ?: "unknown/unknown"
            val mediaType = when {
                mimeType.startsWith("image/") -> MediaPickerMediaType.Image
                mimeType.startsWith("video/") -> MediaPickerMediaType.Video
                else -> return null
            }
            Pair(byteArray, mediaType)
        }
    }.getOrNull()
}


private fun MediaPickerSelectionType.toActivityContracts() = when (this) {
    MediaPickerSelectionType.Image -> ActivityResultContracts.PickVisualMedia.ImageOnly
    MediaPickerSelectionType.Video -> ActivityResultContracts.PickVisualMedia.VideoOnly
    MediaPickerSelectionType.ImageAndVideo -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
}