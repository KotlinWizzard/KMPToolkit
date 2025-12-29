@file:JsModule("tesseract.js")


package io.github.kotlinwizzard.kmptoolkit.image.processing

import kotlin.js.Promise

external fun createWorker(lang: String = definedExternally,
                          oem: Int = definedExternally,
                          options: WorkerOptions = definedExternally): Promise<TesseractWorker>

external interface WorkerOptions {
    var logger: ((JsAny?) -> Unit)?
    var workerPath: String?
    var langPath: String?
    var corePath: String?
}

external interface TesseractWorker: JsAny {
    fun recognize(image: JsAny?): Promise<TesseractRecognizeResult>
}

external interface TesseractRecognizeResult: JsAny {
    val data: TesseractData
}

external interface TesseractData: JsAny {
    val text: String
}
