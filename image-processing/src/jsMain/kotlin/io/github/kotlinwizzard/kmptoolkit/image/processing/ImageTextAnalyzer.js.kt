package io.github.kotlinwizzard.kmptoolkit.image.processing

actual class ImageTextAnalyzer actual constructor() {
    actual suspend fun analyze(imageInput: ImageInput): String? {
        TODO("Not yet implemented")
    }

    actual fun analyze(
        imageInput: ImageInput,
        callback: (String?) -> Unit
    ) {
    }
}