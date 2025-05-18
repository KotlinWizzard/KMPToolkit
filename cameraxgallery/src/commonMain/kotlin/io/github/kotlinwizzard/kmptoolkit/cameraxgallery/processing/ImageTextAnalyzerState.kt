package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ImageTextAnalyzerState: ImageAnalyzerState() {
    var text:String? by mutableStateOf(null)
        private set
    private val textAnalyzer:ImageTextAnalyzer = ImageTextAnalyzer()
    override fun analyze(imageBytes: ByteArray) {
        textAnalyzer.analyze(ImageInput.Bytes(imageBytes)){ textResult->
            text = textResult
        }
    }
}