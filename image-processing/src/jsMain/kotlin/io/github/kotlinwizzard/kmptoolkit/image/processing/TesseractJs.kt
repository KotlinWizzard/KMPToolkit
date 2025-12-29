@file:JsModule("tesseract.js")
@file:JsNonModule

package io.github.kotlinwizzard.kmptoolkit.image.processing

import kotlin.js.Promise

external fun createWorker(lang: String = definedExternally,
                          oem: Int = definedExternally,
                          options: WorkerOptions = definedExternally): Promise<TesseractWorker>

external interface WorkerOptions {
    var logger: ((Any?) -> Unit)?
    var workerPath: String?
    var langPath: String?
    var corePath: String?
}

external interface TesseractWorker {
    fun recognize(image: Any?): Promise<TesseractRecognizeResult>
    fun terminate(): Promise<Unit>
    fun reinitialize(lang: String, oem: Int = definedExternally): Promise<Unit>
}

external interface TesseractRecognizeResult {
    val data: TesseractData
}

external interface TesseractData {
    val text: String
}
