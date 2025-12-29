package io.github.kotlinwizzard.kmptoolkit.image.processing

expect class ImageTextAnalyzer() {
    suspend fun analyze(imageInput: ImageInput): String?
    fun analyze(imageInput: ImageInput, callback: (String?) -> Unit)
}