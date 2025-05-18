package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

expect class ImageTextAnalyzer() {
    suspend fun analyze(imageInput: ImageInput): String?
    fun analyze(imageInput: ImageInput, callback: (String?) -> Unit)
}