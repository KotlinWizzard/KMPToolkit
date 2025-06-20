package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

abstract class ImageAnalyzerState {

    internal abstract fun analyze(imageBytes: ByteArray)
}