package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class ImageTextAnalyzerState: ImageAnalyzerState() {
    var text:String? by mutableStateOf(null)
        private set
    private val textAnalyzer:ImageTextAnalyzer = ImageTextAnalyzer()
    private var isAnalyzing = false

    public override fun analyze(imageBytes: ByteArray) {
        if (isAnalyzing) return 

        isAnalyzing = true
        textAnalyzer.analyze(ImageInput.Bytes(imageBytes)){ textResult->
            text = textResult
            isAnalyzing = false
        }
    }
}