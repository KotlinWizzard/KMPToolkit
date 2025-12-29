package io.github.kotlinwizzard.kmptoolkit.image.processing


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.TesseractException
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual class ImageTextAnalyzer actual constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val language: String = "eng"
    private val tesseract: Tesseract by lazy {
        val dataRoot = TessdataManager.ensureExtracted(javaClass, language)
        val t = Tesseract()
        t.setDatapath(dataRoot.toString())
        t.setLanguage(language)
        t
    }

    actual suspend fun analyze(imageInput: ImageInput): String? {
        val bytes = when (imageInput) {
            is ImageInput.Bytes -> imageInput.bytes
            is ImageInput.File -> imageInput.readBytes() ?: return null
        }

        val img = ImageIO.read(ByteArrayInputStream(bytes)) ?: return null

        return try {
            tesseract.doOCR(img)
        } catch (_: TesseractException) {
            null
        }
    }

    actual fun analyze(
        imageInput: ImageInput,
        callback: (String?) -> Unit
    ) {
        scope.launch {
            callback(analyze(imageInput))
        }
    }
}