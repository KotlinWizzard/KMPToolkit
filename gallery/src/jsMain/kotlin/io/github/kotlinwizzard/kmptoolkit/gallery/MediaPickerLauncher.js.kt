package io.github.kotlinwizzard.kmptoolkit.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import io.github.kotlinwizzard.kmptoolkit.core.extensions.IO
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.Document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.ItemArrayLike
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
    val document = document
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(status) {
        when (status) {
            MediaPickerLauncherStatus.LaunchRequested -> {
                coroutineScope.launch(Dispatchers.IO) {
                    chooseFile(
                        document,
                        onResult = {
                            onResult(it.mapFiles())
                        },
                        multiple = mediaPickerSelectionMode is MediaPickerSelectionMode.Multiple,
                        extensions = mediaPickerMediaSelectionType.extensions()
                    )
                }
                mediaPickerLauncherState.launch()
            }

            else -> Unit
        }
    }
}



private fun MediaPickerSelectionType.extensions(): List<String> {
    return when (this) {
        MediaPickerSelectionType.Image -> listOf(
            "jpg",
            "jpeg",
            "png",
            "webp"
        )

        MediaPickerSelectionType.Video -> listOf("mp4", "mov", "mkv")
        MediaPickerSelectionType.ImageAndVideo -> listOf("mp4", "mov", "mkv") + listOf(
            "jpg",
            "jpeg",
            "png",
            "webp"
        )
    }
}


internal suspend fun List<File>.mapFiles(): List<kotlin.Pair<ByteArray, io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerMediaType>> {
    return mapNotNull { file ->
        val mediaType = file.toMediaTypeOrNull() ?: return@mapNotNull null
        readFileAsByteArray(file) to mediaType
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

suspend fun chooseFile(
    document: Document,
    onResult: suspend (List<File>) -> Unit,
    multiple: Boolean,
    extensions: List<String>,
) {
    val fixedExtensions = extensions.map { ".$it" }
    val file: List<File> = document.selectFilesFromDisk(fixedExtensions.joinToString(","), multiple)
    onResult(file)
}


private suspend fun Document.selectFilesFromDisk(
    accept: String,
    isMultiple: Boolean
): List<File> = suspendCoroutine { cont->
    val tempInput = (createElement("input") as HTMLInputElement).apply {
        type = "file"
        style.display = "none"
        this.accept = accept
        multiple = isMultiple
    }

    var finished = false

    fun finish(result: List<File>) {
        if (finished) return
        finished = true

        tempInput.onchange = null
        window.onfocus = null
        tempInput.remove()
        cont.resume(result)
    }

    tempInput.onchange = { evt ->
        val inputElement = evt.target as HTMLInputElement
        val files = inputElement.files?.asList().orEmpty()
        finish(files)
    }

    val onFocus: (Event) -> Unit = {
        window.setTimeout({
            if (!finished) {
                val files = tempInput.files?.asList().orEmpty()
                finish(files)
            }
            return@setTimeout null
        }, 1000)
    }
    window.addEventListener("focus", onFocus, true)


    body!!.append(tempInput)
    tempInput.click()
}

internal suspend fun readFileAsByteArray(file: File): ByteArray = suspendCoroutine {
    val reader = FileReader()
    reader.onload = { loadEvt ->
        val content = loadEvt.target.asDynamic().result as ArrayBuffer
        val array = Uint8Array(content)
        val fileByteArray = ByteArray(array.length)
        for (i in 0 until array.length) {
            fileByteArray[i] = array[i]
        }
        it.resumeWith(Result.success(fileByteArray))
    }
    reader.readAsArrayBuffer(file)
}