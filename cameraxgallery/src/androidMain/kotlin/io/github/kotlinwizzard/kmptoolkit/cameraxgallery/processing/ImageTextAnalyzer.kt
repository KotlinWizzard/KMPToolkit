package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors

actual class ImageTextAnalyzer {
    private val textRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    actual suspend fun analyze(imageInput: ImageInput): String? {
        val bytes = when (imageInput) {
            is ImageInput.Bytes -> imageInput.bytes
            is ImageInput.File -> imageInput.readBytes() ?: return null
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val textResult = textRecognizer.process(InputImage.fromBitmap(bitmap, 0)).await()
        return textResult.text
    }

    actual fun analyze(
        imageInput: ImageInput,
        callback: (String?) -> Unit
    ) {
        val bytes = when (imageInput) {
            is ImageInput.Bytes -> imageInput.bytes
            is ImageInput.File -> imageInput.readBytes() ?: return
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        textRecognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener {
                callback(it.text)
            }
            .addOnFailureListener { callback(null) }
            .addOnCanceledListener { callback(null) }

    }
}