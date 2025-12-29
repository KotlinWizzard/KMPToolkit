package io.github.kotlinwizzard.kmptoolkit.image.processing

import io.github.kotlinwizzard.kmptoolkit.core.extensions.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.w3c.files.Blob

actual class ImageTextAnalyzer actual constructor() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()
    private var worker: TesseractWorker? = null
    @OptIn(ExperimentalWasmJsInterop::class)
    private suspend fun getWorker(): TesseractWorker =
        mutex.withLock {
            worker ?: run {
                val w = createWorker("eng", 1).await<TesseractWorker>()
                worker = w
                w
            }
        }

    @OptIn(ExperimentalWasmJsInterop::class)
    actual suspend fun analyze(imageInput: ImageInput): String? {
        val bytes = when (imageInput) {
            is ImageInput.Bytes -> imageInput.bytes
            is ImageInput.File -> imageInput.readBytes() ?: return null
        }

        val blob: Blob = jpegBytesToBlob(bytes)

        return try {
            val w = getWorker()
            val res = w.recognize(blob).await<TesseractRecognizeResult>()
            res.data.text
        } catch (_: Throwable) {
            null
        }
    }

    actual fun analyze(imageInput: ImageInput, callback: (String?) -> Unit) {
        scope.launch {
            callback(analyze(imageInput))
        }
    }
}