package io.github.kotlinwizzard.kmptoolkit.image.core.image.processing

abstract class ImageAnalyzerState {

    abstract fun analyze(imageBytes: ByteArray)
}