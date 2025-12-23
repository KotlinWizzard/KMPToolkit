@file:JsModule("pdfjs-dist/legacy/build/pdf")
@file:JsNonModule
package io.github.kotlinwizzard.kmptoolkit.gallery.pdf



import kotlin.js.Promise


external fun getDocument(src: dynamic): PdfLoadingTask

external object GlobalWorkerOptions {
    var workerSrc: String
}


external interface PdfLoadingTask {
    val promise: Promise<PdfDocument>
}

external interface PdfDocument {
    val numPages: Int
    fun getPage(pageNumber: Int): Promise<PdfPage>
}

external interface PdfPage {
    fun getViewport(params: dynamic = definedExternally): PdfViewport
    fun render(params: dynamic): PdfRenderTask
}

external interface PdfViewport {
    val width: Double
    val height: Double
}

external interface PdfRenderTask {
    val promise: Promise<Unit>
}

