package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import platform.CoreImage.CIImage
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.UIKit.UIImage
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import kotlin.coroutines.suspendCoroutine

actual class ImageTextAnalyzer {

    actual suspend fun analyze(imageInput: ImageInput): String? {
        val bytes = when (imageInput) {
            is ImageInput.Bytes -> imageInput.bytes
            is ImageInput.File -> imageInput.readBytes() ?: return null
        }
        return suspendCoroutine { continuation ->
            recognizeText(bytes) { text ->
                continuation.resumeWith(Result.success(text))
            }
        }
    }

    actual fun analyze(
        imageInput: ImageInput,
        callback: (String?) -> Unit
    ) {
        val bytes = when (imageInput) {
            is ImageInput.Bytes -> imageInput.bytes
            is ImageInput.File -> imageInput.readBytes() ?: return
        }
        recognizeText(bytes, callback)
    }

    @OptIn(ExperimentalForeignApi::class)
    fun recognizeText(bytes:ByteArray, onTextGenerated: (text: String?) -> Unit) {
        val image = bytes.toUiImage() ?: return
        println("****capture recognise with bytes!!")
        val cgImage = image.CGImage

        val handler = VNImageRequestHandler(cgImage, options = emptyMap<Any?, Any?>())

        val request = VNRecognizeTextRequest { request, error ->
            if (error != null) {
                return@VNRecognizeTextRequest
            }

            val observations = request?.results as? List<VNRecognizedTextObservation> ?: emptyList()

            val recognizedText = observations.joinToString("\n") { observation ->
                val topCandidate: VNRecognizedText =
                    observation.topCandidates(1u).firstOrNull() as VNRecognizedText
                topCandidate.string
            }

            val queue = dispatch_queue_create("background", null)

            dispatch_async(queue) {
                onTextGenerated(recognizedText)
            }
        }.apply { recognitionLevel = VNRequestTextRecognitionLevelAccurate }

        try {
            handler.performRequests(listOf(request), error = null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}


