package io.github.kotlinwizzard.kmptoolkit.image.processing

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.ImageBitmap
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

internal fun normalizeDegrees(deg: Int): Int {
    val n = ((deg % 360) + 360) % 360
    return when (n) {
        0, 90, 180, 270 -> n
        else -> n
    }
}
internal suspend fun decodeJpegToImageBitmap(jpegBytes: ByteArray): ImageBitmap {
    val blob = bytesToJpegBlob(jpegBytes)
    val promise = window.asDynamic()
        .createImageBitmap(blob)
        .unsafeCast<Promise<ImageBitmap>>()
    return promise.await()
}
internal  fun bytesToJpegBlob(bytes: ByteArray): Blob {
   return jpegBytesToBlob(bytes)
}
internal fun jpegBytesToBlob(bytes: ByteArray): Blob {
    val u8 = Uint8Array(bytes.size)
    val arr = u8.asDynamic()
    for (i in bytes.indices) {
        arr[i] = (bytes[i].toInt() and 0xFF)
    }
    return Blob(arrayOf(u8), BlobPropertyBag(type = "image/jpeg"))
}


internal suspend fun canvasToJpegBlob(canvas: HTMLCanvasElement, quality: Double=1.0): Blob =
    suspendCoroutine { cont ->
        canvas.toBlob({ b ->
            if (b != null) cont.resume(b) else cont.resumeWithException(IllegalStateException("toBlob returned null"))
        }, "image/jpeg", quality)
    }

internal suspend fun blobToByteArray(blob: Blob): ByteArray {
    return suspendCancellableCoroutine { cont ->
        val reader = FileReader()

        reader.onloadend = {
            val result = reader.result
            val buffer = result.unsafeCast<ArrayBuffer>()
            val u8 = Uint8Array(buffer)

            val out = ByteArray(u8.length)
            for (i in 0 until u8.length) out[i] = u8[i]

            cont.resume(out)
        }

        reader.onerror = {
            cont.resumeWithException(IllegalStateException("FileReader failed"))
        }

        reader.readAsArrayBuffer(blob)
    }
}