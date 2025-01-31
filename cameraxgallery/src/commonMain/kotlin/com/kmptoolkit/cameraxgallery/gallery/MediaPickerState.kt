package com.kmptoolkit.cameraxgallery.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kmptoolkit.cameraxgallery.camera.state.ImageCompressionMode
import com.kmptoolkit.cameraxgallery.gallery.MediaPickerStatus.LaunchRequested
import com.kmptoolkit.core.service.image.MediaCache
import com.kmptoolkit.core.service.image.LocalCache

class MediaPickerState(
    private val imageCompressionMode: ImageCompressionMode = ImageCompressionMode.None,
    private val earlyLaunch: Boolean = false
) {
    private var mediaPickerStatus: MediaPickerStatus by mutableStateOf(MediaPickerStatus.Idle)


    private val mediaPickerLauncherState = MediaPickerLauncherState()

    var mediaPickerResult by mutableStateOf<MediaPickerResult?>(null)
        private set
    private var registeredLauncherKey by mutableStateOf<String?>(null)

    private var launcherCount by mutableIntStateOf(0)

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
        val status = mediaPickerStatus
        val key = registeredLauncherKey
        DisposableEffect(key) {
            onDispose {
                if (key == launcherKey) {
                    registeredLauncherKey = null
                }
            }
        }
        if (key != null && key != launcherKey) return
        if (status is LaunchRequested) {
            registeredLauncherKey = launcherKey
            launchMediaPicker(
                onResult = {
                    onResult(it, cache)
                },
                mediaPickerStatus = status,
                mediaPickerLauncherState = mediaPickerLauncherState
            )
        }
    }

    @Composable
    fun ListenMediaPickerResult(onResult: (MediaPickerResult) -> Unit) {
        RegisterLauncher()
        val result = mediaPickerResult
        LaunchedEffect(
            result
        ) {
            if (result != null) {
                onResult(result)
            }
        }
    }

    internal fun onResult(
        result: List<Pair<ByteArray, MediaPickerMediaType>>,
        cache: MediaCache
    ) {
        val resultData = result.map {
            when (it.second) {
                MediaPickerMediaType.Image -> MediaPickerResultData(
                    file = cache.imageCache.cacheFileTemporary(
                        imageCompressionMode.compress(it.first)
                    ), mediaType = it.second
                )

                MediaPickerMediaType.Video -> {
                    MediaPickerResultData(
                        file = cache.videoCache.cacheFileTemporary(
                            it.first
                        ), mediaType = it.second
                    )
                }
            }
        }
        mediaPickerResult = when (resultData.isEmpty()) {
            true -> MediaPickerResult.Cancelled
            false -> MediaPickerResult.Data(resultData)
        }
        reset()
    }

    fun launch(
        mediaPickerMediaSelectionType: MediaPickerSelectionType = MediaPickerSelectionType.Image,
        maxSelection: Int? = null
    ) {
        val mode = when (maxSelection?.coerceAtLeast(1)) {
            null -> MediaPickerSelectionMode.Multiple()
            1 -> MediaPickerSelectionMode.Single
            else -> MediaPickerSelectionMode.Multiple(maxSelection)
        }
        launch(
            mediaPickerMediaSelectionType = mediaPickerMediaSelectionType,
            mediaPickerSelectionMode = mode
        )
    }


    fun launch(
        mediaPickerMediaSelectionType: MediaPickerSelectionType = MediaPickerSelectionType.Image,
        mediaPickerSelectionMode: MediaPickerSelectionMode = MediaPickerSelectionMode.Single,
    ) {
        if (!earlyLaunch && registeredLauncherKey == null) return
        if (mediaPickerStatus is LaunchRequested) return
        if (mediaPickerResult != null) {
            mediaPickerResult = null
        }
        mediaPickerStatus = LaunchRequested(mediaPickerMediaSelectionType, mediaPickerSelectionMode)
        mediaPickerLauncherState.requestLaunch()
    }

    private fun reset() {
        mediaPickerStatus = MediaPickerStatus.Idle
        mediaPickerLauncherState.reset()
    }

    companion object {
        private const val DEFAULT_LAUNCHER_KEY = "MEDIA_PICKER_LAUNCHER"
    }
}

@Composable
fun rememberMediaPickerState(
    imageCompressionMode: ImageCompressionMode = ImageCompressionMode.None,
    earlyLaunch: Boolean = false
) =
    remember {
        MediaPickerState(imageCompressionMode = imageCompressionMode, earlyLaunch = earlyLaunch)
    }


