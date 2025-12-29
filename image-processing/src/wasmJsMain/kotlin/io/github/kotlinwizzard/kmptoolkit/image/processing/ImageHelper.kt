package io.github.kotlinwizzard.kmptoolkit.image.processing

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.Uint8ClampedArray
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
@OptIn(ExperimentalWasmJsInterop::class)
internal suspend fun decodeJpegToImageBitmap(jpegBytes: ByteArray): ImageBitmap {
    val blob = bytesToJpegBlob(jpegBytes)
    val promise = window
        .createImageBitmap(blob)
        .unsafeCast<Promise<ImageBitmap>>()
    return promise.await<ImageBitmap>()
}
internal  fun bytesToJpegBlob(bytes: ByteArray): Blob {
   return jpegBytesToBlob(bytes)
}
@OptIn(ExperimentalWasmJsInterop::class)
internal fun jpegBytesToBlob(bytes: ByteArray): Blob {
    val u8 = Uint8Array(bytes.size)
    for (i in bytes.indices) {
        setU8a(u8, i, bytes[i].toInt() and 0xFF)
    }

    val parts: JsArray<JsAny?> = listOf(u8).toJsArray()
    return Blob(parts, BlobPropertyBag(type = "image/jpeg"))
}


@OptIn(ExperimentalWasmJsInterop::class)
internal suspend fun canvasToJpegBlob(canvas: HTMLCanvasElement, quality: Double=1.0): Blob =
    suspendCoroutine { cont ->
        canvas.toBlob({ b ->
            if (b != null) cont.resume(b) else cont.resumeWithException(IllegalStateException("toBlob returned null"))
        }, "image/jpeg", quality.toJsNumber())
    }

@OptIn(ExperimentalWasmJsInterop::class)
internal suspend fun blobToByteArray(blob: Blob): ByteArray {
    return suspendCancellableCoroutine { cont ->
        val reader = FileReader()
        reader.onloadend = {
            val result = reader.result
            val buffer = result?.unsafeCast<ArrayBuffer>() ?: throw IllegalStateException("FileReader failed")
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





@JsFun("(arr, i) => arr[i]")
external fun getU8(arr: Uint8ClampedArray, i: Int): Int

@JsFun("(arr, i, v) => { arr[i] = v; }")
external fun setU8(arr: Uint8ClampedArray, i: Int, v: Int)

@JsFun("(arr, i, v) => { arr[i] = v; }")
external fun setU8a(arr: Uint8Array, i: Int, v: Int)