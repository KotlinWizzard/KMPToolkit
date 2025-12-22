package io.github.kotlinwizzard.kmptoolkit.image.processing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.kotlinwizzard.kmptoolkit.image.core.image.processing.ImageAnalyzerState

class ImageTextAnalyzerState: ImageAnalyzerState() {
    var text:String? by mutableStateOf(null)
        private set
    private val textAnalyzer:ImageTextAnalyzer = ImageTextAnalyzer()
    private var isAnalyzing = false

    override fun analyze(imageBytes: ByteArray) {
        if (isAnalyzing) return 

        isAnalyzing = true
        textAnalyzer.analyze(ImageInput.Bytes(imageBytes)){ textResult->
            text = textResult
            isAnalyzing = false
        }
    }
}